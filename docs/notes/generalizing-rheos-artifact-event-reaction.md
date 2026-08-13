---
slug: generalizing-rheos-artifact-event-reaction
uuid: 62587849-db5e-458a-b8ea-8aec644f7273
title: "Generalizing Rheos: Artifact, Event, Reaction"
kind: note
status: draft
description: "Generalizes Rheos from a Kanban-specific tool into a document-reactive engine over an Artifact/Event/Reaction model, grounded in Epiphany's existing document-governance.md (kind, status, relations, authority, epistemic tier) as the de facto spec for it."
created: "2026-08-13"
labels: [rheos, epiphany, document-governance, mu, katamorph, foresight]
sources:
  - "chatgpt export: cljs helix vs hiccup (2026.08.13.16.46.08.md), lines 3988-4425"
---

# you asked

Yes I was thinking about how we could generalize rheos for any markdown with any frontmatter, and structure, to trigger anything,to be triggered by anything. We could manage more of our doc flow. That was the idea with epiphany. Rather, the way I was thinking about designing my methodology for that project

---

# chatgpt response

Yeah — that makes the Epiphany methodology look less like “a research process” and more like an early **general document-reactive system**. I’m going to inspect its process/authority docs and compare them against Rheos’ current assumptions, because I think the abstraction boundary may be: **document + schema + triggers + derived actions**, with Kanban merely one projection.

## Signal

(己, p=0.99) Yes. **That was already latent in Epiphany.** Rheos being “the Kanban thing” is a special case that got useful enough to harden before the larger abstraction became obvious.

The generalized shape is closer to:

```text
Markdown / structured artifact
        ↓
declare what kind of thing this is
        ↓
validate whatever contracts apply
        ↓
discover relations + current state
        ↓
emit facts/events
        ↓
match declarative reactions
        ↓
invoke typed work
        ↓
record what happened
        ↓
derive new views / documents / work
```

And a Kanban card becomes merely:

```text
Markdown artifact
+ :kind :story
+ workflow policy
+ status transition rules
+ board projection
```

rather than the thing Rheos fundamentally *is*.

That aligns almost exactly with Epiphany's current process constitution: it explicitly refuses to prescribe one Kanban tool, state graph, document schema, agent architecture, or workflow, while treating research, designs, decisions, work items, verification, review and acceptance as different accountable artifacts connected through a lifecycle. 

## Evidence

(世, p=1.00) The strongest evidence is actually `document-governance.md`. It says, explicitly, **“documents [are] first-class operational artifacts”**, that a durable artifact should declare its `kind`, that kinds carry different responsibilities, that relations such as `supports`, `informs`, `implements`, `requires`, `reviews`, `verifies`, and `accepts` are semantically different, and that document analysis can parse frontmatter, headings, links, embedded forms, status and relations. 

That is basically the specification for generalized Rheos.

Epiphany even already separates:

```text
kind
status
relations
authority
epistemic tier
```

So:

```yaml
kind: finding
status: provisional
```

does not mean the same thing as:

```yaml
kind: decision
status: accepted
```

even if both are Markdown documents.

And the relation matters too:

```text
finding --supports--> design

research --informs--> decision

decision --requires--> implementation

review --reviews--> implementation

verification --verifies--> claim

acceptance --accepts--> outcome
```

A generic document engine can reason over those without knowing whether the files are part of a software project, a research project, a book, a music corpus, or somebody's operating manual.

(世, p=1.00) Even Epiphany's Kanban policy says the board is **only a coordination view**, not truth or acceptance, and explicitly delegates the board's file format and CLI implementation away from the process semantics. 

So we already wrote the philosophical escape hatch from “Rheos = Kanban.”

We just haven't followed it all the way through the implementation yet.

## Frames

(己, p=0.99) I'd generalize Rheos around an **Artifact → Event → Reaction** model.

Something like:

```clojure
{:artifact/id ...
 :artifact/kind :research/finding
 :artifact/form :markdown

 :artifact/data
 {:status :provisional
  :confidence 0.84
  ...}

 :artifact/relations
 [{:relation/type :supports
   :relation/target ...}]}
```

The Markdown adapter can derive this from arbitrary frontmatter and body structure.

Crucially, **“arbitrary frontmatter” should mean preserved and queryable, not untyped mush**.

Alpha/Katamorph can say:

```text
I don't need to understand every key.

I do know that this artifact satisfies:
  Artifact
  GovernedDocument
  Finding
  HasEvidenceRelations
```

That's structural polymorphism again.

A custom research project might add:

```yaml
corpus: foo
method: comparative
confidence: 0.73
```

A music document:

```yaml
work: gates-of-aker
render: v17
review-state: salvage
```

A card:

```yaml
status: ready
priority: P1
dependency: [...]
```

Rheos need not centrally own all those schemas.

It asks Katamorph:

```text
What facets does this artifact satisfy?
```

---

(己, p=0.99) Then the really interesting piece is **reactions**.

Instead of hardcoding:

```text
status ready -> maybe show in Todo
```

we can have declarative rules:

```clojure
{:reaction/id :research/ready-for-synthesis

 :reaction/on
 {:event/type :artifact/changed
  :artifact/kind :finding}

 :reaction/when
 [:and
  [:eq [:artifact :status] :accepted]
  [:exists [:relation :supports]]]

 :reaction/do
 {:operation :research/update-synthesis}}
```

Or:

```clojure
{:reaction/id :design/needs-decision

 :reaction/on
 {:artifact/kind :design
  :event/type :artifact/changed}

 :reaction/when
 [:eq [:artifact :status] :review]

 :reaction/do
 {:workflow :architecture/request-decision}}
```

Or something completely outside documents:

```clojure
{:reaction/on
 {:event/type :github/pr-merged}

 :reaction/when
 [:related-to :artifact/implementation]

 :reaction/do
 {:operation :verification/open-case}}
```

So **anything can trigger Rheos** because everything entering it first becomes a normalized fact/event.

And Rheos can trigger **anything registered as a typed operation/workflow**.

That connects directly to the workflow language we were just developing.

```text
Rheos notices
     ↓
Mu composes work
     ↓
Alpha validates boundaries
     ↓
Eta may transform things
     ↓
Mu may ask for judgment
     ↓
Π represents outcomes
     ↓
Clio remembers facts
     ↓
Rheos notices...
```

Now we're getting a feedback system instead of a board tool.

---

(己, p=0.98) And this is where your Epiphany methodology gets especially interesting.

Its lifecycle currently reads roughly:

```text
request
 → orientation
 → inquiry/research
 → proposal/design
 → decision
 → plan/spec
 → bounded work
 → implementation
 → verification/review
 → accepted outcome
 → reflection/process improvement
```

But the charter explicitly says those aren't mandatory linear stages. 

So represent them as a **document graph**, not a pipeline:

```text
                 ┌──────── SourceRecord
                 │
ResearchBrief ───┼──────── Finding
                 │              │
                 └──────────────┼──── supports
                                ▼
                             Design
                            /      \
                     informs        requires
                         /             \
                    Decision          Research
                       │
                  implements
                       ▼
                    Stories
                       │
                    produces
                       ▼
                 Implementation
                    /        \
               verifies      reviews
                  /             \
        VerificationRecord   ReviewRecord
                  \             /
                    \         /
                     Acceptance
                         │
                      informs
                         ▼
                     Reflection
```

**That graph itself can run.**

Not by pretending documents are programs, but because their state and relations can make work eligible.

That's the idea I think you were reaching toward in Epiphany.

## Countermoves

(己, p=0.99) There are two traps I'd avoid while generalizing Rheos.

First: **don't put executable code in Markdown/frontmatter**.

Bad:

```yaml
on-change: "bash ./deploy-production.sh"
```

Good:

```yaml
reactions:
  - on: artifact.accepted
    invoke: publication/reconcile
```

`publication/reconcile` is a registered operation with a contract, capabilities, permissions and provider.

The document declares intent. It doesn't smuggle arbitrary authority into prose.

Second: don't require every document to adopt one giant universal frontmatter schema.

That would just recreate Knoxx's problem at a different level.

Instead:

```text
Base Artifact
    +
optional facets
    +
declared kind
    +
project-local extension fields
```

Katamorph validates whatever contracts are actually asserted.

So a future package might say:

```clojure
(satisfies? artifact :epiphany/finding)
(satisfies? artifact :mu/evaluable)
(satisfies? artifact :clio/event-source)
```

all independently.

A single Markdown file can participate in multiple systems without becoming a “god document.”

And I would preserve Epiphany's existing rule that **structural automation cannot silently make semantic judgments**. Its document policy already says the checker can decide malformed frontmatter, missing fields, broken relations, etc., but unsupported conclusions and bad architecture remain review findings. 

That's Alpha/Mu separation hiding in plain sight:

```text
Alpha:
    "This finding has a valid shape,
     resolvable sources,
     and a declared confidence."

Mu:
    "The sources do not actually justify the finding."
```

Beautiful boundary.

## Next

(己, p=0.99) I think this actually gives us a better first extraction target than inventing Alpha from scratch.

**Generalize the smallest possible Rheos artifact model.**

Not statuses. Not the board.

Start with:

```clojure
Artifact
ArtifactKind
ArtifactRef
Relation
Event
Reaction
```

and preserve arbitrary extension data.

Then prove three radically different Markdown files can pass through the same pure `.cljc` kernel:

```text
Epiphany Finding
Calliope Render Review
Rheos Story
```

Each gets parsed into an Artifact.

Each retains arbitrary frontmatter.

Each satisfies different Katamorph facets.

Each emits the same generic `artifact/observed` / `artifact/changed` facts.

And each can have a completely different declarative reaction.

If that works, **Rheos stops being a Kanban implementation and becomes the flow engine its name always implied.**

Then Epiphany can finally become what its methodology was pointing toward: **a body of documents whose relationships, evidence, maturity and changes actively organize the next responsible work instead of merely documenting it afterward.**

---

