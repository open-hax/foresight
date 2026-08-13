---
original_name: "2026.08.13.16.46.08.md (chatgpt export, lines 4544-4556)"
title: "Purifying katamorph.schema to .cljc"
summary: "Plans a behavior-preserving move of katamorph.schema from src/cljs to src/cljc: the schema registry is already almost entirely portable Malli data, with only a CLJS-only catch :default in coerce needing a reader-conditional fix, plus a new JVM test lane as portability evidence."
category: "design"
created: "2026-08-13"
---

# you asked

do it give us another PR. Just keep on goin.

---

# chatgpt response

I’m taking the next seam exactly as identified: **purify Katamorph’s schema/validation core to `.cljc` without changing its public namespace**, then stack a Foresight integration PR on top so Alpha actually consumes the purified boundary instead of duplicating Malli mechanics. I’ll keep the move behavior-preserving first; new Alpha-specific registration can be a separate small commit once portability is proven.

The Katamorph seam is cleaner than expected: the schema registry is already almost entirely portable. The only actual runtime leak in the core is the CLJS-only `catch :default` in `coerce`; the rest is Malli data + pure validation/inference. So this PR can be a **real source move**, not a rewrite: same `katamorph.schema` namespace, moved to `src/cljc`, reader-conditional exception handling, JVM portability tests, and source-path/lint updates.

The other open Katamorph PR (#4) only touches `.clj-kondo` import config and `.gitignore`, so this portability move won’t collide with it. I’m preserving the public namespace and every existing contract shape; the only behavioral change is making the exception catch legal on both CLJ and CLJS, then adding a JVM test lane so “portable” becomes executable evidence rather than a filename claim.
