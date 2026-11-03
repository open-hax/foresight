import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import path from 'node:path';
import { homedir } from 'node:os';

process.loadEnvFile(path.join(homedir(), '.secrets/knoxx/local.env'));
const workspace = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const child = spawn(process.execPath, ['dist/server.js'], {
  cwd: path.join(workspace, 'knoxx', 'backend'), env: {
    ...process.env,
    WORKSPACE_ROOT: workspace,
    CONTRACTS_DIR: path.join(workspace, 'knoxx', 'contracts'),
    KNOXX_DISABLE_EVENT_RUNTIMES: 'true',
  }, stdio: 'inherit',
});
for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, () => child.kill(signal));
}
child.on('exit', (code, signal) => {
  process.exitCode = code ?? (signal ? 1 : 0);
});
