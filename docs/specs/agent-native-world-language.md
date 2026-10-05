# Agent-native world language kernel

- **Status:** proposed kernel
- **Tracking:** Foresight #77
- **Scope:** portable contracts, situated actors, event-sourced world state, typed capabilities, causal sessions, prompt/context rendering
- **Authority:** this document records Foresight composition law; reusable contract-language implementation belongs in Katamorph unless later evidence changes that boundary

## Signal

The language describes **desired worlds and lawful transitions**, not prompts.
Prompt construction is one renderer over the same declarative forms used to
validate actors, capabilities, policies, sessions, events, receipts, and
projections.

The source language should feel like Hiccup: small S-expression/data trees whose
nodes are either values, pure computations, or bounded effect requests. An effect
node may remain pending and later resolve into more language forms. Every subtree
executes inside an explicit inherited context rather than relying on ambient
process state.

```text
source forms
   │
   ├── validate ──> normalized AST
   │                    │
   │                    ├── pure evaluator
   │                    ├── effect resolver
   │                    ├── event/receipt reducer
   │                    ├── projection renderer
   │                    └── prompt/context renderer
   │
   ├── EDN codec  <-----┘
   └── trusted-local pickle cache
```

EDN is the canonical portable syntax. Python pickle is permitted only as a
trusted-local cache/adapter for the normalized AST. Loading untrusted pickle is
outside the language contract.

## Kernel laws

1. **Data precedes runtime.** Runtime choice does not define semantic authority.
2. **Closed evaluation.** No arbitrary host `eval`; executable forms dispatch
   through a finite resolver registry.
3. **Typed references.** Identities and references are explicit values, not
   inferred from provider URLs, filenames, or mutable locations.
4. **Actors share pipes, not authority.** Humans, programs, and autonomous agents
   use the same event/capability machinery while policy controls their powers.
5. **Capabilities are typed functions plus declared effects.** Inputs and
   outputs validate against named schemas before effects are admitted.
6. **Events are immutable facts.** Mutable world state is always a projection.
7. **Desired state is data.** Plans are candidate paths from a current projection
   to a desired projection; execution is separately authorized.
8. **Authority is evidence-backed.** Identity/delegation claims carry replayable
   issuer evidence; live availability of one central server is not required for
   already-evidenced decisions.
9. **Sessions are causal subgraphs.** Parent/child sessions exchange structured
   bundles containing events, resources, artifacts, receipts, and projection
   diffs, not only text.
10. **Context is a projection.** A role is an interface for an actor situated in
    a world model. Model context is rendered from role + policy-filtered world
    slice + allowed capabilities/forms.
11. **Every meaningful effect leaves a receipt.** Admission and result evidence
    remain explainable after replay.
12. **Errors are data.** Validation, policy denial, unknown forms, failed effects,
    and partial child sessions have typed failure shapes.

## Structural primitive floor

The first implementation needs only enough functional machinery to make the
schema and evaluator layers precise:

- scalar values, keyword/symbol-like identifiers, lists/vectors, maps, sets;
- equality and ordering predicates;
- membership, subset, and relation predicates;
- `map`, `filter`, `fold/reduce`, composition/pipeline;
- `option` and `result` values;
- traversal with accumulated path-aware validation errors;
- stable typed refs.

This is deliberately not a general Clojure or Haskell implementation. Add a
primitive only after a kernel form requires it.

## Core forms

### Schema

A portable Malli-like subset. Initial constructors:

```clojure
[:string]
[:int]
[:bool]
[:keyword]
[:enum :human :program :agent]
[:maybe <schema>]
[:vector <schema>]
[:tuple <schema> ...]
[:set <schema>]
[:map [:key <schema>] [:optional-key {:optional true} <schema>] ...]
[:or <schema> ...]
[:and <schema> ...]
[:ref :schema/id]
[:pred :predicate/id]
```

Schema refs resolve through a registry. Predicate identifiers resolve through a
closed predicate registry. Validation accumulates errors as values:

```clojure
{:validation/ok? false
 :validation/errors
 [{:path [:actor/id]
   :schema :ref
   :value nil
   :reason :required}]}
```

### Actor

An actor is a stable identity capable of participating in events. Its kind does
not itself grant authority.

```clojure
{:actor/id [:actor "coordinator"]
 :actor/kind :agent
 :actor/authorities [[:authority "local-root"]]
 :actor/roles [[:role "research-coordinator"]]}
```

Kinds initially are `:human`, `:program`, and `:agent`.

### Authority evidence

Authority evidence binds an issuer statement to an actor, subject, capability
or delegation. Provider credentials may prove possession at admission time, but
credentials themselves are not durable identity.

```clojure
{:authority-evidence/id [:authority-evidence "..."]
 :authority-evidence/issuer [:actor "local-root"]
 :authority-evidence/subject [:actor "coordinator"]
 :authority-evidence/grants [[:capability "resource/read"]]
 :authority-evidence/evidence [[:event "..."]]
 :authority-evidence/valid-from "..."
 :authority-evidence/valid-until nil}
```

### Resource

Resources are typed things an actor may observe or transform. A resource ref is
not a provider locator.

```clojure
{:resource/id [:resource "document-a"]
 :resource/kind :document
 :resource/schema [:schema "document"]
 :resource/provenance [[:event "..."]]}
```

### Role

A role describes how an actor should operate in a context. It does not grant
capabilities.

```clojure
{:role/id [:role "research-coordinator"]
 :role/mission "Read the bounded corpus, delegate focused questions, synthesize one artifact."
 :role/instructions [...]
 :role/output-contract [:schema "coordinator-result"]}
```

### Capability

Capabilities are typed operations with declared effects.

```clojure
{:capability/id [:capability "agent/invoke"]
 :capability/input [:schema "agent-invoke"]
 :capability/output [:schema "session-ref"]
 :capability/effects #{:session/create :agent/call}}
```

### Policy

Policy decides whether an actor may exercise a capability in a particular world
slice. Roles may be policy inputs but never implicit grants.

```clojure
{:policy/id [:policy "coordinator"]
 :policy/effect :allow
 :policy/when
 [:and
  [:actor/kind :agent]
  [:has-capability [:capability "agent/invoke"]]
  [:resource-within-session-scope?]]}
```

### Observation, claim, evidence

An observation is something noticed. A claim is an interpretation. Evidence
supports or challenges claims and decisions.

```clojure
{:observation/id [:observation "..."]
 :observation/actor [:actor "reader"]
 :observation/resource [:resource "document-a"]
 :observation/value {...}}

{:claim/id [:claim "..."]
 :claim/proposition {...}
 :claim/evidence [[:evidence "..."]]
 :claim/confidence 0.82}
```

### Desired state, plan, action

```clojure
{:desired-state/id [:desired-state "review-complete"]
 :desired-state/conditions
 [[:artifact/exists? [:artifact "synthesis"]]
  [:children/complete?]]}

{:plan/id [:plan "..."]
 :plan/from [:projection "current"]
 :plan/to [:desired-state "review-complete"]
 :plan/actions [[:action "read"] [:action "delegate-a"] ...]}
```

Plans are proposals. An action still requires policy admission.

### Event and receipt

An event records an immutable happened fact. A receipt records durable admission
or effect evidence.

```clojure
{:event/id [:event "..."]
 :event/type :resource/read
 :event/actor [:actor "coordinator"]
 :event/session [:session "root"]
 :event/causes [[:event "session-started"]]
 :event/payload {...}}

{:receipt/id [:receipt "..."]
 :receipt/event [:event "..."]
 :receipt/status :accepted
 :receipt/policy [:policy "coordinator"]
 :receipt/evidence [...]}
```

### Projection

A projection is a rebuildable view over an event set. Public/private views,
search indexes, current session state, and model context are all projections;
none becomes history merely because it is convenient.

### Session

A session is a causal event subgraph rooted by `:session/started`.

```clojure
{:session/id [:session "root"]
 :session/root-event [:event "session-started"]
 :session/actor [:actor "coordinator"]
 :session/status :running
 :session/children [[:session "child-a"] [:session "child-b"]]
 :session/resources [[:resource "document-a"]]
 :session/artifacts []
 :session/receipts []}
```

## First executable form set

The first complete abstract flow needs exactly six effect-bearing forms.

```clojure
[:session/started
 {:session/id [:session "root"]
  :actor/id [:actor "coordinator"]
  :goal "Read the corpus, ask two focused agents, synthesize one artifact."
  :world/current [:projection "initial"]
  :world/desired [:desired-state "complete"]
  :forms/allowed #{:resource/read :agent/invoke :session/await
                   :artifact/emit :session/complete}}]

[:resource/read
 {:resource/id [:resource "document-a"]
  :purpose "Collect evidence for the requested synthesis."}]

[:agent/invoke
 {:actor/id [:actor "specialist-a"]
  :role/id [:role "specialist"]
  :task {...}
  :resources [[:resource "document-a"]]
  :output/schema [:schema "specialist-result"]}]

[:session/await
 {:sessions [[:session "child-a"] [:session "child-b"]]
  :join :all}]

[:artifact/emit
 {:artifact/id [:artifact "synthesis"]
  :artifact/kind :document
  :inputs [[:session "child-a"] [:session "child-b"]]
  :content {...}}]

[:session/complete
 {:status :completed
  :result [:artifact "synthesis"]}]
```

The runtime expands each admitted effect into one or more immutable events and
receipts. Resolving a child session returns a structured session bundle rather
than flattening it to text.

## Session result bundle

```clojure
{:session/id [:session "root"]
 :session/root-event [:event "..."]
 :session/status :completed
 :session/events [[:event "..."] ...]
 :session/resources [[:resource "..."] ...]
 :session/children [[:session "..."] ...]
 :session/artifacts [[:artifact "synthesis"]]
 :session/receipts [[:receipt "..."] ...]
 :session/projection-diff {...}}
```

A caller can therefore incorporate the complete child causal result into its own
context without pretending the child returned only prose.

## Evaluation model

Each node is classified before evaluation:

```text
literal      -> value
pure form    -> deterministic value/form expansion
effect form  -> EffectRequest, then resolver result -> events/forms
pending      -> stable pending ref, later resolver result -> events/forms
invalid      -> typed error data
```

A subtree receives an immutable context containing the current actor, session,
policy set, schema registry, resolver registry, and permitted world slice.
Children may receive narrower contexts; they never silently inherit more
capability than the parent delegated.

## Prompt/context rendering

A model-facing prompt is a deterministic projection of contract data:

```text
role
+ mission/instructions
+ selected world observations/claims/evidence
+ desired state
+ permitted capabilities/forms
+ input/output schemas
+ execution protocol
```

Target profiles may alter compactness or wording for Gemini, Grok, Perplexity,
OpenAI-compatible hosts, and others. A target profile may not change the
underlying capability, policy, or schema semantics.

Prompt rendering should report byte/character/token estimates and support small
conformance probes so instruction-following drift can be measured separately
from correctness.

## Ownership boundary

Existing Foresight law routes portable resource grammar and resolver contracts
to Katamorph and event identity/canonical replay to Clio. This proposal extends
that lineage rather than replacing it:

- **Foresight:** composition law, architecture, cross-repository evidence, and
  the end-to-end desired-world slice.
- **Katamorph:** canonical portable grammar, schemas, registries, resolver
  contracts, cross-host conformance.
- **Clio:** canonical event identity, causal graph, schema history, admission,
  and replay laws.
- **Axxium/identity layer:** durable actor/principal/delegation semantics when
  that boundary is formally present in the constellation.
- **Knoxx:** product/session/context composition and model-facing interaction.
- **Proxx:** provider/model routing policies and abductive route selection, not
  generic language semantics.
- **Host adapters:** Python, Node/NBB, provider APIs, filesystem, network, and
  other effects.

The first implementation should be dependency-light Python because it can be
handed directly to most coding agents. Its fixtures must be host-neutral so an
NBB/CLJ/Node implementation can prove semantic equivalence rather than fork the
language.

## First proof

Starting from an empty/minimal world projection, construct a coordinator actor
that can read two abstract resources, invoke two child actors, await both child
sessions, synthesize one artifact, complete, and replay the event set into the
same final projection.

No GitHub, Discord, Google, model-provider, or deployment nouns belong in this
proof. Those enter only through adapters after the kernel is coherent.
