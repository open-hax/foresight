# Contributing to Foresight

Foresight is a constellation, not a conventional monorepo. A direct submodule
remains an independently owned repository with its own runtime, package policy,
process, and quality gates.

## Start here

Before editing, ask the checked-in project model for the current view:

```sh
git submodule update --init
nbb scripts/project.clj validate
nbb scripts/project.clj guide
nbb scripts/workspace.clj inventory
```

`guide` is projected from `src/foresight/project.cljc`; it is the quickest way
to see the current repository routes and the Foresight work/learning loop
without relying on prior chat or operator memory.

Then enter the likely owning repository and read its local `AGENTS.md` when
present, followed by its `README.md`, `ROADMAP.md`, ADR/process records, and the
code/tests that own the behavior. Local repository evidence controls local
facts. Foresight routing roles do not silently grant ownership.

## Work and verification

Make the change at the narrowest repository boundary that owns it. Do not
rewrite a child repository's package manager, board workflow, runtime policy, or
quality gates merely to make the constellation uniform.

Run the gate that actually exercises the changed boundary. A nominally green
check that never loads the changed component is not evidence for that
component. Missing tools, credentials, services, or unsupported gates remain
visible as unavailable; they do not become passes.

Root-level inventory and evidence commands are documented in `README.md`.

## Let Foresight learn from successful work

When a local technique works and may be reusable:

1. preserve the exact repository revision and the evidence that exercised it;
2. classify the practice as local behavior, a reusable adapter pattern, or a
   cross-repository semantic candidate;
3. keep recovered evidence distinct from accepted common law;
4. look for independent occurrences when the claim is not inherently
   universal;
5. promote explicitly;
6. encode the accepted reusable part in Foresight as data, portable `.cljc`
   law, a reusable component, or a generated projection.

A child merge proves that a change landed there. It does not by itself prove
that every repository should adopt the same mechanism.

See
[`docs/architecture/self-documentation.md`](docs/architecture/self-documentation.md)
for the Git archaeology behind this loop and the current promotion candidates.
