# Claude — Eta

Read and obey [`../AGENTS.md`](../AGENTS.md) and [`AGENTS.md`](AGENTS.md) before
changing Eta.

Do not expand this file into a second architecture policy. The important rule is
simple: **purify before you port**. Extract portable `.cljc` shapes, laws and pure
functions first; keep JVM/Node/UI/provider effects at the boundary; choose NBB,
Babashka, JVM Clojure, or compiled CLJS only because the capability requires it,
not because predecessor code happened to use it.
