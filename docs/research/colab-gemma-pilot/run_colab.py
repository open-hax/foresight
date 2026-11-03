# SPDX-License-Identifier: GPL-3.0-or-later
"""Foreign-I/O adapter for one campaign. CPU-only, fixed controller-owned inputs.
Budget decisions live in budget_law.cljc. Re-run resumes the same 30-unit cap.
Never installs a candidate in production. ADC remains on Stealth.
"""
import datetime, fcntl, hashlib, json, os, pathlib, re, signal, subprocess, sys, tarfile, threading, time, uuid
HERE=pathlib.Path(__file__).resolve().parent
DATA=pathlib.Path('/home/err/.local/share/promethean/research/colab-gemma-20261025')
ADC=pathlib.Path('/home/err/.local/share/promethean/services/colab-mcp-stealth/gcloud/application_default_credentials.json')
CLI='/home/err/.local/bin/colab'
CLI_PY='/home/err/.local/share/uv/tools/google-colab-cli/bin/python'
CAMPAIGN='colab-gemma-2026-10-25'
FILES=['cases-24.json','corpus.npz','retrieval-meta.json','queries.npz']
MAX_SECONDS=600

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def now():return datetime.datetime.now(datetime.timezone.utc).isoformat()
def atomic_json(path,value):
    temp=path.with_suffix('.tmp');temp.write_text(json.dumps(value,indent=2)+'\n');temp.chmod(0o600)
    with temp.open('rb') as f:os.fsync(f.fileno())
    temp.replace(path)
    fd=os.open(path.parent,os.O_RDONLY);os.fsync(fd);os.close(fd)

def parse_usage(text):
    patterns={'balance':r'Current balance: ([0-9]+(?:\.[0-9]+)?) compute units','rate':r'Usage rate: ([0-9]+(?:\.[0-9]+)?)/hr','assignments':r'Active assignments: ([0-9]+)'}
    result={}
    for key,pattern in patterns.items():
        matches=re.findall(pattern,text)
        if len(matches)!=1:raise RuntimeError('Unrecognized Colab usage response')
        result[key]=float(matches[0])
    result['observed_at']=now();return result

class Campaign:
    def __init__(self):
        self.env={**os.environ,'GOOGLE_APPLICATION_CREDENTIALS':str(ADC),'NO_COLOR':'1','TERM':'dumb'}
        self.base=[CLI,'--auth','adc','--config',str(DATA/'colab-sessions.json')]
        self.statefile=DATA/'campaign-state.json';self.state=None;self.started=None
        self.abort=threading.Event();self.done=threading.Event();self.active=None;self.guard_error=None
    def command(self,args,timeout=90,watch=False):
        with (DATA/'adapter-private.log').open('a') as log:
            proc=subprocess.Popen(self.base+args,stdout=log,stderr=log,env=self.env,start_new_session=True)
            if watch:self.active=proc
            try:
                end=time.monotonic()+timeout
                while proc.poll() is None:
                    if time.monotonic()>end or (watch and self.abort.is_set()):
                        os.killpg(proc.pid,signal.SIGTERM)
                        try:proc.wait(timeout=5)
                        except subprocess.TimeoutExpired:os.killpg(proc.pid,signal.SIGKILL);proc.wait()
                        raise RuntimeError('CLI interrupted: timeout or budget watchdog; '+str(self.guard_error))
                    time.sleep(.25)
                if proc.returncode:raise RuntimeError('Colab command failed; inspect private adapter log')
            finally:
                if watch:self.active=None
    def usage(self):
        r=subprocess.run(self.base+['usage'],env=self.env,capture_output=True,text=True,timeout=20,check=True)
        return parse_usage(r.stdout)
    def endpoints(self):
        # Direct SDK call fails on auth/network errors. CLI sessions may convert auth failure to an empty list.
        code="from colab_cli.auth import get_credentials,AuthProvider;from colab_cli.client import Client,Prod;import json;c=Client(Prod(),get_credentials('',provider=AuthProvider.ADC));print(json.dumps([a.endpoint for a in c.list_assignments()]))"
        r=subprocess.run([CLI_PY,'-c',code],env=self.env,capture_output=True,text=True,timeout=20,check=True)
        value=json.loads(r.stdout)
        if not isinstance(value,list) or any(not isinstance(x,str) for x in value):raise RuntimeError('Invalid session evidence')
        return set(value)
    def decision(self,usage):
        inp={'campaign-id':CAMPAIGN,'spent':self.state['committed_units'],'balance':usage['balance'],'initial-balance':self.state['initial_balance'],'rate':usage['rate'],'cleanup-verified':self.state['cleanup_verified'],'telemetry-age-seconds':max(0,(datetime.datetime.now(datetime.timezone.utc)-datetime.datetime.fromisoformat(usage['observed_at'])).total_seconds())}
        r=subprocess.run(['bb',str(HERE/'law_cli.clj')],input=json.dumps(inp),capture_output=True,text=True,timeout=10,check=True)
        return json.loads(r.stdout)
    def persist(self):atomic_json(self.statefile,self.state)
    def watchdog(self):
        while not self.done.wait(10):
            try:
                u=self.usage();observed=max(0,self.state['initial_balance']-u['balance'])
                # Reservation is charged before launch and never refunded. Two units remain for delayed billing/cleanup.
                if observed>=28 or u['rate']>1 or time.monotonic()-self.started>MAX_SECONDS:
                    raise RuntimeError('Budget/rate/deadline reached')
            except Exception as e:
                self.guard_error=type(e).__name__+': '+str(e);self.abort.set();return
    def cleanup(self):
        name=self.state['active_job']['session'];endpoint=self.state['active_job'].get('endpoint')
        if endpoint is None:
            p=DATA/'colab-sessions.json'
            if p.exists():endpoint=json.loads(p.read_text()).get(name,{}).get('endpoint')
        if endpoint is None:
            self.state['cleanup_verified']=False;self.persist();return False
        self.command(['stop','--session',name],timeout=45)
        absent=endpoint not in self.endpoints()
        self.state['cleanup_verified']=absent
        self.state['active_job']['runtime_released']=absent
        self.state['active_job']['endpoint']=endpoint
        self.persist();return absent
    def replay_yoga(self, jobdir):
        remote='/home/err/.local/share/promethean/research/colab-gemma-20261025/'+jobdir.name
        ssh=['ssh','-o','BatchMode=yes','-o','ForwardAgent=no','-o','ConnectTimeout=8','yoga']
        scp=['scp','-q','-o','BatchMode=yes','-o','ForwardAgent=no','-o','ConnectTimeout=8']
        subprocess.run(ssh+['mkdir','-p','-m','700',remote],check=True,timeout=20)
        files=[str(DATA/name) for name in FILES]+[str(jobdir/'candidate.json'),str(HERE/'evaluate.py')]
        subprocess.run(scp+files+['yoga:'+remote+'/'],check=True,timeout=120)
        subprocess.run(ssh+['env','OPENBLAS_NUM_THREADS=1','python3',remote+'/evaluate.py',remote,remote+'/yoga-replay.json','--candidate',remote+'/candidate.json'],check=True,timeout=90)
        subprocess.run(scp+['yoga:'+remote+'/yoga-replay.json',str(jobdir/'yoga-replay.json')],check=True,timeout=30)
        subprocess.run([sys.executable,str(HERE/'verify_replays.py'),str(jobdir)],check=True,timeout=15)

    def run(self):
        os.umask(0o077);DATA.mkdir(parents=True,exist_ok=True)
        with (DATA/'campaign.lock').open('a') as lock:
            fcntl.flock(lock,fcntl.LOCK_EX|fcntl.LOCK_NB)
            u=self.usage()
            if self.statefile.exists():
                self.state=json.loads(self.statefile.read_text())
                if self.state['campaign']!=CAMPAIGN or self.state['cap_units']!=30:raise RuntimeError('Campaign identity/cap drift')
                if not self.state['cleanup_verified']:
                    if not self.cleanup():raise RuntimeError('Prior owned-runtime cleanup remains uncertain')
                    u=self.usage()
            else:
                self.state={'campaign':CAMPAIGN,'cap_units':30,'initial_balance':u['balance'],'committed_units':0,'cleanup_verified':True,'created_at':now(),'jobs':[]}
            decision=self.decision(u)
            if not decision['admit']:raise RuntimeError('Admission denied: '+json.dumps(decision))
            package=DATA/'research-inputs.tar.gz'
            with tarfile.open(package,'w:gz') as tar:
                for name in FILES:tar.add(DATA/name,arcname=name)
                tar.add(HERE/'evaluate.py',arcname='evaluate.py')
            hashes={name:sha(DATA/name) for name in FILES};hashes['evaluate.py']=sha(HERE/'evaluate.py')
            job_id='research-'+uuid.uuid4().hex[:12];name='knoxx-colab-'+job_id
            jobdir=DATA/'jobs'/job_id;jobdir.mkdir(parents=True)
            self.state['committed_units']=decision['charged']+decision['reservation']
            self.state['cleanup_verified']=False
            self.state['active_job']={'id':job_id,'session':name,'created_at':now(),'status':'reserved','input_hashes':hashes,'before_endpoints':sorted(self.endpoints())}
            self.persist();self.started=time.monotonic()
            worker=threading.Thread(target=self.watchdog,daemon=True);worker.start()
            # Cancellation is handled by the same finally block as job and watchdog failures.
            def cancelled(signum,frame):self.abort.set();raise KeyboardInterrupt()
            signal.signal(signal.SIGTERM,cancelled);signal.signal(signal.SIGINT,cancelled)
            failure=None
            try:
                print('Reserved 2 units within 30-unit campaign; CPU allocation',flush=True)
                self.command(['new','--session',name],timeout=150,watch=True)
                state=json.loads((DATA/'colab-sessions.json').read_text())[name]
                self.state['active_job']['endpoint']=state['endpoint'];self.persist()
                u=self.usage()
                if u['rate']>1 or max(0,self.state['initial_balance']-u['balance'])>=28:raise RuntimeError('Post-allocation rate/balance not admissible')
                self.command(['upload','--session',name,str(package),'/content/research-inputs.tar.gz'],timeout=120,watch=True)
                script=jobdir/'remote_job.py'
                code="""import pathlib,hashlib,json,tarfile,subprocess,sys,os
root=pathlib.Path('/content/knoxx-research');root.mkdir(exist_ok=True)
expected=EXPECTED
with tarfile.open('/content/research-inputs.tar.gz') as tar:
    assert set(tar.getnames())==set(expected)
    for m in tar.getmembers():
        assert m.isfile() and m.name in expected
        data=tar.extractfile(m).read();assert hashlib.sha256(data).hexdigest()==expected[m.name]
        (root/m.name).write_bytes(data)
os.environ['OPENBLAS_NUM_THREADS']='1'
subprocess.run([sys.executable,str(root/'evaluate.py'),str(root),str(root/'candidate.json')],check=True,timeout=240)
print('RESULT_SHA256='+hashlib.sha256((root/'candidate.json').read_bytes()).hexdigest())
""".replace('EXPECTED',repr(hashes))
                script.write_text(code)
                self.command(['exec','--session',name,'--file',str(script),'--timeout','260'],timeout=290,watch=True)
                self.command(['download','--session',name,'/content/knoxx-research/candidate.json',str(jobdir/'candidate.json')],timeout=60,watch=True)
                result=json.loads((jobdir/'candidate.json').read_text())
                if result['input_hashes']!={k:v for k,v in hashes.items() if k!='evaluate.py'} or result['evaluator_sha256']!=hashes['evaluate.py']:raise RuntimeError('Result/input fingerprint mismatch')
                # Verify the output against the local deterministic scorer before trusting remote metrics.
                subprocess.run([sys.executable,str(HERE/'evaluate.py'),str(DATA),str(jobdir/'stealth-replay.json'),'--candidate',str(jobdir/'candidate.json')],env={**self.env,'OPENBLAS_NUM_THREADS':'1'},timeout=60,check=True)
                self.state['active_job'].update(status='completed',result_sha256=sha(jobdir/'candidate.json'),selected_config=result['selected_config'])
            except BaseException as e:
                failure=e;self.state['active_job']['status']='failed';self.state['active_job']['failure']=type(e).__name__+': '+str(e)
            finally:
                self.done.set();worker.join(timeout=22)
                try:
                    if not self.cleanup():raise RuntimeError('Owned runtime release unverified; further jobs blocked')
                    after=self.usage();self.state['last_usage']=after
                    observed=max(0,self.state['initial_balance']-after['balance'])
                    self.state['committed_units']=max(self.state['committed_units'],observed)
                    self.state['active_job']['completed_at']=now();self.state['jobs'].append(dict(self.state['active_job']))
                    self.persist()
                except BaseException as cleanup_error:
                    self.state['cleanup_verified']=False;self.persist()
                    raise RuntimeError('Cleanup/billing readback failed; next allocation blocked') from cleanup_error
            if failure:raise failure
            try:
                self.replay_yoga(jobdir)
                self.state['active_job']['isolated_local_validation']='stealth+yoga passed; production held'
            except BaseException:
                self.state['active_job']['isolated_local_validation']='blocked; production held'
                self.state['jobs'][-1]=dict(self.state['active_job']);self.persist();raise
            self.state['jobs'][-1]=dict(self.state['active_job']);self.persist()
            print(json.dumps({'job':job_id,'status':'completed','cleanup_verified':self.state['cleanup_verified'],'initial_balance':self.state['initial_balance'],'last_usage':self.state['last_usage'],'committed_units':self.state['committed_units'],'candidate':str(jobdir/'candidate.json')}),flush=True)

if __name__=='__main__':Campaign().run()
