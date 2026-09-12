/** Preload for audited Node test files; a caught unexpected attempt still fails the process. */
'use strict';

const {install} = require('./node-test-transport.cjs');
let blocked = 0;
install({onBlocked(error) { blocked++; process.stderr.write(`${error.code}: ${error.message}\n`); }});
process.on('exit', () => {
  if (blocked) process.exitCode = 1;
});
