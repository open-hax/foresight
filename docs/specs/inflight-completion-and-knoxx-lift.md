---
title: "Inflight completion and Knoxx lift program"
summary: "Terminal-state rules for the Foresight portfolio, a revision-bound evidence workflow, and separate translation/review and content/layout/publishing/review extraction tracks."
category: "program-spec"
status: "active"
created: "2026-08-29"
---

# Inflight completion and Knoxx lift program

## Outcome

Finish the current Foresight constellation portfolio before beginning a new
extraction wave. Every open pull request and active card must end in one of five
explicit states:

1. **Merged** — the current revision satisfies its owned repository gates and
   every substantive review finding is addressed.
2. **Superseded** — a newer merged change or accepted direction makes the branch
   obsolete; the closing record names the replacement evidence.
3. **Split** — useful work remains, but it is safer or clearer as one or more
   bounded issues/cards; the original branch closes after the replacements exist.
4. **Blocked** — completion requires an external fact, credential, host, or
   decision that is not present; the blocker names an owner and a verification
   condition.
5. **Rejected** — evidence shows the proposed behavior should not enter the
   product; the reason is durable and does not masquerade as inactivity.

"Open but unattended" and "green because a relevant suite skipped" are not
terminal states.

## Architectural boundary

The next Knoxx extraction wave contains two independent product systems.

### Translation generation and SME review

```text
source artifact + locale + terminology + translation policy
  -> translation operation/provider
  -> candidate artifact + provenance
  -> translation review case
  -> SME judgment/correction/decision
  -> revision-bound review receipt
```

This track proves typed transduction followed by domain-specific evaluation.
It does not own layout, a CMS provider, publication materialization, or a web
page. Its first end-to-end acceptance path is an agent walking one SME through
one segment from pending candidate to durable accepted or corrected receipt.

### Content generation, layout, publishing, and product review

```text
brief/source artifacts + content policy
  -> content generation
  -> content artifact
  -> layout/representation operation
  -> publication intent
  -> replaceable publishing provider
  -> observed publication receipt
  -> content/product review case and decision
```

This track owns a content product's organization and representations, plus the
desired-versus-observed publication loop. Translation may supply an artifact to
it, but translation is neither a required stage nor its review ontology.

### Shared seams, not shared pipelines

The tracks may share only contracts that remain true independently:

- immutable artifact identity and revision references;
- operation `requires`/`provides` shapes;
- provider-neutral request/result envelopes;
- review-case, evidence, rubric, judgment, correction, decision, and receipt
  concepts after they survive both domains;
- desired intent versus observed effect receipts;
- lawful workflow composition over compatible shapes.

Side-by-side presentation, GitHub-style diffs, Angular, Helix, SSR, MCP, HTTP,
filesystem storage, Optimizely, and model providers remain adapters or clients.
They do not define the portable domain model.

## Ownership and lift order

| Concern | Initial owner | Extraction rule |
| --- | --- | --- |
| Portable identities, laws, admissibility, graph and ledger semantics | Foresight native `.cljc` components | Recover evidence, purify decisions, then accept a lift explicitly. |
| Reusable shapes and operation contracts | Katamorph | Validate on JVM and CLJS; keep host objects at adapters. |
| Workflow execution and Kanban/Rheos transitions | eta-mu | Consume contracts; do not become product-domain authority. |
| Reference application, MCP/HTTP surfaces, and migration source | Knoxx | Keep working adapters while pure decisions move outward. |
| Review host/profile and agent interaction | Muse or the applicable host | Emit typed evidence; do not publish unconstrained model prose as authority. |
| Rendering components and representation adapters | UXX and product-specific modules | Consume resolved view/content data; do not own repository or publication truth. |
| Deployment topology and live-host probes | Services | GPL service surface; never own application source or secrets. |

The lift sequence is always: observe current behavior, characterize it with
tests, split pure decisions from effects, port the portable shape/law, run both
host suites, switch one consumer, and only then retire duplicated logic.

## Evidence workflow

Evidence is bound to the exact revision under decision. A rerun on a different
head is new evidence, not an update to the old result.

For locally spawned gates, the runner captures one raw catalog snapshot and
retains its SHA-256 identity, the exact argument vector, and the gate source
path plus repository revision in every result. The selected checkout must be
initialized, clean, error-free, and at the captured HEAD immediately before
and after the spawn. Any post-spawn movement, dirty state, or unverifiable Git
state rejects the attempted result and cannot emit a revision-bound pass.

Catalog validation rejects keys that are not actionable direct submodules in
the portable project inventory. Root execution never encodes a child's nested
package layout: a child without an owning root command remains workflow-only or
external. Promotion receives the trusted catalog snapshot and its identity and
requires every result's repository, execution mode, source, command, and
revision to match the selected gate exactly.

### Gate kinds

| Gate | What it proves | Minimum evidence |
| --- | --- | --- |
| Static | Source and configuration satisfy mechanical constraints. | Exact command, revision, tool version or immutable workflow reference, and result. |
| Unit/law | Pure decisions and invariants behave over valid and invalid cases. | Deterministic tests including negative and boundary cases. |
| Integration | Two or more real internal boundaries agree. | Real serialization, filesystem, database, process, protocol, or runtime boundary; mocks may isolate unrelated externals only. |
| E2E/vertical | A user-meaningful path works through its actual adapters. | Named entry point, exercised boundaries, expected observable outcome, and retained failure artifacts. |
| Coverage | The tested surface is measured rather than inferred from test count. | Machine-readable report, repository-owned threshold/baseline, and no unexplained regression. |
| Security/destructive-path | Fail-closed behavior protects credentials, authorization, and durable writes. | Denial, malformed-input, duplicate-target, rollback/idempotency, and least-privilege cases as applicable. |
| Live smoke | The deployed or service-dependent path works in its real environment. | Host/environment identity, revision, probe, status/output, and sanitized logs. |
| Independent review | Findings were classified and their disposition matches the diff. | Revision-bound review artifact; substantive unresolved findings block promotion. |

An E2E label is valid only when the test crosses the integration seam named in
its description. A component test with mocked network calls remains an
integration test even when it uses a browser-like DOM.

### Required tiers by change class

| Change class | Required before merge |
| --- | --- |
| Documentation/board only | Static validation, board drift/FSM check when relevant, independent review. |
| Pure shape/law | Static, JVM and CLJS unit/law tests when portable, coverage, independent review. |
| Runtime adapter | Static, unit/law, integration across the changed boundary, coverage, independent review. |
| User workflow | Runtime-adapter tier plus one vertical E2E path and retained failure artifacts. |
| Security, credentials, or durable mutation | User-workflow tier plus negative/fail-closed tests and idempotency or rollback evidence. |
| Deployment/topology | Applicable code tiers plus configuration validation and a post-deploy live smoke on the target environment. |

Repository-specific gates are version-controlled by the owning repository.
Foresight may aggregate them but must not rename a cheap suite "E2E," invent a
command, or convert `blocked`, `unavailable`, or `skipped` into `passed`.

### Coverage policy

- Every executable-logic change must produce coverage for the affected testable
  package or record an explicit, reviewed not-applicable reason.
- Each repository owns version-controlled minimums and baseline rules. Lowering
  either requires a documented decision tied to the revision.
- Pure laws require exhaustive decision-table examples for known branches,
  including invalid-state behavior; a high percentage cannot substitute for
  missing law cases.
- Security and durable-write code uses mutation or fault-injection evidence
  where practical, with an explicit follow-up when the tool is unavailable.

## Program phases

### Phase 1 — close the current portfolio

- Refresh every PR head, base, checks, and review threads.
- Merge bottom-up where dependency evidence is explicit.
- Repair substantive defects in bounded branches.
- Create replacement issues/cards before closing split or superseded work.
- Record external blockers with owner, missing fact, and unblock proof.

### Phase 2 — reconcile durable state

- Update the root portfolio note from the final GitHub state.
- Reconcile cards through the Rheos FSM and append canonical events.
- Append Receipt River decisions, test runs, and push truth.
- Preserve one coherent completion PR rather than editing submodule pointers
  opportunistically.

### Phase 3 — establish extraction foundations

- Inventory Knoxx translation and content/publication code by pure law, adapter,
  UI/client, storage, deployment, and historical compatibility role.
- Prove the two vertical slices independently in Knoxx before lifting them.
- Promote only cooled, provider-neutral shapes/laws into Foresight/Katamorph.
- Keep the generic Mu review model at candidate status until both domains prove
  which concepts are actually shared.

## Acceptance

- Every PR from the opening portfolio has a current terminal disposition or a
  bounded, owned external blocker.
- Root board state matches GitHub state and has no unexplained drift.
- Translation and content-product extraction live on separate cards with
  separate vertical-slice acceptance tests.
- A machine-readable gate catalog distinguishes unit, integration, E2E,
  coverage, static, live, blocked, unavailable, and not-applicable outcomes.
- At least one non-Node repository gate is executed through the root aggregator,
  and failure/unavailable probes demonstrate nonzero outcomes.
- Receipt River contains opening observation, decisions, verification, and push
  truth; Session Mycology records the reusable process learning.

## Licensing

Portable libraries extracted from Knoxx are licensed LGPL-3.0-or-later.
Deployable services and applications are licensed GPL-3.0-or-later. Provider
adapters must retain a process/API boundary when proprietary client logic cannot
be distributed under the applicable open-source license.
