# Oikos: the desktop remembers; the operator keeps the room

2026-11-03. Planning program and effects-disabled bootstrap, not installation.
License: GPL-3.0-or-later.

## The dream recovered

The July workbench designs already name the desired shape: one keyboard-driven
surface for windows, buffers, files, tabs, tasks and agents; task-centric resume;
named tools and review queues; modest reversible layout changes; isolated tests.
This turn connects that work to local perception and memory, Clio projections,
governed cloud disclosure and training-ready feedback.

The invariant is **shared context without stolen attention**. Agents can prepare
other named views, the operator can deliberately join them, and neither logical
membership nor a generated suggestion silently moves a physical window.

Recovered sources:

- `/home/err/docs/notes/design/epics-and-tasks.md` (2026-07-10), especially
  workbench themes, `go`, task-centric resume and Emacs MCP/review tools.
- Sibling `epic-workspace-go.md`, `epic-task-centric.md`, `epic-emacs-mcp.md`,
  `epic-i3-mcp.md`, `epic-layout-hygiene.md`, `epic-agent-sandbox.md`.
- `/home/err/.spacemacs.d/docs/notes/agent-i3-sandbox/02-spacemacs-target-layer-architecture.md`:
  separate core/Spacemacs/dev targets, rather than changing the live daemon to test.
- Existing [workstation distribution](../agile/kanban/federated-workstation-distribution.md)
  and [bounded Colab research](../agile/kanban/qualify-the-bounded-colab-gemma-retrieval-research-pilot-and-retain-production-holdback.md)
  remain linked programs; no budget or adoption approval is inherited.

Older note IDs are design identifiers, not presumed current Rheos UUIDs.

## New independent product

[`open-hax/oikos`](https://github.com/open-hax/oikos) is private by operator choice;
`foresight/oikos` is its development submodule. Public Foresight consumers need
explicit access or must initialize an allowed subset. Source privacy does not
make raw desktop data safe to commit: runtime state/captures stay outside Git.

NBB is primary, with portable `.cljc` tested in both NBB and BB. Node/npm and
Babashka facilities meet at explicit adapters/process protocols, not an assumption
that every BB/JVM package imports into NBB. The bootstrap has a manifest and
dry-run proposal preconditions; no desktop executor, event-store replacement,
installed daemon or live tool publication.

Proposed installed generations are `~/.local/share/oikos/releases/<sha>` plus an
explicit selected generation. They are not links into development. Installed/dev
config, state, sockets and ports remain separate. Services/shx own promotion;
Muse compiles client exposure. shx shell IR is not assumed to parse arbitrary
Emacs/i3/systemd files without owning target adapters.

The full design and measured smoke live in the private child:
[`oikos/docs/architecture/workbench.md`](../../oikos/docs/architecture/workbench.md)
and [`oikos/docs/research/perception-smoke-2026-11-03.md`](../../oikos/docs/research/perception-smoke-2026-11-03.md).
These links require the private submodule checkout.

## Epic map

Created through Rheos in the board's native `epic`/`task` vocabulary: eight epics
and sixteen parent-linked child tasks. All begin incoming. Creation is not
planning-review approval, ready admission, implementation completion or deployment.

| Epic | Outcome | UUID |
| --- | --- | --- |
| [Isolated generations and shx/Muse configuration](../agile/kanban/oikos-isolated-generations-and-shx-muse-configuration-boundaries-6f7533de.md) | Dev cannot accidentally become installed; preview/check/apply/rollback | `722906b7-77d7-4414-a976-23996f7533de` |
| [Clio desktop journal and causal EDN projections](../agile/kanban/oikos-clio-desktop-journal-and-causal-edn-projections-e4415de0.md) | Honest snapshot/event reconciliation, identity, replay and freshness | `3b9279b5-aafd-431a-90a9-128ce4415de0` |
| [Named collaborative projection spaces](../agile/kanban/oikos-named-collaborative-projection-spaces-with-deliberate-joining-2eb908b4.md) | Speculative views, protected human workspace, deliberate joining | `ff5998a4-2cd6-482c-a43e-94522eb908b4` |
| [Shared Muse i3 and existing-Emacs capabilities](../agile/kanban/oikos-shared-muse-i3-and-existing-emacs-capabilities-across-clients-e0a3251a.md) | One typed surface for OpenCode, Claude, ChatGPT, gptel and Knoxx | `456b2c69-cb5d-417d-9aeb-ac05e0a3251a` |
| [Continuous local perception and event-backed knowledge](../agile/kanban/oikos-continuous-local-perception-and-event-backed-knowledge-84a8adaa.md) | Local memory with resource/retention/pause controls, not imaginary online weight updates | `92b529b9-256d-4654-8aa6-5f1884a8adaa` |
| [GovOne egress admission and Knoxx Lens evidence](../agile/kanban/oikos-govone-egress-admission-and-knoxx-lens-evidence-6244a5be.md) | Govern text, image, labels, tool returns and exports before any cloud path | `e616187d-ba13-42b1-addd-aa4d6244a5be` |
| [Empirical local Gemma and OCR qualification](../agile/kanban/oikos-empirical-local-gemma-and-ocr-qualification-66198864.md) | E4B measured against NPU OCR; separate 12B fit/backend/speed and governed 31B comparison | `e842e6d9-dd41-4ca2-b229-dc2466198864` |
| [Consented labels and bounded Colab training corpus](../agile/kanban/oikos-consented-labels-and-bounded-colab-training-corpus-9faa6482.md) | Correctable labels, leakage-safe partitions and approved cloud exports | `3de19b00-adae-43e6-a6c4-da339faa6482` |

## Boundaries the plan preserves

- Clio owns canonical event admission/persistence/replay. i3 events alone are not
  complete tree deltas; reconnect/snapshot races must remain visible.
- Native i3 has one physical position per window. Many logical views are data,
  not duplicate live windows. Hidden workspaces do not guarantee noninterference.
- Existing Emacs server is the integration target; remote arbitrary eval is not
  a capability. Named actions, bounded scopes and preview/consent are required.
- Muse owns compatibility compilation, not runtime/governance authority. ChatGPT
  needs supported authenticated remote transport; a loopback socket is not enough.
- The intended GovOne is the `mojomast/govone` gateway, not the homonymous project.
  Its existing session/policy path owns egress; denial never triggers direct retry.
- Model-produced text, labels and commands are untrusted proposals. Local
  availability or reportedly free 31B service does not waive admission.

## What was measured, not inferred

The NPU OCR endpoint returned boxes and text for three synthetic 960×600 images:
nine geometrically valid line boxes, **3/9 exact normalized lines**, reported
inference **130–286 ms**. This is a development smoke, not held-out OCR quality,
IoU qualification or a meaningful p95. No real desktop captures were used.

The expected standalone llama.cpp E4B endpoint on `:8082` was unavailable. A
discovery worker's earlier tiny schema-constrained text success on a transient
host runner does not establish the requested container deployment or any image
quality. The endpoint had disappeared by the main experiment. E4B vision and
proposal quality remain unmeasured; no model fallback was used.

Google's current [Gemma 4 model card](https://ai.google.dev/gemma/docs/core/model_card_4)
does list 12B Unified. Fit, co-residency, NPU compilation and useful speed are
distinct unproven tests on this laptop. No 12B download/load, cloud inference,
Colab allocation, production restart or installed config migration occurred.

## Next implementation slice

After planning review, qualify isolated configuration and a stable local E4B
endpoint; then capture/replay a synthetic i3 observation through Clio and expose
one bounded read-only projection through Muse into the existing Emacs review
surface. It proves the composition without rearranging the live workspace.
