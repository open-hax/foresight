---
original_name: "2026.08.13.16.46.08.md (chatgpt export, lines 442-709)"
title: "EDN Contracts, MCP, and the Angular Client Decision"
summary: "Argues the Knoxx CMS should make EDN contracts + MCP workflows the product boundary, with any web UI (Helix or the clients Angular team) as a replaceable consumer, and that an AI walking an SME through one translation review is the real acceptance test."
category: "architecture"
created: "2026-08-13"
---

# you asked

Now ok... so I was wanting this CMS to be defined inside of EDN files basicly check out knoxx's latest epic we're doing some of the work now. I am moving away from actually providing a UI at first, and provide everything as these contracts, and MCP servers. Doing it that way is more data oriented any way. When that all works, and I can get an AI to walk me through a SME translation segment review from start to finish, then the model is there again. So maybe the frontend as it is right now isn't that important... thinkin about it too, my client's team are all angular people.

---

# chatgpt response

I’m going to ground this in the current Knoxx epic, because I think the important decision is no longer “Helix vs Hiccup.” It’s whether **EDN contracts + MCP workflows are the product boundary**, with any web UI becoming a replaceable client later.

The repository is already pointing in the direction you’re describing. The older CMS epic still framed `view-contract.edn` as the source of truth with a visual builder on top, while the newer contract-runtime work is explicitly pushing Knoxx toward manifests/resources and separating reusable mechanism from deployment/UI concerns.

## Signal

(己, p=0.99) **Yeah. I think you just found the more important architectural boundary.** The frontend should stop being the architecture and become **one consumer of the architecture**.

The new publication epic is already saying almost exactly that:

```text
EDN/resource graph       → desired semantic state
laws/domain functions    → admissibility + decisions
receipts                 → observed facts
adapters                 → effects

          ↓ expose operations

MCP / HTTP / CLI / UI / agent
```

The epic explicitly makes Knoxx resources the owner of desired state, Knoxx law/domain the owner of decisions, receipts the owner of observed execution, and publication adapters the effect boundary. OpenPlanner becomes merely one projection/adapter. 

(己, p=0.98) Once that's the architecture, **Helix vs Angular becomes substantially less consequential**.

## Evidence

(世, p=1.00) The publication epic's success condition is already headless: with OpenPlanner REST completely unavailable, Knoxx still has to know what documents exist, their target gardens/locales/revisions, publication intent, translation/review requirements, and blockers. 

That's a much stronger contract than "the CMS page works."

And look at what the translation-config task is doing. It deliberately establishes one canonical configuration boundary used by UI **and workers**, with explicit domain shapes and explicit JSON wire contracts:

```text
EDN resources
     ↓
resolved translation config
     ├── MCP/agent
     ├── ingestion worker
     ├── HTTP
     └── browser UI
```

It specifically prohibits the UI or worker from independently deciding model precedence. 

(世, p=0.99) The translation/publication gate is even more telling. It's explicitly designed as **pure domain computation** with no UI, adapter, worker, or OpenPlanner dependency:

```clojure
publication intent
+ translation receipts
+ review receipts
+ source revision facts
--------------------------------
= concrete revision
+ blockers
+ derivable next action
```

The card's tests are intentionally pure, and the CMS is merely supposed to *explain* the blockers that domain calculation produces. 

That's the architecture you want if AI is supposed to drive the workflow.

## Frames

(己, p=0.99) **Frame 1 — The first real CMS UI is the Muse/agent.**

Your acceptance criterion is better than a conventional frontend acceptance criterion:

```text
"Can an AI take an SME through one
translation segment from discovery
to reviewed/accepted evidence?"
```

That forces the underlying model to actually exist.

The interaction could be:

```text
AI: There are 14 translation segments awaiting SME review.
    Segment 7 has two terminology conflicts.

SME: Show me.

AI: Source:
    ...

    Translation:
    ...

    Prior terminology says X should be Y.
    Current translation uses Z.

    [accept] [correct] [defer] [inspect evidence]

SME: Y is correct here.

AI: Records correction/review receipt.
    Segment 7 now satisfies review policy.
    Moving to segment 8.
```

There need not be a bespoke web component anywhere in that loop.

The **MCP tools become your first interaction surface**.

---

(己, p=0.98) **Frame 2 — EDN is the authoring/configuration representation; MCP is the behavioral interface.**

I would resist making the MCP protocol itself the domain model.

Something closer to:

```text
              ┌────────────────────┐
              │   EDN resources    │
              │ contracts / intent │
              └─────────┬──────────┘
                        │
                 laws / projections
                        │
            ┌───────────┴───────────┐
            │                       │
       commands/actions         queries/views
            │                       │
            └───────────┬───────────┘
                        │
                 capability API
                  /           \
                MCP           HTTP
                 │             │
                AI        Angular/Helix/etc.
```

So your useful interfaces become things like:

```clojure
(list-review-work ...)
(get-review-segment ...)
(explain-publication-blockers ...)
(propose-translation-correction ...)
(record-sme-review ...)
(defer-review ...)
(get-next-review-segment ...)
```

Those are much more durable than:

```text
TranslationReviewPage.tsx
TranslationReviewPage.cljs
translation-review.component.ts
```

---

(己, p=0.97) **Frame 3 — Angular being the client's native stack is now an argument *against* spending much effort finishing the Helix application.**

Not because Helix was a mistake.

It gave you a working exploratory surface and helped discover requirements. But there's an option-value asymmetry now:

```text
Spend 3 weeks perfecting Helix UI
    ↓
contracts change
    ↓
client eventually wants Angular
    ↓
rewrite substantial work
```

versus:

```text
Spend 3 weeks perfecting contracts/laws/MCP
    ↓
AI workflow works
    ↓
Angular team builds client
    ↓
same semantic machinery survives
```

The second investment compounds.

And your current epic is explicitly moving in that direction. PR #227 describes the work as making publication intent fully describable from Knoxx resources alone, with resource contracts, pure reconciliation laws, replaceable effects, receipts, and only then a CMS UI cutover. 

## Countermoves

(世, p=0.98) I wouldn't delete the existing frontend or declare UI irrelevant.

It changes roles.

**Before:**

```text
frontend
  ↕
backend implementation
  ↕
data
```

**Now:**

```text
semantic kernel
  ├── contracts
  ├── laws
  ├── actions
  ├── receipts
  └── projections

        ↓

interfaces
  ├── MCP
  ├── HTTP
  ├── CLI
  ├── Helix
  └── Angular
```

The existing frontend becomes a **reference client / test harness / exploratory workbench**.

That is a much less expensive thing to maintain.

(己, p=0.96) And I would probably put the Helix migration epic on a fairly severe diet. The repository currently has an accepted epic whose goal is migrating *all* frontend TS to Helix and retiring Vite. Given this new boundary and an Angular-oriented client team, "zero TS and everything Helix" is no longer obviously a product-level goal. It may still make sense for Knoxx-the-open-source-distribution, but it shouldn't be allowed to compete with getting the translation/review semantic kernel working. 

The really useful proof isn't:

```text
✓ CMS renders
```

It's:

```text
✓ EDN describes the work
✓ contracts reject invalid states
✓ MCP exposes every lawful operation
✓ an agent can discover the current state
✓ an agent can explain why progress is blocked
✓ the SME can make each necessary judgment
✓ every judgment produces durable evidence
✓ the next lawful action follows mechanically
✓ publication becomes admissible
✓ materialization happens through a replaceable adapter
```

Then an Angular application is mostly **ergonomics over a working machine**.

## Next

(己, p=0.99) Make **“an AI walks one SME through one translation segment from pending → reviewed receipt → publication gate satisfied”** the vertical-slice acceptance test, and let that exercise define the contracts and MCP tool surface before doing more frontend architecture.

---

