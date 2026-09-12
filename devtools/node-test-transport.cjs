/** Node test-host boundary: native transports may reach only this process's listeners. */
'use strict';

const net = require('node:net');
const tls = require('node:tls');
const dns = require('node:dns');
const dgram = require('node:dgram');
const {syncBuiltinESMExports} = require('node:module');

function connectOptions(args) {
  const [first, second] = args;
  if (Array.isArray(first)) return first[0];
  if (first && typeof first === 'object') return first;
  if (typeof first === 'number' || (typeof first === 'string' && /^\d+$/.test(first))) {
    return {port:Number(first), host:typeof second === 'string' ? second : 'localhost'};
  }
  return {path:first};
}

function install({onBlocked = () => {}} = {}) {
  const listeners = new Map();
  const restores = [];
  const loopback = host => host === '127.0.0.1' || host === '::1';
  function block(kind, endpoint) {
    const error = Object.assign(new Error(`Test transport refused ${kind}; use an owned literal loopback listener`),
      {code:'ERR_TEST_TRANSPORT_NOT_OWNED', endpoint});
    onBlocked(error);
    throw error;
  }
  function ownConnection(args) {
    const options = connectOptions(args);
    const host = options?.host || options?.hostname;
    if (!options || options.path || !loopback(options.host || options.hostname)
        || ![...listeners.entries()].some(([server, address]) => server.listening
          && address.address === host && address.port === Number(options.port))) {
      block('a connection', {host:host || null, port:options?.port || null});
    }
  }
  function patch(owner, name, replacement) {
    const original = owner[name];
    owner[name] = replacement(original);
    restores.push(() => {owner[name] = original;});
  }
  patch(net.Server.prototype, 'listen', original => function (...args) {
    const record = () => {
      const address = this.address();
      if (address && typeof address === 'object' && loopback(address.address)) listeners.set(this, address);
    };
    this.prependOnceListener('listening', record);
    this.once('close', () => listeners.delete(this));
    try { return original.apply(this, args); }
    catch (error) { this.removeListener('listening', record); throw error; }
  });
  patch(net.Socket.prototype, 'connect', original => function (...args) {
    ownConnection(args);
    return original.apply(this, args);
  });
  patch(tls, 'connect', original => function (...args) {
    ownConnection(args);
    return original.apply(this, args);
  });
  // Refuse DNS before resolution: hostname aliases are not evidence of ownership.
  for (const owner of [dns, dns.promises, dns.Resolver.prototype, dns.promises.Resolver.prototype]) {
    for (const name of Object.getOwnPropertyNames(owner).filter(key => key.startsWith('lookup') || key.startsWith('resolve') || key === 'reverse')) {
      if (typeof owner[name] !== 'function') continue;
      patch(owner, name, original => function (host, ...args) {
        if (name !== 'lookup' || !loopback(host)) block('DNS resolution');
        return original.call(this, host, ...args);
      });
    }
  }
  for (const name of ['connect', 'send']) {
    patch(dgram.Socket.prototype, name, () => () => block('a datagram operation'));
  }
  patch(dgram, 'Socket', () => function () { return block('a datagram socket'); });
  patch(dgram, 'createSocket', () => () => block('a datagram socket'));
  syncBuiltinESMExports();
  return {
    close() {
      for (const restore of restores.reverse()) restore();
      listeners.clear();
      syncBuiltinESMExports();
    },
  };
}

module.exports = {install};
