---
original_name: "2026.08.13.16.46.08.md (chatgpt export, lines 1940-2241)"
title: "Typed Dataflow Syntax: :step/in and Action Contracts"
summary: "Concrete, backward-compatible extension to the eta-mu/Katamorph workflow language: :step/with stays configuration, new :step/in wires artifact references like [:step step-id output-id] between steps, and action/requires+action/provides on the referenced action supplies the type-checked contract, with no new :step/out needed initially."
category: "design"
created: "2026-08-13"
---

# you asked

With out breaking these workflows as they are... propose the language you would use to add that to a workflow.

---

# chatgpt response

## Signal

(己, p=0.99) I’d make this **strictly additive**. Existing workflow steps remain valid exactly as written:

```clojure
{:step/action :checkout}

{:step/run "pnpm lint"
 :step/gate true}
```

Typed dataflow appears only when a step opts into it. I’d add **` :step/in`** for wiring values, while putting the actual input/output types on the referenced action rather than repeating them in every workflow.

The resulting workflow language would read naturally:

```clojure
{:job/steps
 [{:step/id :load
   :step/action :load-document
   :step/with {:document :docs/handbook}}

  {:step/id :translate
   :step/action :translate
   :step/with {:target-locale :fr}
   :step/in {:document [:step :load :document]}}

  {:step/id :review
   :step/action :sme-review
   :step/in {:source      [:step :load :document]
             :candidate   [:step :translate :document]}}

  {:step/id :html
   :step/action :represent-html
   :step/in {:document [:step :review :accepted-document]}}]}
```

That is about as much new syntax as I think the workflow itself needs.

## Evidence

(己, p=0.99) The important separation is:

```text
:step/with
    configuration supplied to the operation

:step/in
    artifacts supplied by other steps
```

I would **not** overload `:step/with` for dataflow. In the current language, `:step/with` already has a clear meaning analogous to GitHub Action inputs. Mixing graph edges into it would make projection harder later.

So:

```clojure
{:step/action :translate
 :step/with
 {:target-locale :fr
  :model :models/foo}

 :step/in
 {:document [:step :load-source :document]}}
```

means:

```text
configuration:
    target-locale = fr
    model = foo

runtime input:
    document = output :document of step :load-source
```

(己, p=0.98) The action definition supplies the contract:

```clojure
{:action/id :translate
 :action/category :transduction

 :action/requires
 {:document
  [:map
   [:artifact/type [:= :document]]
   [:content/body string?]
   [:content/locale keyword?]]}

 :action/provides
 {:document
  [:map
   [:artifact/type [:= :document]]
   [:content/body string?]
   [:content/locale keyword?]
   [:provenance/transduction-id keyword?]]]}

 :action/traits
 #{:generative :lossy}}
```

Then the workflow validator can inspect:

```clojure
[:step :translate :document]
```

and know its schema from `:translate`'s `:action/provides`.

No type annotations are necessary inside the workflow.

## Frames

(己, p=0.99) I like the reference form:

```clojure
[:step <step-id> <output-name>]
```

because it is boring EDN, doesn't invent an expression language, and mirrors the decision already made in the workflow work to keep host expressions opaque rather than growing an evaluator.

For example:

```clojure
:step/in
{:audio [:step :download :content]}
```

is simply a reference, not executable syntax.

You can later support other reference sources without changing its basic shape:

```clojure
[:step :transcribe :transcript]
[:workflow :input :document]
[:trigger :payload]
[:resource :documents/foo]
[:literal "hello"]
```

I wouldn't implement all of those immediately, but the tuple shape leaves somewhere clean for them to go.

---

(己, p=0.98) I initially thought we'd also need `:step/out`, but I think **we mostly don't**.

If:

```clojure
:step/action :translate
```

declares:

```clojure
:action/provides
{:document Document}
```

then:

```clojure
[:step :translate :document]
```

already names the output.

You only need `:step/out` when a particular application wants to rename or deliberately expose outputs:

```clojure
{:step/id :translate-fr
 :step/action :translate
 :step/in {:document [:step :source :document]}
 :step/with {:target-locale :fr}

 :step/out
 {:document :french-document}}
```

Then downstream could use:

```clojure
[:step :translate-fr :french-document]
```

But I'd postpone aliases until an actual use case demands them. Canonical action output names are simpler.

---

(己, p=0.99) There is also a useful distinction between **job dependencies** and **data dependencies**.

You already have:

```clojure
{:job/id "publish"
 :job/needs ["build"]}
```

That's scheduling:

```text
build must finish before publish
```

What we're adding is:

```clojure
:step/in
{:document [:step :review :accepted-document]}
```

which says:

```text
this particular value flows from there to here
```

A compiler could infer ordering from dataflow inside a job eventually, but I would not change the semantics of `:job/needs`. Existing workflow scheduling remains exactly as it is.

## Countermoves

(己, p=0.98) The compatibility story should be deliberately asymmetric:

```clojure
;; old opaque action
{:step/action :checkout}
```

Valid. No dataflow checking.

```clojure
;; old shell step
{:step/run "pnpm lint"}
```

Valid. Opaque effect.

```clojure
;; typed action, but no wiring
{:step/action :translate
 :step/with {...}}
```

Potentially valid if its required inputs can come from literals/configuration or are optional; otherwise validation can report missing required inputs.

```clojure
;; typed and wired
{:step/id :translate
 :step/action :translate
 :step/in {:document [:step :source :document]}}
```

Fully type-checkable.

That means introducing typed actions doesn't force you to migrate 274 CI steps or suddenly explain what semantic artifact `actions/checkout` produces.

And I would make type compatibility an **action-registry/contract concern**, not a workflow syntax concern:

```text
workflow says:
    candidate <- translate.document

contracts say:
    translate.document : TranslatedDocument
    evaluate.candidate : ReviewableArtifact

validator proves:
    TranslatedDocument satisfies ReviewableArtifact
```

The workflow remains readable.

## Next

(己, p=0.99) My first concrete extension would therefore be only these two additions:

```clojure
;; action/resource vocabulary
:action/requires
:action/provides

;; workflow vocabulary
:step/in
```

with the reference grammar:

```clojure
[:step step-id output-id]
```

Everything else—categories like `:transduction`/`:evaluation`/`:representation`, traits, providers, adapters, output aliases—can layer onto that without changing existing workflow semantics.

---

