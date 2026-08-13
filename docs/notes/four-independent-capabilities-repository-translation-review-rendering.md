---
original_name: "2026.08.13.16.46.08.md (chatgpt export, lines 710-1051)"
title: "Four Independent Capabilities: Repository, Translation, Review, Rendering"
summary: "Decomposes the Knoxx CMS epic into four independent capabilities (resource CMS, translation, review/evaluation, rendering) so a client can swap in Optimizely or another vendor behind the same boundaries, and generalizes the translation-review UI into a domain-independent review-case model."
category: "architecture"
created: "2026-08-13"
---

# you asked

you're right about that epic. A lot of things are going on a diet. I let this become a lot of stuff. Slowly, slicing out the well isolated ideas into eta-mu, and other repos. So I think there are like 3 or 4 things that are all independent of each other. 



1. the resource contract file CMS
2. Automatic translation agent
3. SME data review system
4. The server side rendering we were talking about is seperate from the CMS.





With all of these kept seperate with clear boundries and input/output, each set of resource contracts can be set up with any provider of that type of service.



this is important for the CMS part, because our client will use optimizely, not what ever we put together, but I made this open source for a reason, and yea. They may or may not be doing anything with me on this, so I wanted to have something that worked the way I work first, it's also faster to dev with something like that.



I refered to the translation review system generally because the UI I made for it felt like a github PR diff view but it was translations. I thinkt here are a lot of situations where you will be doing some kinda side by side comparison of an agents inputs and outputs for a task to label for learning and training.

---

# chatgpt response

## Signal

(己, p=0.99) I think that decomposition is much cleaner, and **#3 is bigger than translation without becoming coupled to translation**.

I would draw the system as four independent capabilities:

```text
1. Resource CMS
   EDN resources ↔ content repository/provider
                 ↔ Optimizely adapter
                 ↔ filesystem adapter
                 ↔ whatever

2. Translation
   source artifact
       ↓
   translation policy + provider
       ↓
   candidate translation + provenance/receipts

3. Review / Evaluation
   task evidence
       ↓
   human/SME judgment
       ↓
   labels + corrections + decisions + receipts

4. Rendering
   document/view model
       ↓
   renderer
       ↓
   HTML / SSR / static output
```

(己, p=0.99) **None of those four should know which implementation the others use.**

That gives you exactly the property you're describing: Knoxx can ship one useful open-source implementation, while a client can substitute Optimizely, their translation vendor, their own review tooling, Angular, or something else behind the same boundaries.

## Evidence

(世, p=0.98) Your translation-review UI resembling a GitHub PR view is a useful clue about the underlying abstraction.

A GitHub review is fundamentally:

```text
artifact A
artifact B
context
evidence
rules / expectations

        ↓ human judgment

comment
approve
reject
request correction
defer
```

Translation happens to instantiate that as:

```text
source text
translated text
terminology/context
translation policy

        ↓ SME judgment

accept
correct
flag
defer
```

But an agent-training case can instantiate exactly the same machinery:

```text
task input
agent output
tool/context evidence
rubric

        ↓ reviewer judgment

correct
incorrect
partially correct
preferred alternative
correction
labels
```

So I wouldn't make the core object a `TranslationReview`.

I'd make translation **produce a review case**.

Something roughly like:

```clojure
{:review/id :review/123

 :review/subject
 {:kind :agent-task-attempt
  :id :attempt/456}

 :review/artifacts
 [{:artifact/role :input
   :artifact/ref ...}
  {:artifact/role :candidate
   :artifact/ref ...}]

 :review/context [...]
 :review/rubric :translation/sme-v1

 :review/required-judgments
 [:accuracy :terminology :acceptability]}
```

Translation can add its own domain data without poisoning the generic model:

```clojure
{:translation/source-locale :en
 :translation/target-locale :fr
 :translation/terminology-set :client/foo}
```

The generic review system doesn't need to understand those keys.

## Frames

(己, p=0.98) **“Side-by-side comparison” is probably a presentation mode, not the ontology.**

The deeper thing is a **review case composed of artifacts and evidence**.

Sometimes it's naturally two-pane:

```text
source       translation
before       after
input        output
expected     actual
model A      model B
```

Sometimes it may be three-way:

```text
source | machine translation | SME correction
```

or:

```text
prompt | agent output | reference answer
```

or not comparative at all:

```text
agent output
evidence
rubric
↓
label it
```

So I'd avoid encoding:

```clojure
{:left ...
 :right ...}
```

into the domain.

Encode roles:

```clojure
:artifact/role :source
:artifact/role :candidate
:artifact/role :reference
:artifact/role :previous
:artifact/role :evidence
```

Then a PR-diff-like UI can choose how to lay those out.

---

(己, p=0.97) There's also a useful separation inside #3:

```text
Review Case
    │
    ├── evidence/artifacts
    ├── rubric
    └── requested judgments
             │
             ▼
        Review Session
             │
             ▼
          Judgment
       /      |       \
   labels  correction  decision
             │
             ▼
           Receipt
```

That gives you training data **as a consequence of doing real work**, instead of building a labeling application whose entire purpose is manufacturing labels.

That distinction matters.

An SME might say:

> "No, *account* here means the customer's subscription account, not their financial account."

Operationally, that's a correction needed to complete a task.

But downstream you have excellent learning material:

```clojure
{:judgment/type :correction
 :judgment/domain :terminology
 :input ...
 :candidate ...
 :correction ...
 :reason ...
 :reviewer-role :sme}
```

Now the same corpus can support evaluation, few-shot examples, retrieval, fine-tuning preparation, regression tests, or policy refinement.

---

(己, p=0.98) And then #1 becomes pleasantly boring.

The **resource CMS** doesn't have to be your publishing platform.

It's basically:

```text
canonical content/resource contracts

          ↕ provider boundary

filesystem
git
Optimizely
Contentful
database
remote API
```

Your filesystem/EDN implementation is still extremely worthwhile because it gives the open-source project a provider that is:

```text
transparent
git-native
AI-readable
diffable
scriptable
cheap
```

That's not pretending to compete with Optimizely. It's a reference/provider implementation optimized for how you want to work.

## Countermoves

(己, p=0.97) I would watch one naming trap: calling #3 a **“data review system”** may make it sound like its input is datasets.

What you're actually describing is closer to:

**review/evaluation of work products, producing structured judgments.**

Possible stable vocabulary:

```text
Review Case       thing requiring judgment
Artifact          something shown as evidence
Rubric            questions/laws governing judgment
Judgment          atomic human/agent determination
Correction        proposed replacement/improvement
Decision          accept/reject/defer/etc.
Receipt           durable fact that judgment occurred
```

“SME Review” can then be a particular workflow or role.

(己, p=0.94) `adjudication` is another technically good word for #3, especially when multiple judgments conflict, but I wouldn't make that the top-level name unless you want the more formal/legal connotation. **Review** is probably the better ordinary noun; **adjudication** can describe resolving disagreement between reviews.

(己, p=0.98) And yes: **SSR belongs outside the CMS.** The renderer should consume some resolved document/view representation:

```text
CMS provider ─┐
              │
other source ─┼─→ resolved document → renderer → HTML
              │
generated ────┘
```

That lets the same renderer render content retrieved from your EDN CMS, Optimizely, or something generated dynamically. Conversely, the CMS doesn't care whether anyone renders it with React SSR, Angular SSR, static templates, or not at all.

## Next

(己, p=0.99) Extract the ontology for **Review Case → Artifacts/Evidence → Rubric → Judgment/Correction/Decision → Receipt** first; then translation becomes the first concrete producer/consumer proving that #3 is genuinely domain-independent.

---

