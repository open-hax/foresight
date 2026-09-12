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
Redirects therefore cannot escape through an unmatched fetch fallback. Only the
blocked host and port are logged for fixture diagnosis; no request body,
credential, header or response body is logged by the adapter.

A refused attempt makes the process fail even if application code catches it.
Nine native regression tests prove owned HTTP/fetch operation, refusal through
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

Codex's subsequent review found that assigning `process.exitCode` from the early
exit listener could be undone by a later listener. Two real child-process
regressions reproduced a successful exit after a caught refusal. The preload now
captures native `process.exit` and ends the exit event with status 1 before later
listeners execute. A subsequent Codex review found that a prepended listener
could call `process.exit(0)` before that listener ran. Three additional real child
cases reproduced two failures, including the named ESM `exit` export. The preload
now also protects explicit exits and synchronizes the builtin exports. The
nine-test proof covers both listener orders, both reset forms, explicit success
exit, and inherited Node test children; it exits zero with no skips.

A fresh guarded Proxx suite ran 651 tests: 647 passed, two test files failed, and
two existing cases skipped. Fourteen refused attempts exposed a LAN Ollama
default, the quota monitor's remote usage endpoint, an undeclared Chroma service,
and request pools reconnecting after fixture listeners closed. No rejected old
process was resumed. Corrected local fixtures first passed 187 tests with one
existing skip and no refused transports. The complete successor run passed 650
of 651 tests with no failures or refused transports; one pre-existing external
bootstrap-script contract remains skipped because its sibling script is absent.
The previously skipped cloud reasoning case now runs and asserts the upstream
request's maximum reasoning setting. Both production builds and full ESLint
complete successfully; ESLint still reports 236 existing complexity warnings.
