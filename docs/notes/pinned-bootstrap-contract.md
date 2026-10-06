# Pinned direct-child bootstrap contract

CC1.01 adds a portable decision boundary for CC1.02–CC1.05. Both namespaces are
pure `.cljc`; neither executes Git, traverses children, changes remotes, installs
dependencies, or proves actual cloud access.

## Planning

```clojure
(foresight.bootstrap/plan
  {:bootstrap/root-revision full-root-object-id
   :bootstrap/project foresight.project/project
   :bootstrap/manifest [{:name "rheos" :path "rheos" :url committed-url} ...]
   :bootstrap/gitlinks [{:path "rheos" :mode "160000" :revision pinned-object-id} ...]})
```

The adapter supplies the manifest and direct gitlink records from the named
committed root, rather than the mutable working directory or a branch tip.
Each object ID is a full lowercase SHA-1 or SHA-256; the null object ID and mixed
object formats are rejected. Existing project law supplies declaration,
identity, confinement, and manifest agreement checks. Missing, extra, duplicate,
malformed, or non-gitlink records return `:bootstrap/status :rejected` with
`:bootstrap/errors`; there is no `:bootstrap/children` on a rejected result.
Each error names `:stage :planning` and `:path`.

A successful result has `:bootstrap/status :planned`, the root revision, and
children ordered by path. Each child retains `:source/id`, `:source/path`,
`:source/repository`, `:source/revision`, `:source/actionable?`, and
`:source/consolidation?`. Every declared direct submodule is included, including
inventory-only `.agents`. Its presence confers no execution, package, skill, or
nested repository authority. Root-owned `eta` and `clobber` are excluded.

## Assessment

```clojure
(foresight.bootstrap/assess
  planning-input
  planned
  {:bootstrap/root-revision full-root-object-id
   :bootstrap/checkouts
   [{:source/path "rheos" :source/revision pinned-object-id
     :checkout/initialized? true :checkout/fetch :passed
     :bootstrap/root-revision full-root-object-id} ...]
   :bootstrap/gates
   [{:gate/id :workspace-law :gate/outcome :passed
     :bootstrap/root-revision full-root-object-id} ...]}
  #{:workspace-law :project-law})
```

The first argument is the original project and committed manifest/gitlink facts
used to construct the plan. Assessment reconstructs that plan and requires exact
agreement before checking observations. Dropping a child, changing identity or
repository, or changing inventory-only authority cannot yield readiness.

The fourth argument is a nonempty set of keyword identities from the caller's
required-gate policy. This namespace does not invent or discover that policy.
Every planned child needs one exact HEAD, initialization, successful fetch, and
root-bound observation. Duplicate, absent, or undeclared checkout observations
fail. Every required gate needs one passing current-root observation; missing,
duplicate, skipped, failed, blocked, unavailable, or stale required evidence
fails. Observed optional gates do not become required merely by appearing.

The result carries `:bootstrap/ready?`, `:bootstrap/root-revision`, and
`:bootstrap/errors` with `:stage :assessment` and a path. `ready?` assesses only
the supplied facts. It is neither independent attestation nor permission to
execute an inventory-only source. Later adapters must bind collected facts to
the committed root and preserve the existing evidence/authorization boundaries.

## Verification

```bash
nbb test/bootstrap_test.cljs
nbb test/project_test.cljs
nbb -cp scripts:test test/workspace_test.cljs
clj-kondo --lint src scripts test
```

The bootstrap suite runs without initialized child directories, network,
credentials, or future bootstrap adapters. It is also invoked by the existing
root hosted evidence workflow. Local fixture success does not establish an
executed hydration or cloud acceptance result.
