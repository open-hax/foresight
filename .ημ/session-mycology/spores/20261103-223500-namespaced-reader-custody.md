---
status: incubating
created: 2026-11-03T22:35:00Z
source-session: plan/oikos-execution-20261103
source-task: Qualify private Clio readback without weakening plain-input law
p-efficiency: 0.7
p-friction: 0.75
p-skill-candidate: 0.85
promoted-to: ""
rejected-reason: ""
---

## Problem

An owner reader adds location metadata, accepts raw metadata/discards and can
silently collapse namespace-expanded key aliases. Value equality after reading
does not establish faithful or safe readback.

## Pattern

Keep raw in-memory rejection strict. Capture and hash the same immutable bytes;
apply a small private refusal profile before the owner reader; check the whole
decoded tree before fresh plain construction. Test the owner's actual default
printer, including namespaced maps and mixed-key ordinary maps. Reject lexical
keys that could collide after namespace expansion. Guard new outer containers
before mapping, not only their elements.

## Candidate skill outline

- Trigger: owner-reader metadata, canonical readback, namespaced EDN wire.
- Reproduce actual reader/printer behavior in every supported runtime.
- Separate byte custody, representation validation and owner semantic admission.
- Test alias pairs in both orders, raw container metadata and same-byte races.
- Keep owner parsing/canonicalization authoritative; do not build another kernel.
- Obtain independent exact-source review before connecting storage effects.

## Better path

Probe the actual serializer/reader before writing persistence tests. Include
namespace-expanded aliases and raw outer-container wrappers in the first RED
matrix, alongside tags, metadata and byte limits.

## Receipt refs

- `.ημ/runs/oikos-program-20261103/reviews/s2a-readback-boundary-design.edn`
- `.ημ/runs/oikos-program-20261103/reviews/s2a-wire-code-v1.edn`
- `.ημ/runs/oikos-program-20261103/reviews/s2a-wire-fix-review-v2.edn`
- `.ημ/runs/oikos-program-20261103/permits/s2a-wire-97e41a6b.edn`

No promotion occurred in the creating session.
