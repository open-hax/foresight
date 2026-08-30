---
uuid: "054e1f6f-9186-4101-bbad-6666affb6925"
title: "Define EvidenceRef v1 with resolvers and freshness"
status: "done"
priority: "P0"
labels: [""]
created_at: "2026-08-29T01:42:57.935Z"
parent: "225c8b6b-3ad1-4c29-b5bf-70104630d950"
write-id: "1787968010702-0.jnkai8mx1w80dktzbcv"
---

---
Depends on the delivered Epiphany context foundation. Define source-neutral EvidenceRef v1 in portable .cljc law/shape; migrate Epiphany packet projection; add resolver outcomes resolved/stale/unavailable/unsupported, revision/freshness identity, compatibility tests and docs. No network resolver implementation until the pure contract passes.

Implemented EvidenceRef v1 pure law/shape at the Foresight root. Kinds: Git object, Rheos card/event/workflow, Clio event, skill definition, skill-graph node. Authority-scoped identity is separate from selectors; local paths are rejected from identity. Resolver outcomes preserve resolved/stale/unavailable/unsupported. Migrated Muse Epiphany context projection and semantic Lucene result identity. Verification: root 4/14 + 9/41, Muse 192/500, Epiphany 24/61, zero relevant lint warnings.
---