(ns verify-cephalon-local
  "Read-only verification of the recovered, explicitly selected local cephalon."
  (:require [clojure.string :as str]
            ["node:child_process" :as child]
            ["node:crypto" :as crypto]
            ["node:fs" :as fs]
            ["node:path" :as path]))

(def deployment "/home/err/.local/share/promethean/services/knoxx-social-local")
(def container "knoxx-social-local-backend-1")
(def failures (atom 0))

(defn check!
  "Print one observed check and retain failures for the final exit status."
  [passed? description]
  (when-not passed? (swap! failures inc))
  (println (if passed? "PASS" "FAIL") description))

(defn command!
  "Run a bounded diagnostic command, rejecting nonzero status without secrets."
  [executable arguments input]
  (let [result (.spawnSync child executable (clj->js arguments)
                          #js {:encoding "utf8" :input input :timeout 60000
                               :maxBuffer 1048576})]
    (when-not (= 0 (.-status result))
      (throw (ex-info "Diagnostic command failed; inspect the named command locally"
                      {:executable executable :status (.-status result)})))
    (.-stdout result)))

(def observation-script
  "(async()=>{
    const h={'x-api-key':process.env.KNOXX_API_KEY};
    const get=async p=>{const r=await fetch('http://127.0.0.1:8000'+p,{headers:h,signal:AbortSignal.timeout(10000)});if(!r.ok)throw Error('HTTP '+r.status+' '+p);return r.json()};
    const unauth=await fetch('http://127.0.0.1:8000/api/admin/config/events',{signal:AbortSignal.timeout(10000)});
    const control=await get('/api/admin/config/events');
    const head=await get('/api/tools/catalog?agent=ussyverse_social_replies&actor=discord_automation');
    const maker=await get('/api/tools/catalog?agent=ussyverse_social_creative&actor=discord_automation');
    const active=await get('/api/admin/agents/active');
    const {MongoClient}=require('mongodb'),db=new MongoClient(process.env.MONGODB_URI);
    let runs;
    try{
      await db.connect();
      runs=await db.db('knoxx').collection('knoxx_runs').find({'settings.agentSpec.contractId':'ussyverse_social_creative'},{projection:{run_id:1,status:1,created_at:1,error:1,'settings.agentSpec.contractId':1,'settings.agentSpec.eventType':1,'settings.agentSpec.scheduleId':1,'tool_receipts.tool_name':1,'tool_receipts.status':1}}).sort({created_at:-1}).limit(8).toArray();
    }finally{await db.close()}
    const p=await fetch('https://public.api.bsky.app/xrpc/app.bsky.feed.getAuthorFeed?actor=open-hax.bsky.social&limit=3',{signal:AbortSignal.timeout(10000)});
    const feed=p.ok?await p.json():{};
    console.log(JSON.stringify({unauth:unauth.status,running:control.runtime.running,
      schedules:control.control.resources.schedule.map(s=>({id:s.id,rule:s.resource.rule})),
      triggers:control.runtime.triggers.map(t=>({id:t.id,enabled:t.enabled,events:t.events})),
      headTools:head.tools.map(t=>t.id),makerTools:maker.tools.map(t=>t.id),
      active:active.runs.map(r=>({id:r.run_id,status:r.status,event:r.latest_event})),runs,
      publications:feed.feed?.map(x=>({uri:x.post.uri,createdAt:x.post.record.createdAt,images:x.post.embed?.images?.length||0})),
      synthesisScript:require('fs').existsSync('/app/scripts/synthesize-music.mjs')}));
  })().catch(e=>{console.error(e.name+': '+e.message);process.exitCode=1})")

(defn verify!
  "Verify image/contract identity before inspecting runtime and artifacts."
  []
  (when-not (= ["--only" "knoxx"] (vec *command-line-args*))
    (throw (ex-info "Usage: nbb scripts/verify_cephalon_local.cljs --only knoxx" {})))
  (let [manifest (js->clj (js/JSON.parse (.readFileSync fs (path/join deployment "contracts-manifest.json") "utf8"))
                         :keywordize-keys true)
        image (str/trim (command! "docker" ["inspect" "--format" "{{.Image}}" container] nil))]
    (check! (= image (:runtimeImageId manifest)) "served image digest matches the deployment manifest")
    (when (pos? @failures)
      (throw (ex-info "Wrong served image; stop before misleading runtime verification" {})))
    (doseq [{relative :path expected :sha256} (:files manifest)]
      (let [bytes (.readFileSync fs (path/join deployment "contracts" relative))
            actual (.digest (.update (.createHash crypto "sha256") bytes) "hex")]
        (when-not (= expected actual)
          (check! false (str "contract snapshot hash drift: " relative)))))
    (check! (zero? @failures) "contract snapshot hashes match; source revision and local overlays remain distinct")
    (let [observed (js->clj (js/JSON.parse (command! "docker" ["exec" "-i" container "node"] observation-script))
                           :keywordize-keys true)
          head-tools (set (:headTools observed))
          maker-tools (set (:makerTools observed))]
      (check! (#{401 403} (:unauth observed)) "anonymous callers cannot control the event runtime")
      (check! (:running observed) "Knoxx event runtime is running")
      (check! (some #(= "*/15 * * * *" (:rule %)) (:schedules observed)) "native Knoxx schedule retains the 15-minute creative cadence")
      (check! (some #(and (:enabled %) (some #{"creative-request" "cephalon/creative-request"} (:events %)))
                    (:triggers observed)) "non-clock creative-request event is bound to an enabled trigger")
      (check! (= head-tools #{"discord.send" "discord.read" "discord.react" "agents.spawn"})
              "head exposes only four conversation/delegation tools")
      (check! (every? maker-tools ["bash" "write" "music.generate" "bluesky.publish" "discord.send"])
              "maker exposes native creation and publication tools")
      (check! (:synthesisScript observed) "the actual image contains the native music engine")
      (check! (some #(pos? (:images %)) (:publications observed)) "public Bluesky API independently observes an image post")
      (println "OBSERVED active runs:" (pr-str (:active observed)))
      (println "OBSERVED recent maker runs:" (pr-str (:runs observed)))
      (println "OBSERVED native publication identities:" (pr-str (:publications observed)))
      (doseq [gap (:operationalGaps manifest)] (println "WARN" gap))
      (println "No agent invocation, publication, contract change or board transition was performed."))))

(try
  (verify!)
  (catch :default error
    (swap! failures inc)
    (println "FAIL" (ex-message error))))
(println "Failures:" @failures)
(when (pos? @failures) (set! (.-exitCode js/process) 1))
