// ES Module loader for Node.js 16 polyfills
import { createRequire } from 'module';

const require = createRequire(import.meta.url);

// Apply polyfills before any module loading
export async function resolve(specifier, context, defaultResolve) {
  // Apply crypto polyfill
  if (!globalThis.crypto) {
    const nodeCrypto = require('crypto');
    
    globalThis.crypto = {
      getRandomValues: function(array) {
        if (!(array instanceof Uint8Array) && 
            !(array instanceof Uint16Array) && 
            !(array instanceof Uint32Array) &&
            !(array instanceof Int8Array) && 
            !(array instanceof Int16Array) && 
            !(array instanceof Int32Array)) {
          throw new TypeError('Argument must be a typed array');
        }
        
        const bytes = nodeCrypto.randomBytes(array.byteLength);
        const view = new Uint8Array(array.buffer, array.byteOffset, array.byteLength);
        view.set(bytes);
        return array;
      },
      
      randomUUID: function() {
        return nodeCrypto.randomUUID();
      }
    };
  }
  
  return defaultResolve(specifier, context);
}