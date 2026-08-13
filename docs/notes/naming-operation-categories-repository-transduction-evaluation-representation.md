---
original_name: "2026.08.13.16.46.08.md (chatgpt export, lines 1052-1615)"
title: "Naming Operation Categories: Repository, Transduction, Evaluation, Representation"
summary: "Renames CMS/translation/review/SSR to the ML/architecture-neutral terms Repository, Transduction, Evaluation, and Representation, and proposes a five-dimension (operation, representation, semantics, modality, behavior) DSL for typed workflow steps."
category: "design"
created: "2026-08-13"
---

# you asked

you know, let's think of the translation system generally too. Not just one language to another, one that comes to mind immediately is text to voice or voice to text. There is a word for this class of transformations in ML... and I feel like the other two things for the CMS also have more general words.

---

# chatgpt response

(己, p=0.97) I think the word you’re reaching for is probably **transduction**. I’m checking the terminology because there are two nearby abstractions—*sequence transduction* and *cross-modal translation*—and they imply slightly different boundaries for the system.

## Signal

(世, p=1.00) **The ML word is _transduction_.** Graves' original RNN-Transducer paper explicitly describes speech recognition, machine translation, and text-to-speech as examples of transforming—or *transducing*—input sequences into output sequences. ([arXiv](https://arxiv.org/abs/1211.3711?utm_source=chatgpt.com))
(己, p=0.98) And I think that unlocks better names for the whole decomposition:

```text
1. Repository
2. Transduction
3. Evaluation
4. Projection
```

with more concrete implementations underneath:

```text
Repository   → EDN/files, Git, Optimizely, Contentful, ...
Transduction → translation, ASR, TTS, OCR, summarization, ...
Evaluation   → SME review, labeling, grading, comparison, adjudication, ...
Projection   → HTML/SSR, static HTML, PDF, feeds, presentation models, ...
```

That feels considerably more stable than `CMS / translation / review UI / SSR`.

## Evidence

(世, p=1.00) **Transduction** is unusually good here because it does *not* imply that the source and target are both natural language text. Sequence-transduction literature explicitly includes speech recognition, machine translation, and text-to-speech under the same abstraction. ([arXiv](https://arxiv.org/abs/1211.3711?utm_source=chatgpt.com))
So:

```text
English text → French text       machine translation
speech       → text              transcription / ASR
text         → speech            synthesis / TTS
speech       → French speech     speech translation
image        → text              captioning / OCR-ish cases
text         → image             generation
```

can all have the same outer contract:

```clojure
{:transduction/input  ...
 :transduction/output ...
 :transduction/kind   ...
 :transduction/provider ...
 :transduction/provenance ...}
```

(世, p=0.96) `cross-modal translation` is also used in multimodal research—for example, work treating speech, image, and text as different modalities that can be translated between one another—but **transduction is the broader word** and doesn't require crossing modalities. ([arXiv](https://arxiv.org/abs/2402.16021?utm_source=chatgpt.com))
## Frames

(己, p=0.98) **#1 — “CMS” → Repository**

Your EDN thing isn't fundamentally a CMS.

It has:

```text
resources
identity
schemas/contracts
references
versioning
read/write
enumeration
provider-specific persistence
```

That's a **repository**.

Maybe explicitly:

```text
Resource Repository
```

or:

```text
Content Repository
```

I'd favor **Resource Repository** given where Knoxx has gone. "Content" becomes only one family of resource.

Then:

```text
ResourceRepository
    ├── FileRepository       ; EDN
    ├── GitRepository
    ├── OptimizelyRepository
    └── ...
```

The thing you've been calling your CMS is then an *opinionated resource-repository provider*, perhaps with content-specific contracts layered on it.

---

(己, p=0.99) **#2 — “Translation” → Transduction**

This one clicks hard:

```text
              Transduction
             /     |       \
            /      |        \
 language       modality    representation
    ↓              ↓             ↓
en → fr       audio → text   raw → summary
             text → audio    image → description
```

A provider advertises what transformations it supports:

```clojure
{:transducer/id :openai/foo
 :transducer/accepts #{:text}
 :transducer/emits #{:audio}
 :transducer/capabilities #{:text-to-speech}}
```

Or more generally, source/target shapes rather than a giant enum.

That also separates the **operation** (`transduction`) from the thing doing it (`transducer`).

---

(己, p=0.98) **#3 — “SME data review” → Evaluation**

I like **evaluation** more now than `review`, because the generic operation is:

```text
artifact(s)
+ evidence
+ rubric
       ↓
   evaluation
       ↓
judgment(s)
```

Review is one interaction pattern for performing an evaluation.

Labeling is one output.

Adjudication is what happens when evaluations conflict.

So:

```text
Evaluation
 ├── SME review
 ├── translation QA
 ├── agent-output grading
 ├── preference comparison
 ├── factuality checking
 └── safety labeling

Evaluation Result
 ├── judgment
 ├── labels
 ├── correction
 ├── rationale
 └── receipt
```

That's a clean subsystem.

---

(己, p=0.97) **#4 — “SSR” → Projection**

`rendering` is perfectly correct, but I think **projection** may fit your architecture better.

You're taking some semantic/domain thing:

```clojure
{:document/title ...
 :document/blocks ...}
```

and projecting it into some representation:

```text
document → HTML
document → PDF
document → RSS
document → plain text
document → search document
document → UI view model
```

HTML rendering becomes:

```text
Projection
    ↓
HTML Renderer
    ↓
SSR / static generation / whatever
```

That matters because **SSR isn't really the capability**. It's an execution strategy for an HTML projection.

## Countermoves

(己, p=0.97) There is one seductive over-generalization to avoid: technically, **projection and evaluation could both be called transduction** because they're functions from inputs to outputs.

Don't do that at the architecture level.

The useful distinctions are semantic:

```text
Repository
"What facts/resources are declared?"

Transduction
"Produce another artifact while transforming representation/content."

Evaluation
"Make a judgment about artifacts."

Projection
"Produce a consumer-facing representation of semantic state."
```

Those boundaries imply very different laws.

For example:

```text
transduction may be nondeterministic
evaluation may require a human
projection should usually be reproducible
repository operations concern identity/versioning
```

That's much more useful than reducing everything to `input → output`.

(己, p=0.96) I'm also not fully sold on `Projection` as the public-facing name. **Renderer** is clearer to ordinary developers; **projection** is the better architectural concept.

You could use both:

```text
Projection              ; domain concept
IProjectionProvider     ; protocol
HtmlRenderer            ; implementation
ReactSsrRenderer        ; provider
```

Likewise:

```text
Repository              ; concept
IResourceStore          ; protocol
EdnRepository           ; provider
OptimizelyRepository    ; provider
```

## Next

(己, p=0.99) Try the four nouns as the top-level ontology for a bit: **Repository → Transduction → Evaluation → Projection**. Then test every existing Knoxx CMS/translation/review/rendering responsibility against exactly one of those boxes; anything landing in two boxes exposes the next boundary to cut.

---

# you asked

Then the idea is that the DSL can talk about all of these, long as type the output of each stage matches or shares enough characteristics with the input of the next stage. I think you are right about the Projection thing. that could get mixed up talking about the graph memory, with out winning us anything useful. You are talking about a renderer, but it has the same problem as projection, in that it is an overuserd word so it's not very specific. I think we'd be good to think of several different categories that any one of these steps could belong to. I think maybe "Representation" makes sense as a general term, then you would have like `ReactSsRepresentationRenderer` but you aren't nailing this thing to just be a visual renderer like people are gonna think when they hear that word.

---

# chatgpt response

## Signal

(己, p=0.99) Yes — I think **“Representation” works better as an axis than as the name of the operation**. The useful move is to stop trying to find one noun that classifies every stage and instead let every DSL node carry several orthogonal classifications.

```clojure
{:step/id ...
 :step/kind        ...
 :step/input       ...
 :step/output      ...
 :step/traits      #{...}
 :step/provider    ...}
```

(己, p=0.99) Then the composition law is basically:

```text
producer output satisfies consumer input
               OR
a registered transformation can bridge them
```

That gives you a typed dataflow DSL rather than a CMS workflow DSL.

## Evidence

(己, p=0.98) I think there are at least **five useful dimensions** that shouldn't be collapsed together:

| Dimension | Examples |
|---|---|
| **operation** | store, retrieve, transduce, evaluate, represent |
| **representation** | text, audio, HTML, EDN, React tree, image |
| **semantics** | document, translation, review case, publication |
| **modality** | textual, auditory, visual, structured-data |
| **behavior** | deterministic, generative, human-gated, lossy, reversible |

So the same thing might be:

```clojure
{:step/kind :transduction
 :input/representation :audio
 :output/representation :text
 :traits #{:generative :lossy}
 :semantics :transcription}
```

while SSR might be:

```clojure
{:step/kind :representation
 :input/semantics :document
 :output/representation :html
 :traits #{:deterministic}}
```

(己, p=0.97) That avoids making `renderer` carry too much meaning. A React SSR implementation can merely be a provider for the representation operation:

```clojure
{:provider/id :react-ssr
 :provider/implements :representation
 :provider/accepts :document-view
 :provider/emits :html}
```

Calling the implementation something like `ReactSSRRepresentationProvider` or `ReactSSRRepresenter` is much less misleading than making “renderer” the ontology itself.

## Frames

(己, p=0.99) I think the deeper abstraction is:

```text
                         Step
                          │
        ┌─────────────────┼──────────────────┐
        │                 │                  │
   operation          input shape       output shape
        │                 │                  │
        ▼                 ▼                  ▼
 transduction       representation      representation
 evaluation         semantics           semantics
 persistence        modality            modality
 representation     facets              facets
```

And then **Representation** means:

> the concrete form in which some semantic artifact is expressed.

Examples:

```text
same semantic document
    ├── EDN resource
    ├── HTML
    ├── Markdown
    ├── PDF
    ├── React element tree
    ├── Angular component model
    └── speech
```

That is a genuinely useful concept.

(己, p=0.98) In fact, `text → speech` demonstrates why **semantics and representation must be separate**.

You might have:

```text
semantic content:    document
representation:      English text
```

and after TTS:

```text
semantic content:    document
representation:      English audio
```

The semantic artifact may substantially survive while its representation changes.

Machine translation is different:

```text
English textual document
        ↓
French textual document
```

Same broad representation (`text`), but semantic content has undergone a meaning-preserving-but-imperfect transformation.

So those really are different operations even though both are often casually called “translation.”

---

(己, p=0.99) This also suggests a nice taxonomy for operation classes:

```text
Repository / Persistence
    retains artifacts across time

Transduction
    derives one semantic artifact from another

Evaluation
    derives judgments about artifact(s)

Representation
    expresses an artifact in some representational form

Selection / Query
    derives a subset or view without altering the artifact

Aggregation
    combines several artifacts into one derived artifact

Decomposition
    splits one artifact into addressable parts

Orchestration
    chooses/invokes other steps
```

Those can compose arbitrarily.

A real pipeline might be:

```text
Repository
   │ retrieve source document
   ▼
Decomposition
   │ translation segments
   ▼
Transduction
   │ candidate French segments
   ▼
Evaluation
   │ SME judgments/corrections
   ▼
Aggregation
   │ accepted French document
   ▼
Representation
   │ HTML
   ▼
Repository
     publish/store
```

And another:

```text
Repository → Transduction(TTS) → Repository
```

No CMS assumptions anywhere.

## Countermoves

(己, p=0.99) The part I'd make very strict is your phrase **“shares enough characteristics.”** That's exactly right conceptually, but it needs a formal meaning or the DSL eventually turns into runtime guesswork.

I would make compatibility structural:

```text
output satisfies all required input facets
```

For example:

```clojure
;; producer
{:artifact/type :audio
 :audio/encoding :pcm
 :audio/sample-rate 48000
 :language/id :en}

;; consumer requires
{:artifact/type :audio
 :audio/sample-rate int?
 :language/id keyword?}
```

That connects.

But:

```clojure
{:artifact/type :text}
```

doesn't connect to an audio consumer merely because both contain language.

A transducer must bridge it.

(己, p=0.98) With Malli-ish schemas, the composition rule becomes almost type-theoretic:

```text
OutputSchema <: InputSchema
```

or:

```text
OutputSchema
    ↓ registered adapter/transducer
InputSchema
```

And facets give you structural polymorphism:

```clojure
[:and
 [:map [:artifact/id keyword?]]
 [:map [:content/language keyword?]]
 [:map [:content/body any?]]]
```

A consumer doesn't need to know whether that came from Optimizely, Git, EDN, ASR, or another agent. It cares only about the facets it requires.

(己, p=0.97) I also wouldn't create names like `ReactSSRRepresentationRenderer` unless that class actually needs to exist. The ontology can remain short:

```text
operation:      Representation
provider:       React SSR
input:          DocumentView
output:         HTML
```

Names stay local and boring:

```clojure
:representation/react-ssr
```

rather than encoding the entire taxonomy into every identifier.

## Next

(己, p=0.99) Define a **Step contract** with four things first: `operation`, `requires`, `provides`, and `traits`. Then model the current CMS → translation → SME review → SSR flow entirely as composable steps and see whether any edge requires knowledge that isn't expressed in those contracts. That missing knowledge will tell us what the next facet/category needs to be.

---

