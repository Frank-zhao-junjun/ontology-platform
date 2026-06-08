import { spawnSync } from 'node:child_process';

const pnpmCommand = 'pnpm';

function quoteArg(arg) {
  return /[\s"]/u.test(arg) ? `"${arg.replace(/"/gu, '\\"')}"` : arg;
}

function runCommand(args, extraEnv = {}) {
  const result = process.platform === 'win32'
    ? spawnSync(process.env.ComSpec || 'cmd.exe', ['/d', '/s', '/c', [pnpmCommand, ...args].map(quoteArg).join(' ')], {
      stdio: 'inherit',
      shell: false,
      env: {
        ...process.env,
        ...extraEnv,
      },
    })
    : spawnSync(pnpmCommand, args, {
    stdio: 'inherit',
    shell: false,
    env: {
      ...process.env,
      ...extraEnv,
    },
  });

  if (result.error) {
    console.error(result.error);
    process.exit(1);
  }

  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

runCommand([
  'install',
  '--prefer-frozen-lockfile',
  '--prefer-offline',
  '--loglevel',
  'debug',
  '--reporter',
  'append-only',
]);

runCommand(['exec', 'vite', 'build'], {
  NODE_OPTIONS: [process.env.NODE_OPTIONS, '--max-old-space-size=4096']
    .filter(Boolean)
    .join(' '),
});

runCommand([
  'tsup',
  'server/server.ts',
  '--format',
  'cjs',
  '--platform',
  'node',
  '--target',
  'node20',
  '--outDir',
  'dist-server',
  '--no-splitting',
  '--no-minify',
  '--external',
  'vite',
]);