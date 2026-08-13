# Eta Harness

Eta is a Foresight root-owned consolidation input. Inherit the workspace rules in
[`../AGENTS.md`](../AGENTS.md); they are authoritative when this file is silent.

## Eta-specific boundary

Eta is a harness for transduction and agent-mediated work. Do not let the harness
become the domain model.

- New reusable shapes, laws, artifact contracts, tool/result semantics, turn
  decisions, and other pure logic should be `.cljc` whenever practical.
- Keep JLine, HTTP clients, nREPL handles, filesystem/process access, provider
  SDK values, and other runtime details in adapter/infra namespaces.
- A pure function that currently depends on JVM or JS values should be split at
  the conversion boundary before reuse.
- Eta may remain JVM Clojure where its current capabilities benefit from it, but
  new functionality must not assume the JVM unless the capability actually
  requires it.
- When an Eta capability is naturally Node-adjacent, prefer an NBB runner over
  adding more compiled shadow-cljs machinery. When it is host/CLI oriented and
  Node is unnecessary, Babashka is preferred.
- Provider output is untrusted input. Validate it against reusable Katamorph
  contracts before it becomes an artifact supplied to another stage.

The architectural goal is that Eta's semantic kernel can move between runtimes
without changing what an artifact, operation, receipt, or lawful transition
means.
