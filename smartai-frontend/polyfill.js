// Node.js 16 兼容性 polyfill
import { createRequire } from 'module';

const require = createRequire(import.meta.url);

// 确保 globalThis 存在
if (typeof globalThis === 'undefined') {
  global.globalThis = global;
}

// Crypto polyfill for Node.js 16
if (!globalThis.crypto) {
  const nodeCrypto = require('crypto');
  
  globalThis.crypto = {
    getRandomValues: function(array) {
      if (!(array instanceof Uint8Array) && 
          !(array instanceof Uint16Array) && 
          !(array instanceof Uint32Array) &&
          !(array instanceof Int8Array) && 
          !(array instanceof Int16Array) && 
          !(array instanceof Int32Array) &&
          !(array instanceof BigUint64Array) &&
          !(array instanceof BigInt64Array)) {
        throw new TypeError('Argument must be a typed array');
      }
      
      const bytes = nodeCrypto.randomBytes(array.byteLength);
      const view = new Uint8Array(array.buffer, array.byteOffset, array.byteLength);
      view.set(bytes);
      return array;
    },
    
    randomUUID: function() {
      return nodeCrypto.randomUUID();
    },
    
    subtle: {
      digest: async function(algorithm, data) {
        const hash = nodeCrypto.createHash(algorithm.toLowerCase().replace('-', ''));
        hash.update(data);
        return hash.digest();
      }
    }
  };
}

// Web APIs polyfill
if (!globalThis.performance) {
  globalThis.performance = {
    now: function() {
      const hrTime = process.hrtime();
      return hrTime[0] * 1000 + hrTime[1] / 1e6;
    }
  };
}

console.log('✅ Node.js 16 polyfills loaded successfully');