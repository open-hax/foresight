/** Preload for audited Node test files; a caught unexpected attempt still fails the process. */
'use strict';

const {install} = require('./node-test-transport.cjs');
const exit = process.exit.bind(process);
let blocked = 0;
install({onBlocked(error) {
  blocked++;
  const endpoint = error.endpoint ? ` ${JSON.stringify(error.endpoint)}` : '';
  process.stderr.write(`${error.code}: ${error.message}${endpoint}\n`);
}});
process.on('exit', () => {
  // Native exit ends this event immediately, before later listeners can reset it.
  if (blocked) exit(1);
});
