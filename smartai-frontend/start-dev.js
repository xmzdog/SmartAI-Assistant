// Node.js compatibility script for Vite
import { createRequire } from 'module';
import { fileURLToPath } from 'url';
import path from 'path';

// Polyfill for crypto.getRandomValues if not available
if (typeof globalThis.crypto === 'undefined') {
  globalThis.crypto = {};
}

if (typeof globalThis.crypto.getRandomValues === 'undefined') {
  const require = createRequire(import.meta.url);
  const crypto = require('crypto');
  
  globalThis.crypto.getRandomValues = function(array) {
    return crypto.randomFillSync(array);
  };
}

// Start Vite dev server
const { createServer } = await import('vite');

const server = await createServer({
  // 任何有效的用户配置选项，加上 `mode` 和 `configFile`
  configFile: false,
  root: path.dirname(fileURLToPath(import.meta.url)),
  server: {
    port: 3000,
    open: true
  }
});

await server.listen();

server.printUrls();