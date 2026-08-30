;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.project
  "Portable declaration of the Foresight source constellation and the laws its
   current repositories already claim. This namespace contains data only plus
   pure indexing helpers; checkout inspection and execution stay in adapters.")

(def invariants
  [{:invariant/id :foresight/source-ids-unique
    :invariant/scope :project
    :invariant/enforcement :executable
    :invariant/check :source-ids-unique
    :invariant/statement "Every declared direct source has one stable project-local identity."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "test/workspace_test.cljs"
                       :basis/symbol "inventories-declared-consolidation-inputs"}]}
   {:invariant/id :foresight/source-paths-unique
    :invariant/scope :project
    :invariant/enforcement :executable
    :invariant/check :source-paths-unique
    :invariant/statement "Every declared direct source owns one unique workspace path."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "scripts/workspace.clj"
                       :basis/symbol "validate-submodules!"}]}
   {:invariant/id :foresight/source-paths-confined
    :invariant/scope :project
    :invariant/enforcement :executable
    :invariant/check :source-paths-confined
    :invariant/statement "Declared direct source paths are nonblank relative paths without dot traversal."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "scripts/workspace.clj"
                       :basis/symbol "lexical-source-path"}
                      {:basis/repository "open-hax/foresight"
                       :basis/path "test/workspace_test.cljs"
                       :basis/symbol "lexical-paths-stay-confined"}]}
   {:invariant/id :foresight/gitmodules-match-project
    :invariant/scope :project
    :invariant/enforcement :executable
    :invariant/check :gitmodules-match-project
    :invariant/statement "The direct Git submodule manifest must match the declared project repositories by path and URL."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path ".gitmodules"}
                      {:basis/repository "open-hax/foresight"
                       :basis/path "README.md"}]}
   {:invariant/id :foresight/consolidation-inputs-inventory-only
    :invariant/scope :project
    :invariant/enforcement :executable
    :invariant/check :consolidation-inputs-inventory-only
    :invariant/statement "Consolidation inputs are visible to inventory but never gain execution authority from being present."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "README.md"}
                      {:basis/repository "open-hax/foresight"
                       :basis/path "test/workspace_test.cljs"
                       :basis/symbol "execution-boundaries-refuse-consolidation-inputs"}]}
   {:invariant/id :foresight/actionable-only-independent-submodules
    :invariant/scope :project
    :invariant/enforcement :executable
    :invariant/check :actionable-only-independent-submodules
    :invariant/statement "Only independently owned direct Git submodules may be actionable workspace execution roots."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "scripts/workspace.clj"
                       :basis/symbol "executable-source?"}
                      {:basis/repository "open-hax/foresight"
                       :basis/path "test/workspace_test.cljs"
                       :basis/symbol "executable-authority-is-not-forgeable"}]}
   {:invariant/id :foresight/source-invariants-resolve
    :invariant/scope :project
    :invariant/enforcement :executable
    :invariant/check :source-invariants-resolve
    :invariant/statement "Every invariant referenced by a source or native component is declared by this project model."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "src/foresight/project.cljc"}]}
   {:invariant/id :foresight/independent-repository-ownership
    :invariant/scope :project
    :invariant/enforcement :declarative
    :invariant/statement "Direct .gitmodules entries remain independently owned repositories; root orchestration does not rewrite their package-manager or quality policy."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "AGENTS.md"
                       :basis/symbol "Boundary"}]}
   {:invariant/id :foresight/pure-kernel-before-runtime
    :invariant/scope :architecture
    :invariant/enforcement :declarative
    :invariant/statement "Portable shapes, laws, identity, normalization, graph and ledger semantics belong in pure .cljc before runtime adapters whenever practical."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "AGENTS.md"
                       :basis/symbol "Divine mandate: purify before you port"}]}
   {:invariant/id :foresight/host-values-stay-at-edge
    :invariant/scope :architecture
    :invariant/enforcement :declarative
    :invariant/statement "Semantic boundaries stay Clojure-shaped; raw JS/JVM/provider values are converted at runtime edges and do not become domain authority."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "AGENTS.md"}
                      {:basis/repository "open-hax/knoxx"
                       :basis/path "AGENTS.md"
                       :basis/revision "5249e5303c7b5ee694b8ae5f01b23f30d0a67c31"}]}
   {:invariant/id :foresight/immutable-ledgers
    :invariant/scope :provenance
    :invariant/enforcement :declarative
    :invariant/statement "Ledgers record immutable facts; mutable projections, caches, provider responses and UI state never become semantic authority merely because they exist."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "AGENTS.md"}
                      {:basis/repository "octave-commons/calliope"
                       :basis/path "AGENTS.md"
                       :basis/revision "2655ae6eddbd20ac400a8e1ff99914c56d81b835"}]}
   {:invariant/id :foresight/derived-state-not-authority
    :invariant/scope :provenance
    :invariant/enforcement :declarative
    :invariant/statement "Derived indexes and projections are rebuildable views, not the only copy of a fact or decision."
    :invariant/basis [{:basis/repository "octave-commons/epiphany"
                       :basis/path "AGENTS.md"
                       :basis/revision "ca3fd843b30ef8fd9ca2881aeb9758e58dac6b66"}
                      {:basis/repository "octave-commons/calliope"
                       :basis/path "AGENTS.md"
                       :basis/revision "2655ae6eddbd20ac400a8e1ff99914c56d81b835"}]}
   {:invariant/id :foresight/provider-output-untrusted
    :invariant/scope :boundary
    :invariant/enforcement :declarative
    :invariant/statement "Provider output is untrusted input and must satisfy reusable contracts before crossing a replaceable boundary."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "AGENTS.md"}
                      {:basis/repository "open-hax/foresight"
                       :basis/path "eta/AGENTS.md"}]}
   {:invariant/id :foresight/unavailable-never-pass
    :invariant/scope :execution
    :invariant/enforcement :declarative
    :invariant/statement "Missing tools, unreachable selected services and unsupported actions remain observable as unavailable or failing; they are never guessed into success."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "AGENTS.md"}
                      {:basis/repository "octave-commons/epiphany"
                       :basis/path "AGENTS.md"
                       :basis/revision "ca3fd843b30ef8fd9ca2881aeb9758e58dac6b66"}
                      {:basis/repository "octave-commons/calliope"
                       :basis/path "AGENTS.md"
                       :basis/revision "2655ae6eddbd20ac400a8e1ff99914c56d81b835"}]}
   {:invariant/id :foresight/provenance-under-eta-mu-root
    :invariant/scope :provenance
    :invariant/enforcement :declarative
    :invariant/statement "Foresight provenance ledgers are physically stored beneath .ημ/."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "README.md"}
                      {:basis/repository "open-hax/foresight"
                       :basis/path "AGENTS.md"}]}
   {:invariant/id :alpha/artifact-owns-embedded-relation-source
    :invariant/scope :alpha
    :invariant/enforcement :declarative
    :invariant/statement "An embedded artifact relation names its containing artifact as relation/source."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "alpha/src/alpha/law/artifact.cljc"
                       :basis/symbol "validate-artifact"}]}
   {:invariant/id :alpha/reaction-operation-registered
    :invariant/scope :alpha
    :invariant/enforcement :declarative
    :invariant/statement "A reaction is lawful only when its referenced operation id is registered by the caller."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "alpha/src/alpha/law/artifact.cljc"
                       :basis/symbol "validate-reaction"}]}
   {:invariant/id :epiphany/similarity-not-identity
    :invariant/scope :epiphany
    :invariant/enforcement :declarative
    :invariant/statement "Similarity never silently becomes identity."
    :invariant/basis [{:basis/repository "octave-commons/epiphany"
                       :basis/path "AGENTS.md"
                       :basis/revision "ca3fd843b30ef8fd9ca2881aeb9758e58dac6b66"}]}
   {:invariant/id :epiphany/epistemic-promotion-explicit
    :invariant/scope :epiphany
    :invariant/enforcement :declarative
    :invariant/statement "Observed -> derived -> provisional -> accepted promotion requires an explicit durable event with preserved evidence."
    :invariant/basis [{:basis/repository "octave-commons/epiphany"
                       :basis/path "AGENTS.md"
                       :basis/revision "ca3fd843b30ef8fd9ca2881aeb9758e58dac6b66"}]}
   {:invariant/id :epiphany/git-is-canonical-source
    :invariant/scope :epiphany
    :invariant/enforcement :declarative
    :invariant/statement "Git is canonical for commits, trees, blobs and paths-at-commit; durable observations and decisions may be stored elsewhere without replacing that authority."
    :invariant/basis [{:basis/repository "octave-commons/epiphany"
                       :basis/path "AGENTS.md"
                       :basis/revision "ca3fd843b30ef8fd9ca2881aeb9758e58dac6b66"}]}
   {:invariant/id :calliope/append-only-ingest-and-receipts
    :invariant/scope :calliope
    :invariant/enforcement :declarative
    :invariant/statement "Calliope ingestion truth and Receipt River accountability are append-only; projections are regenerated rather than hand-edited into truth."
    :invariant/basis [{:basis/repository "octave-commons/calliope"
                       :basis/path "AGENTS.md"
                       :basis/revision "2655ae6eddbd20ac400a8e1ff99914c56d81b835"}]}
   {:invariant/id :calliope/similarity-not-identity
    :invariant/scope :calliope
    :invariant/enforcement :declarative
    :invariant/statement "Title, embedding, edit distance and repeated model agreement are evidence, never automatic identity."
    :invariant/basis [{:basis/repository "octave-commons/calliope"
                       :basis/path "AGENTS.md"
                       :basis/revision "2655ae6eddbd20ac400a8e1ff99914c56d81b835"}]}
   {:invariant/id :katamorph/translation-preserves-semantics
    :invariant/scope :katamorph
    :invariant/enforcement :declarative
    :invariant/statement "A host translation preserves the declaration's meaning or reports incompatibility; silently dropping unsupported semantics is a bug."
    :invariant/basis [{:basis/repository "open-hax/katamorph"
                       :basis/path "README.md"
                       :basis/revision "ebeb13657a18cef1094feef43dad6685b9b7d138"}]}
   {:invariant/id :muse/not-canonical-runtime-owner
    :invariant/scope :muse
    :invariant/enforcement :declarative
    :invariant/statement "Muse is a compatibility/compiler workspace, not the canonical owner of actor, session, policy, capability, event or workflow semantics."
    :invariant/basis [{:basis/repository "octave-commons/muse"
                       :basis/path "README.md"
                       :basis/revision "b4bdb0a7d019bb33c71aba1bd8daec5933e7ebde"}]}
   {:invariant/id :services/deployment-vs-application-ownership
    :invariant/scope :services
    :invariant/enforcement :declarative
    :invariant/statement "Deployment topology belongs to services; application behavior, source, tests and package builds belong to the application repository."
    :invariant/basis [{:basis/repository "open-hax/services"
                       :basis/path "README.md"
                       :basis/revision "74e2a8af1bc2e223d75549b17a5e7ee06dc2e091"}]}
   {:invariant/id :services/no-secrets-in-repository
    :invariant/scope :services
    :invariant/enforcement :declarative
    :invariant/statement "Services commits environment schemas and names, never live credentials, tokens, private keys or host secret files."
    :invariant/basis [{:basis/repository "open-hax/services"
                       :basis/path "README.md"
                       :basis/revision "74e2a8af1bc2e223d75549b17a5e7ee06dc2e091"}]}
   {:invariant/id :truth/single-simulation-substrate
    :invariant/scope :truth
    :invariant/enforcement :declarative
    :invariant/statement "Truth has one simulation substrate: the ECS world; phases add content and systems rather than parallel world models."
    :invariant/basis [{:basis/repository "octave-commons/Truth"
                       :basis/path "README.md"
                       :basis/revision "8ade66a3553bdd89696aebf4fc4628a6fe66e5ae"}]}
   {:invariant/id :truth/domain-never-imports-infra
    :invariant/scope :truth
    :invariant/enforcement :declarative
    :invariant/statement "Truth domain code is pure simulation logic and never imports infra."
    :invariant/basis [{:basis/repository "octave-commons/Truth"
                       :basis/path "README.md"
                       :basis/revision "8ade66a3553bdd89696aebf4fc4628a6fe66e5ae"}]}
   {:invariant/id :truth/single-writer-components
    :invariant/scope :truth
    :invariant/enforcement :declarative
    :invariant/statement "Each Truth ECS component has exactly one writer so parallel system order does not define semantics."
    :invariant/basis [{:basis/repository "octave-commons/Truth"
                       :basis/path "README.md"
                       :basis/revision "8ade66a3553bdd89696aebf4fc4628a6fe66e5ae"}]}
   {:invariant/id :eta/harness-not-domain-model
    :invariant/scope :eta
    :invariant/enforcement :declarative
    :invariant/statement "Eta is a transduction harness; reusable semantics remain portable and the harness must not become the domain model."
    :invariant/basis [{:basis/repository "open-hax/foresight"
                       :basis/path "eta/AGENTS.md"}]}
   {:invariant/id :eta-mu/board-is-work-source-of-truth
    :invariant/scope :eta-mu
    :invariant/enforcement :declarative
    :invariant/statement "Eta-mu board state is the single source of truth for work; implementation slices are anchored on cards."
    :invariant/basis [{:basis/repository "open-hax/eta-mu"
                       :basis/path "AGENTS.md"
                       :basis/revision "0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90"}]}
   {:invariant/id :eta-mu/lawful-board-hops
    :invariant/scope :eta-mu
    :invariant/enforcement :declarative
    :invariant/statement "Board transitions follow the Rheos FSM through lawful hops; hand-edited frontmatter is drift, not a legitimate transition."
    :invariant/basis [{:basis/repository "open-hax/eta-mu"
                       :basis/path "AGENTS.md"
                       :basis/revision "0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90"}]}
   {:invariant/id :knoxx/raw-js-confined-to-extern
    :invariant/scope :knoxx
    :invariant/enforcement :declarative
    :invariant/statement "Raw JavaScript interop is born, decoded, encoded and mutated only in Knoxx extern boundaries; ordinary layers exchange CLJS data."
    :invariant/basis [{:basis/repository "open-hax/knoxx"
                       :basis/path "AGENTS.md"
                       :basis/revision "5249e5303c7b5ee694b8ae5f01b23f30d0a67c31"}]}
   {:invariant/id :knoxx/backend-cljs-first
    :invariant/scope :knoxx
    :invariant/enforcement :declarative
    :invariant/statement "Knoxx backend/domain/persistence/service work is ClojureScript-first; TypeScript does not expand into new backend domain logic without explicit authorization."
    :invariant/basis [{:basis/repository "open-hax/knoxx"
                       :basis/path "AGENTS.md"
                       :basis/revision "5249e5303c7b5ee694b8ae5f01b23f30d0a67c31"}]}
   {:invariant/id :bitch-tracker/commonjs-meta-factory
    :invariant/scope :bitch-tracker
    :invariant/enforcement :declarative
    :invariant/statement "The distributable BetterDiscord plugin retains the CommonJS meta-factory export shape."
    :invariant/basis [{:basis/repository "octave-commons/bitch-tracker"
                       :basis/path "README.md"
                       :basis/revision "2751fa62b164c739cdb1ef86adc6aa1a9ff1fb90"}]}
   {:invariant/id :uxx/react-canonical-bindings-wrap
    :invariant/scope :uxx
    :invariant/enforcement :declarative
    :invariant/statement "React is the canonical Uxx component implementation; Reagent and Helix are parity wrappers over the canonical build, with shared tokens as design source of truth."
    :invariant/basis [{:basis/repository "open-hax/uxx"
                       :basis/path "README.md"
                       :basis/revision "97e67a7a758c080450a200e8e6e1ada614eabc6d"}]}
   {:invariant/id :proxx/secrets-stay-local
    :invariant/scope :proxx
    :invariant/enforcement :declarative
    :invariant/statement "Federated Proxx may share non-secret control-plane state; provider credentials, OAuth refresh tokens, API keys and secret keys stay local."
    :invariant/basis [{:basis/repository "open-hax/proxx"
                       :basis/path "README.md"
                       :basis/revision "abbbc8b1ad80738233593e17e751203db785c9e2"}]}
   {:invariant/id :proxx/pricing-overrides-are-edn-policy
    :invariant/scope :proxx
    :invariant/enforcement :declarative
    :invariant/statement "Model-pricing overrides are EDN policy contracts, not one-off JSON blobs or hard-coded TypeScript tables."
    :invariant/basis [{:basis/repository "open-hax/proxx"
                       :basis/path "README.md"
                       :basis/revision "abbbc8b1ad80738233593e17e751203db785c9e2"}]}
   {:invariant/id :website/copy-resolves-from-locale-dictionaries
    :invariant/scope :website
    :invariant/enforcement :declarative
    :invariant/statement "User-visible website copy resolves from portable per-locale .cljc dictionaries keyed by a single message-key set; view namespaces never embed translatable literals."
    :invariant/basis [{:basis/repository "open-hax/website"
                       :basis/path "AGENTS.md"}
                      {:basis/repository "open-hax/foresight"
                       :basis/path "AGENTS.md"
                       :basis/symbol "Divine mandate: purify before you port"}]}
   {:invariant/id :website/published-artifact-is-static
    :invariant/scope :website
    :invariant/enforcement :declarative
    :invariant/statement "The published website is a static build artifact: it holds no secrets, carries no runtime application authority, and needs no application backend to serve a locale."
    :invariant/basis [{:basis/repository "open-hax/website"
                       :basis/path "AGENTS.md"}
                      {:basis/repository "open-hax/services"
                       :basis/path "digitalocean/services/website/README.md"}]}
   {:invariant/id :website/translations-are-reviewed-upstream
    :invariant/scope :website
    :invariant/enforcement :declarative
    :invariant/statement "Translated copy enters the website as reviewed data exported from the Knoxx translation pipeline; the build never machine-translates and the site never fetches copy at runtime."
    :invariant/basis [{:basis/repository "open-hax/website"
                       :basis/path "AGENTS.md"}
                      {:basis/repository "open-hax/knoxx"
                       :basis/path "kanban/epics/knowledge-ops-translation-review-epic.md"}]}])

(def sources
  [{:source/id :agents
    :source/name ".agents"
    :source/path ".agents"
    :source/type :git-submodule
    :source/repository "riatzukiza/.agents"
    :source/url "git@github.com:riatzukiza/.agents.git"
    :source/ownership :nested-git-canonical
    :source/role :skill-catalog
    :source/consolidation? true
    :source/actionable? false
    :source/invariants [:foresight/consolidation-inputs-inventory-only]}
   {:source/id :truth
    :source/name "Truth"
    :source/path "Truth"
    :source/type :git-submodule
    :source/repository "octave-commons/Truth"
    :source/url "git@github.com:octave-commons/Truth.git"
    :source/ownership :independent-repository
    :source/role :simulation-research
    :source/actionable? true
    :source/invariants [:truth/single-simulation-substrate
                        :truth/domain-never-imports-infra
                        :truth/single-writer-components]}
   {:source/id :bitch-tracker
    :source/name "bitch-tracker"
    :source/path "bitch-tracker"
    :source/type :git-submodule
    :source/repository "octave-commons/bitch-tracker"
    :source/url "git@github.com:octave-commons/bitch-tracker.git"
    :source/ownership :independent-repository
    :source/role :betterdiscord-plugin
   :source/actionable? true
   :source/invariants [:bitch-tracker/commonjs-meta-factory]}
   {:source/id :river-city
    :source/name "River-City"
    :source/path "River-City"
    :source/type :git-submodule
    :source/repository "octave-commons/River-City"
    :source/url "git@github.com:octave-commons/River-City.git"
    :source/ownership :independent-repository
    :source/role :observability-and-inference
    :source/actionable? true
    :source/invariants []}
   {:source/id :calliope
    :source/name "calliope"
    :source/path "calliope"
    :source/type :git-submodule
    :source/repository "octave-commons/calliope"
    :source/url "git@github.com:octave-commons/calliope.git"
    :source/ownership :independent-repository
    :source/role :corpus
    :source/actionable? true
    :source/invariants [:calliope/append-only-ingest-and-receipts
                        :calliope/similarity-not-identity]}
   {:source/id :epiphany
    :source/name "epiphany"
    :source/path "epiphany"
    :source/type :git-submodule
    :source/repository "octave-commons/epiphany"
    :source/url "git@github.com:octave-commons/epiphany.git"
    :source/ownership :independent-repository
    :source/role :knowledge-archaeology
    :source/actionable? true
    :source/invariants [:epiphany/similarity-not-identity
                        :epiphany/epistemic-promotion-explicit
                        :epiphany/git-is-canonical-source]}
   {:source/id :eta-mu
    :source/name "eta-mu"
    :source/path "eta-mu"
    :source/type :git-submodule
    :source/repository "open-hax/eta-mu"
    :source/url "git@github.com:open-hax/eta-mu.git"
    :source/ownership :independent-repository
    :source/role :agent-runtime-and-workflow
    :source/actionable? true
    :source/invariants [:eta-mu/board-is-work-source-of-truth
                        :eta-mu/lawful-board-hops]}
   {:source/id :katamorph
    :source/name "katamorph"
    :source/path "katamorph"
    :source/type :git-submodule
    :source/repository "open-hax/katamorph"
    :source/url "git@github.com:open-hax/katamorph.git"
    :source/ownership :independent-repository
    :source/role :contract-language
    :source/actionable? true
    :source/invariants [:katamorph/translation-preserves-semantics]}
   {:source/id :knoxx
    :source/name "knoxx"
    :source/path "knoxx"
    :source/type :git-submodule
    :source/repository "open-hax/knoxx"
    :source/url "git@github.com:open-hax/knoxx.git"
    :source/ownership :independent-repository
    :source/role :agent-product-runtime
    :source/actionable? true
    :source/invariants [:knoxx/raw-js-confined-to-extern
                        :knoxx/backend-cljs-first]}
   {:source/id :muse
    :source/name "muse"
    :source/path "muse"
    :source/type :git-submodule
    :source/repository "octave-commons/muse"
    :source/url "git@github.com:octave-commons/muse.git"
    :source/ownership :independent-repository
    :source/role :compatibility-compiler
    :source/actionable? true
    :source/invariants [:muse/not-canonical-runtime-owner]}
   {:source/id :opencode
    :source/name "opencode"
    :source/path "opencode"
    :source/type :git-submodule
    :source/repository "open-hax/opencode"
    :source/url "git@github.com:open-hax/opencode.git"
    :source/ownership :independent-repository
    :source/role :coding-agent-host
    :source/actionable? true
    :source/options {:git/shallow? true}
    :source/invariants []}
   {:source/id :proxx
    :source/name "proxx"
    :source/path "proxx"
    :source/type :git-submodule
    :source/repository "open-hax/proxx"
    :source/url "git@github.com:open-hax/proxx.git"
    :source/ownership :independent-repository
    :source/role :model-proxy
    :source/actionable? true
    :source/invariants [:proxx/secrets-stay-local
                        :proxx/pricing-overrides-are-edn-policy]}
   {:source/id :services
    :source/name "services"
    :source/path "services"
    :source/type :git-submodule
    :source/repository "open-hax/services"
    :source/url "git@github.com:open-hax/services.git"
    :source/ownership :independent-repository
    :source/role :deployment-orchestration
    :source/actionable? true
    :source/invariants [:services/deployment-vs-application-ownership
                        :services/no-secrets-in-repository]}
   {:source/id :uxx
    :source/name "uxx"
    :source/path "uxx"
    :source/type :git-submodule
    :source/repository "open-hax/uxx"
    :source/url "git@github.com:open-hax/uxx.git"
    :source/ownership :independent-repository
    :source/role :ui-kit
    :source/actionable? true
    :source/invariants [:uxx/react-canonical-bindings-wrap]}
   {:source/id :website
    :source/name "website"
    :source/path "website"
    :source/type :git-submodule
    :source/repository "open-hax/website"
    :source/url "git@github.com:open-hax/website.git"
    :source/ownership :independent-repository
    :source/role :published-website
    :source/actionable? true
    :source/invariants [:website/copy-resolves-from-locale-dictionaries
                        :website/published-artifact-is-static
                        :website/translations-are-reviewed-upstream]}
   {:source/id :eta
    :source/name "eta"
    :source/path "eta"
    :source/type :workspace-root
    :source/ownership :workspace-root
    :source/role :clojure-harness
    :source/consolidation? true
    :source/actionable? false
    :source/invariants [:eta/harness-not-domain-model
                        :foresight/consolidation-inputs-inventory-only
                        :foresight/provider-output-untrusted]}])

(def native-components
  [{:component/id :alpha
    :component/path "alpha"
    :component/role :structural-integrity
    :component/invariants [:alpha/artifact-owns-embedded-relation-source
                           :alpha/reaction-operation-registered]}
   {:component/id :eta
    :component/path "eta"
    :component/role :transduction-harness
    :component/invariants [:eta/harness-not-domain-model]}])

(def project
  {:project/id :open-hax/foresight
   :project/kind :source-constellation
   :project/repository "open-hax/foresight"
   :project/sources sources
   :project/native-components native-components
   :project/invariants invariants})

(defn invariant-index []
  (into {} (map (juxt :invariant/id identity)) invariants))

(defn source-index []
  (into {} (map (juxt :source/id identity)) sources))

(defn submodule-sources []
  (filterv #(= :git-submodule (:source/type %)) sources))

(defn consolidation-inputs []
  (filterv :source/consolidation? sources))

(defn gitmodule-declarations []
  (mapv (fn [source]
          {:name (:source/name source)
           :path (:source/path source)
           :url (:source/url source)})
        (submodule-sources)))
