const path = require('node:path');
const os = require('node:os');

const workspace = __dirname;

module.exports = {
  apps: [
    {
      name: 'axxium-local',
      cwd: path.join(workspace, 'axxium'),
      script: 'node',
      args: [`--env-file=${path.join(os.homedir(), '.secrets/axxium/local.env')}`, 'dist/server.js'],
      interpreter: 'none',
      autorestart: true,
      max_restarts: 5,
      restart_delay: 3000,
      watch: false,
    },
    {
      name: 'knoxx-axxium-local',
      cwd: workspace,
      script: 'node',
      args: ['scripts/start-knoxx-identity-local.mjs'],
      interpreter: 'none',
      autorestart: true,
      max_restarts: 5,
      restart_delay: 5000,
      watch: false,
      env: {
        NODE_ENV: 'development',
        HOST: '127.0.0.1',
        PORT: '8003',
        KNOXX_PUBLIC_BASE_URL: 'http://127.0.0.1:8003',
        KNOXX_BASE_URL: 'http://127.0.0.1:8003',
        MONGODB_DB: 'knoxx_axxium_local',
        KNOXX_AXXIUM_ORIGIN: 'http://127.0.0.1:8788',
      },
    },
    {
      name: 'knoxx-axxium-ui',
      cwd: path.join(workspace, 'knoxx', 'frontend'),
      script: path.join(workspace, 'knoxx', 'frontend', 'node_modules', 'vite', 'bin', 'vite.js'),
      args: ['--config', 'vite.identity-local.config.ts'],
      autorestart: true,
      max_restarts: 5,
      restart_delay: 3000,
      watch: false,
    },
  ],
};
