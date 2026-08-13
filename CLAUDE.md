# Claude — Foresight

Read and obey [`AGENTS.md`](AGENTS.md) before changing this workspace. It is the
canonical architectural instruction file for Foresight; do not restate or fork
its policy here.

In particular, the **Divine mandate: purify before you port** is binding:
portable data, shapes, laws, validation, ledger semantics, graph logic, and pure
functions default to `.cljc`; runtime-specific code stays at the outer edge.
Prefer the lightest adequate runtime (NBB → Babashka → JVM Clojure, with compiled
CLJS only when the target requires it) without allowing runtime choice to split
the semantic model.

When consolidating predecessor code, extract the lawful/pure kernel first and
leave incidental runtime coupling behind.
