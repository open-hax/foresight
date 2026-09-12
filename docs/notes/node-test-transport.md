# Owned local transports for Node integration tests

The earlier Proxx full-suite attempt was rejected by automatic approval review
because a fixture could reach an unverified private HTTPS fallback. That action
and its process have not been resumed. Read-only inspection found partial fetch
mocks which fall back to native fetch, and provider registries with nonlocal
defaults. The test surface uses Node HTTP, fetch and WebSocket transports; the
audited `src` tree has no child-process, worker, datagram or raw native-binding
imports.

`devtools/node-test-loopback.cjs` is a test-only preload for a fresh, bounded
execution. It uses the native adapter in `node-test-transport.cjs` to admit only
literal loopback address/port pairs owned by a currently listening server in the
same test process. Wildcard listeners and hostname aliases are not evidence of
ownership. Native TCP and TLS connections are checked before delegation;
module-level and resolver-instance DNS operations and datagrams are refused.
Redirects therefore cannot escape through an unmatched fetch fallback. No model
request, credential, header or response body is logged by the adapter.

A refused attempt makes the process fail even if application code catches it.
Three native regression tests prove owned HTTP/fetch operation, refusal through
TCP/TLS/DNS/UDP and redirects, immediate closure revocation, IPv4/IPv6 separation,
and propagation into real Node test child processes. An early peer review found
the address-family, resolver-instance and exported UDP-constructor gaps; those
were closed and regression coverage added before any Proxx suite execution.

Run the adapter's proof with:

```sh
node --test devtools/node-test-transport.test.cjs
```

This is an adapter for the audited Node test surface, not an OS sandbox or a
claim that arbitrary native extensions and child programs are contained. It does
not authorize OpenCode/Bun execution or any previously rejected process action.
A future test introducing another native transport must extend and verify the
boundary before execution. Full-suite failures caused by an unmatched fallback
remain failures and identify fixtures that need owned local endpoints; they
must not be converted to skipped tests or silently mocked success.

At this checkpoint the three adapter tests pass. Proxx's new full suite has not
yet run; its result must be recorded separately from this transport proof.
