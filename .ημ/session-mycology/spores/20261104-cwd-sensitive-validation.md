---
status: incubating
created: 2026-11-04T04:40:09Z
source-task: Oikos typed dependency correction
p-efficiency: 0.35
p-friction: 0.95
p-skill-candidate: 0.95
promoted-to: ""
rejected-reason: ""
---

## Problem

A report-validation `bb -e` launched from an upstream checkout implicitly loaded
that checkout's dependency configuration. A harmless expression did not make the
launcher harmless: it performed unapproved network/global-cache resolution.

## Pattern

Working directory, environment and implicit configuration are executable inputs
to validation tools, even when the expression only parses EDN or prints a hash.
The guarded test was not the incident; an auxiliary command outside its wrapper was.

## Better path

- Bind an explicit empty tool configuration or use an independently checked
  nonproject working directory for artifact-only validation.
- Keep every command, including report checks, within the reviewed invocation
  boundary; do not assume a package manager or harness launcher is just a parser.
- Preserve exact argv, cwd, start/end, output and pre-command cache inventory.
- After a breach, stop the affected lane, retain evidence, inspect read-only,
  and do not reset or delete shared cache files without attribution and permission.
- Separate a successful guarded RED test from a failed GREEN candidate and from
  later validation side effects. None supplies an accepted replacement.

## Receipt refs

- `.ημ/runs/oikos-program-20261103/incidents/typed-cwd-dependency-resolution-v1.edn`
- `.ημ/runs/oikos-program-20261103/reviews/typed-seq-annotation-fix-v2.edn`
- `.ημ/runs/oikos-program-20261103/decisions/typed-guarded-continuation-and-parser-source-authorization.edn`

Do not promote this candidate in its creating session.
