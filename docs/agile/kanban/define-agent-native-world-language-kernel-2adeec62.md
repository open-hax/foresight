---
category: "kanban"
labels: "contract-language, actors, event-sourcing, world-model, python"
type: "task"
write-id: "1788658237266-8152bb80ee77c342"
title: "Define agent-native world language kernel"
priority: "P0"
status: "accepted"
uuid: "8b4c1abb-6599-4bdf-a2aa-2adeec62e821"
created_at: "2026-09-06T01:30:37Z"
---

# Define agent-native world language kernel

Canonical tracking lives in [Foresight issue #77](https://github.com/open-hax/foresight/issues/77).
The architecture seed is `docs/specs/agent-native-world-language.md`.

## Objective

Build the first executable implementation of the portable language Foresight has
been converging toward: a small S-expression/Hiccup-shaped contract language for
actors situated inside an event-sourced world model. The language must describe
current state, desired state, lawful plans/actions, typed capabilities, policies,
causal sessions, events, receipts, projections, and model context without baking
in GitHub, Discord, Google, a specific LLM provider, or a specific runtime.

Prompt generation is one renderer over this language, not the language itself.

## Work ledger

### 1. Grammar and AST

- [ ] Specify literal, pure, effect, pending, and invalid node classes.
- [ ] Specify normalized AST identity and versioning.
- [ ] Specify immutable subtree context inheritance/narrowing.
- [ ] Specify evaluation order and form expansion.
- [ ] Specify errors as data.
- [ ] Specify codec/version negotiation.
- [ ] Prohibit arbitrary host `eval` from the kernel.

### 2. Functional primitive floor

- [ ] scalar values and keyword/symbol-like identifiers
- [ ] vectors/lists, maps, sets
- [ ] equality and ordering predicates
- [ ] membership/subset/relation predicates
- [ ] `map`, `filter`, `fold/reduce`
- [ ] composition/pipeline
- [ ] option/result values
- [ ] traversal with accumulated path-aware errors
- [ ] stable typed refs

Add no broader language feature until a kernel contract actually requires it.

### 3. Malli-like schema subset

- [ ] `:string`, `:int`, `:bool`, `:keyword`
- [ ] `:enum`
- [ ] `:maybe`
- [ ] `:vector`, `:tuple`, `:set`
- [ ] `:map` with required/optional entries
- [ ] `:or`, `:and`
- [ ] registry `:ref`
- [ ] closed-registry `:pred`
- [ ] path-aware error accumulation
- [ ] recursive schemas only where demonstrated necessary
- [ ] schema registry identity/version rules

### 4. Kernel shapes/contracts

Every shape needs a schema, identity/ref rules, validation fixtures, and explicit
ownership boundary.

- [ ] `schema`
- [ ] `actor` with kinds `:human`, `:program`, `:agent`
- [ ] `authority`
- [ ] `authority-evidence`
- [ ] delegation
- [ ] `resource`
- [ ] `role`
- [ ] `capability`
- [ ] `policy`
- [ ] `observation`
- [ ] `claim`
- [ ] `evidence`
- [ ] `desired-state`
- [ ] `plan`
- [ ] `action`
- [ ] `event`
- [ ] `receipt`
- [ ] `projection`
- [ ] `session`
- [ ] `artifact`

### 5. Actor and authority laws

- [ ] An actor kind never grants authority by itself.
- [ ] Human/program/agent actors share interaction/event machinery.
- [ ] Capabilities are explicit typed grants, not role side effects.
- [ ] Roles remain descriptive/contextual rather than permissive.
- [ ] Authority decisions are backed by issuer/delegation evidence.
- [ ] External/provider account linkage is evidence, not universal identity.
- [ ] Durable decisions remain replayable when the original issuer is offline.
- [ ] Delegated child contexts cannot silently gain authority.

### 6. World/event laws

- [ ] Events are immutable facts.
- [ ] Current world state is a projection over admitted events.
- [ ] Desired state is explicit data.
- [ ] Plans are candidate transition paths, not execution authority.
- [ ] Actions require policy admission.
- [ ] Effects emit events and receipts.
- [ ] Multiple projections may coexist without becoming competing truth.
- [ ] Public/private/content-management views are visibility projections over
      linked provenance, not destructive data moves.

### 7. Session/effect forms

Implement and specify the first closed resolver set:

- [ ] `:session/started`
- [ ] `:resource/read`
- [ ] `:agent/invoke`
- [ ] `:session/await`
- [ ] `:artifact/emit`
- [ ] `:session/complete`

Each form needs:

- [ ] input schema
- [ ] output schema
- [ ] capability id
- [ ] policy admission boundary
- [ ] resolver contract
- [ ] event expansion
- [ ] receipt shape
- [ ] typed failure shape
- [ ] deterministic replay behavior

### 8. Session result bundle

- [ ] session id
- [ ] root event id
- [ ] status
- [ ] causal events
- [ ] resources read
- [ ] child session refs/results
- [ ] artifacts
- [ ] receipts
- [ ] projection diff

A child invocation must return this structured causal bundle to its parent rather
than collapsing the result to prose.

### 9. Python reference runtime

Build a dependency-light implementation that can be handed directly to an agent
as a folder/script.

- [ ] EDN reader
- [ ] EDN writer
- [ ] AST normalizer
- [ ] schema validator
- [ ] primitive registry
- [ ] predicate registry
- [ ] pure-form evaluator
- [ ] effect resolver registry
- [ ] immutable evaluation context
- [ ] event/receipt reducer
- [ ] session projection
- [ ] deterministic context/prompt renderer
- [ ] character/byte counts for rendered prompts
- [ ] trusted-local pickle encode/cache adapter
- [ ] no untrusted-pickle load path
- [ ] CLI for validate/eval/render/replay

### 10. Portable codecs and host equivalence

- [ ] EDN is canonical portable interchange/source syntax.
- [ ] Pickle is documented as trusted-local runtime cache only.
- [ ] Define canonical semantic equality independent of codec.
- [ ] EDN -> AST -> pickle cache -> AST equality fixture.
- [ ] Host-neutral conformance fixture set.
- [ ] Later Node/NBB/CLJ implementation must pass identical fixtures.

### 11. Prompt/context compiler

- [ ] Render role + mission/instructions.
- [ ] Render policy-filtered world slice.
- [ ] Render observations/claims/evidence relevant to the task.
- [ ] Render desired state.
- [ ] Render allowed capabilities/forms.
- [ ] Render input/output schemas and execution protocol.
- [ ] Add compact target profiles for Gemini, Grok, Perplexity, and
      OpenAI-compatible hosts.
- [ ] Target profiles may change wording/limits, never contract semantics.
- [ ] Add conformance probes that measure instruction retention separately from
      task correctness.

### 12. First end-to-end proof

From a minimal/empty initial world:

- [ ] create one coordinator actor and role;
- [ ] declare bounded resource-read capability;
- [ ] declare child-agent invocation capability;
- [ ] read two abstract resources;
- [ ] invoke two specialist child actors with narrowed contexts;
- [ ] await both child sessions;
- [ ] receive structured child-session bundles;
- [ ] synthesize one artifact;
- [ ] complete the parent session;
- [ ] replay the event set into the same final session/world projection;
- [ ] render the coordinator model context from the same source contracts.

No provider-specific noun belongs in this proof.

### 13. Conformance/evidence

- [ ] golden parse/print fixtures
- [ ] valid/invalid schema fixtures
- [ ] unknown-form rejection
- [ ] policy-denial fixture
- [ ] authority-evidence fixture
- [ ] deterministic replay fixture
- [ ] parent/child causal-session fixture
- [ ] pending/effect-resolution fixture
- [ ] prompt renderer snapshots/counts
- [ ] cross-codec semantic-equality fixture
- [ ] exact-revision receipts and independent review

### 14. Ownership reconciliation

Current Foresight routing says portable contract grammar/resolver contracts belong
to Katamorph and canonical event/replay semantics belong to Clio. Before
promoting code as canonical:

- [ ] identify which forms are Foresight composition law;
- [ ] place reusable grammar/schema/resolver implementation in Katamorph;
- [ ] reuse rather than clone Clio event/causal/replay law;
- [ ] map durable actor/authority semantics onto the proper identity boundary;
- [ ] keep Knoxx focused on product/session/context composition;
- [ ] keep Proxx focused on provider/model routing policy and abductive route
      selection;
- [ ] keep Python/Node/NBB/filesystem/provider code as adapters around the pure
      contract layer.

## Exit

One portable, reviewed kernel can express and execute the abstract coordinator
session end to end, reconstruct the same world/session state by replay, render a
model context from the same source contracts, and pass host-neutral conformance
fixtures. The Foresight/Katamorph/Clio/identity/Knoxx/Proxx ownership boundaries
are explicit enough that the next implementation PR cannot accidentally create a
second semantic authority.

---
Projected from Foresight #77.
---
