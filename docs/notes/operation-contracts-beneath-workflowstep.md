---
original_name: "2026.08.13.16.46.08.md (chatgpt export, lines 1616-1939)"
title: "Operation Contracts Beneath WorkflowStep"
summary: "Analyzes eta-mu PR #181 against the proposed step-contract idea and concludes Katamorph WorkflowStep/WorkflowJob/WorkflowContract already have the right shape; what is missing is semantic typing (requires/provides/category/traits) on the action/operation referenced by :step/action, not a new peer StepContract concept."
category: "design"
created: "2026-08-13"
---

# you asked

would a step contract be meaningfully different from a workflow like this? https://github.com/open-hax/eta-mu/pull/181

---

# chatgpt response

I’m checking PR #181 directly, because the answer depends on whether that workflow already encodes the same `requires/provides` contract or whether it mainly encodes orchestration and sequencing.

## Signal

(己, p=0.99) **Yes, but I think “step contract” was the wrong level for me to put it at.** Looking at PR #181, I would *not* introduce another peer abstraction called `StepContract`.

Your existing model already has the right structural hierarchy:

```text
Workflow
  └── Job
       └── Step
            └── Action
```

Katamorph's `WorkflowStep` already describes a step occurrence: run something or invoke an action, with inputs/configuration, environment, conditions, timeouts, etc. 

What it's **missing for the DSL we're discussing is semantic typing of the action/operation**, not another concept of a step.

So I'd revise what I said to:

```text
Operation contract     ← requires / provides / traits / category
        ↓ instantiated by
Workflow step
        ↓ connected by
Workflow graph
```

## Evidence

(世, p=1.00) PR #181 already defines a workflow as an identity, triggers, and a job graph whose steps are actions; the same declaration projects to GitHub Actions and local gates. 

And the actual Katamorph shape is already pretty clean:

```clojure
WorkflowStep
  :step/run
  :step/action
  :step/with
  :step/env
  :step/if
  ...

WorkflowJob
  :job/needs
  :job/steps

WorkflowContract
  :workflow/triggers
  :workflow/jobs
```

The PR explicitly describes `:job/needs` as the dependency DAG. 

Eta-mu then adds resolution of reusable actions and toolchains. For example, `:step/action :checkout` resolves against the action registry, while toolchains expand reusable sequences of steps. 

So this:

```clojure
{:step/use :node-pnpm}
```

or:

```clojure
{:step/action :cache
 :step/with {...}}
```

already answers:

> **What operation should execute here, and how?**

What it doesn't answer is:

> **What semantic thing does this operation consume and produce?**

That's the missing layer.

## Frames

(己, p=0.99) I think there are actually **three distinct objects**, and keeping them distinct clears this up:

```text
Operation
    defines capability

Step
    applies an operation

Workflow
    composes applications
```

For example, a transduction operation might declare:

```clojure
{:operation/id :translate
 :operation/category :transduction

 :operation/requires
 [:map
  [:content/body string?]
  [:content/language keyword?]]

 :operation/provides
 [:map
  [:content/body string?]
  [:content/language keyword?]
  [:provenance/source keyword?]]

 :operation/traits
 #{:generative :lossy}}
```

Then a workflow **step** says:

```clojure
{:step/id :translate-french
 :step/action :translate
 :step/with
 {:target-language :fr}}
```

And the workflow says:

```text
load document
      │
      ▼
translate-french
      │
      ▼
SME evaluation
      │
      ▼
HTML representation
```

The workflow validator can now establish:

```text
output(load-document)
       <: input(translate)

output(translate)
       <: input(evaluate)

output(evaluate/accepted-artifact)
       <: input(html-representation)
```

That is meaningfully new, but it belongs primarily to **the action/operation vocabulary**, not `WorkflowStep`.

---

(己, p=0.98) This also makes PR #181 look like the beginning of the same architecture rather than a competing one.

Right now its action registry says things like:

```clojure
:checkout
:setup-java
:setup-node
:cache
```

and fixes an important property: an action is identified centrally instead of each step inventing its implementation/pin. 

The generalized version is:

```text
action identity
    +
implementation/provider
    +
semantic input contract
    +
semantic output contract
    +
traits/capabilities
```

Then providers become replaceable.

For example:

```clojure
{:operation/id :speech-to-text
 :operation/category :transduction
 :requires Audio
 :provides Transcript}
```

could have:

```text
OpenAI provider
Whisper provider
Azure provider
local provider
```

The workflow doesn't care.

That's exactly analogous to #181 making:

```text
workflow resource
      ├── GitHub Actions projector
      └── local-gate projector
```

independent of the declaration itself. 

## Countermoves

(己, p=0.99) I would therefore **avoid this**:

```text
WorkflowContract
StepContract
TransductionContract
EvaluationContract
RepresentationContract
```

That starts duplicating concepts.

I'd lean toward:

```text
Resource
  ├── Workflow
  └── Operation

Operation
  category
  requires
  provides
  traits

Workflow
  graph of Steps

Step
  operation reference
  bindings/configuration
  edge wiring
```

And categories are just classifications of operations:

```clojure
:operation/category :repository
:operation/category :transduction
:operation/category :evaluation
:operation/category :representation
:operation/category :aggregation
:operation/category :decomposition
```

A step is therefore **not a type of operation**.

It's an occurrence/application:

```text
Operation ≈ function
Step      ≈ function application
Workflow  ≈ composition graph
```

In μ-ish notation:

```text
op₁ : A → B
op₂ : B → C

workflow:
  s₁ = op₁(x)
  s₂ = op₂(s₁)

valid iff
  output(op₁) <: input(op₂)
```

(己, p=0.97) There's one further implication I like: **the existing CI workflow doesn't need to acquire semantic artifact typing everywhere.**

This:

```clojure
{:step/run "pnpm lint"}
```

can remain an opaque effect.

It might simply be:

```text
requires: Workspace
provides: GateResult
```

or even `:any → GateResult` initially.

The richer typing becomes valuable specifically when artifacts flow between operations. We don't have to turn shell commands into dependent types to earn the architecture.

## Next

(己, p=0.99) I would treat PR #181's `WorkflowContract` as the composition substrate and add the missing abstraction **underneath it**: define what a typed `Operation`/`Action` resource looks like, then make `WorkflowStep :step/action` reference one and optionally wire named outputs to later inputs.

So: **no new StepContract. Extend the action/operation model until workflows can be type-checked.**

---

