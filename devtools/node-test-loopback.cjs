/** Preload for audited Node test files; an unexpected attempt immediately fails the process. */
'use strict';

const {install} = require('./node-test-transport.cjs');
const {writeSync} = require('node:fs');
// Node's native termination boundary bypasses mutable exit listeners entirely.
// This skips worker cleanup: callers must own and clean up worker temp directories.
const terminate = process.reallyExit.bind(process);
install({onBlocked(error) {
  try {
    writeSync(2, `${error.code}: ${error.message}\n`);
    if (error.endpoint) writeSync(2, `Endpoint: ${JSON.stringify(error.endpoint)}\n`);
  } finally {
    terminate(1);
  }
}});
