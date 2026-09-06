;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.onboarding
  "Pure new-actor projections derived from the Foresight project model.

   This namespace deliberately contains no checkout or Git I/O. Runtime adapters
   may print the projection, while repository-local process documents remain
   authoritative for local facts."
  (:require [clojure.string :as str]))

(def steps
  [{:step/id :orient
    :step/title "Orient at the root"
    :step/instruction
    "Validate the semantic project declaration and inspect the checkout before editing anything."
    :step/commands ["nbb scripts/project.clj validate"
                    "nbb scripts/workspace.clj inventory"]}
   {:step/id :route
    :step/title "Route the work"
    :step/instruction
    "Use the project-model route below to choose the likely owning repository. Roles are evidence-routing hints, not grants of cross-repository ownership."}
   {:step/id :ground
    :step/title "Ground in the owner"
    :step/instruction
    "Enter the selected repository and read its AGENTS.md when present, then README.md, ROADMAP.md, architecture/process records, and the code/tests that own the fact. Repository-local evidence controls repository-local behavior."}
   {:step/id :work
    :step/title "Work at the narrowest lawful boundary"
    :step/instruction
    "Implement in the repository that owns the behavior. Do not make Foresight overwrite a child's package-manager, runtime, board, or quality policy merely for uniformity."}
   {:step/id :verify
    :step/title "Verify what actually changed"
    :step/instruction
    "Run the owning repository's relevant gates. A green check that does not execute the changed boundary is not evidence for that boundary. Missing tools or services remain unavailable, never silently pass."}
   {:step/id :learn
    :step/title "Learn from working history"
    :step/instruction
    "When a technique works, preserve the exact repository revision and evidence. Repeated success can become a Foresight lift candidate; one child merge is evidence of local success, not automatic global law."}
   {:step/id :promote
    :step/title "Promote the reusable part"
    :step/instruction
    "Move only the durable cross-repository semantics into Foresight: recovered evidence -> lift candidate -> explicit acceptance -> executable law or generated projection. Keep runtime-specific machinery with its owner."}])

(def precedents
  [{:precedent/id :evidence-first-review
    :precedent/repository "open-hax/foresight"
    :precedent/pr 29
    :precedent/revision "bc45117abbc5208ceee25f73f03eadfc6eae58c8"
    :precedent/lesson
    "Mine a working cross-repository review stack as revision-scoped evidence before promoting portable semantics."}
   {:precedent/id :executable-portability
    :precedent/repository "open-hax/foresight"
    :precedent/pr 30
    :precedent/revision "c19b4dca35ade1c57a6e2827fd82b6041e6a1c66"
    :precedent/lesson
    "Portable source intent is not portability proof; claimed runtimes must actually load and execute the law."}
   {:precedent/id :semantic-project-law
    :precedent/repository "open-hax/foresight"
    :precedent/pr 33
    :precedent/revision "bb6b478bc33b52952698b6abcbf76c3f68ccc9ae"
    :precedent/lesson
    "Recover repository knowledge into one portable semantic project declaration with provenance."}
   {:precedent/id :projected-routing
    :precedent/repository "open-hax/foresight"
    :precedent/pr 35
    :precedent/revision "df95723be66f228c69e4c276dbdc0cc183ba7a08"
    :precedent/lesson
    "Project the semantic source inventory into a repository-routing view for new actors."}
   {:precedent/id :routing-not-authority
    :precedent/repository "open-hax/foresight"
    :precedent/pr 36
    :precedent/revision "873b49ba4e9d5ee9e0f267c2af2c7301441f73bc"
    :precedent/lesson
    "Keep routing hints distinct from ownership and promotion authority; local evidence wins for local facts."}
   {:precedent/id :revision-bound-review
    :precedent/repository "open-hax/foresight"
    :precedent/pr 38
    :precedent/revision "909ec43caa592a310d738e2b5fe562990d2551c5"
    :precedent/lesson
    "Use merged child behavior as revision-bound evidence for candidate laws without silently promoting it."}
   {:precedent/id :normalized-archaeology
    :precedent/repository "open-hax/foresight"
    :precedent/pr 39
    :precedent/revision "85f04b3fcc2c608320d8e76d81dfffa2bd709e09"
    :precedent/lesson
    "Make recurring archaeology append-only and causal, with normalized facts and disposable projections."}
   {:precedent/id :relevant-gate
    :precedent/repository "open-hax/foresight"
    :precedent/pr 41
    :precedent/revision "2013351996f1766246f201640795f342b3e12461"
    :precedent/lesson
    "A relevant executable gate can reveal that several nominally green checks never exercised the broken component."}
   {:precedent/id :evidence-driven-promotion
    :precedent/repository "open-hax/foresight"
    :precedent/pr 49
    :precedent/revision "366d1ff427c690602ad67b0ac3df2a806af939f5"
    :precedent/lesson
    "Bind promotion to exact revisions and explicit evidence; unavailable and incomplete evidence cannot become a pass."}
   {:precedent/id :portable-actor-runtime
    :precedent/repository "open-hax/foresight"
    :precedent/pr 50
    :precedent/revision "fcb30c0bbbf1b7558d465e479c0b1b34f3d275a5"
    :precedent/lesson
    "Package the common actor toolchain as a reproducible verified runtime instead of assuming host setup."}
   {:precedent/id :pinned-repository-census
    :precedent/repository "open-hax/foresight"
    :precedent/pr 60
    :precedent/revision "c3e2da70a444eacd943f156aef076b3247766065"
    :precedent/lesson
    "Census the wider repository lineage at immutable revisions while preserving gaps and uncertainty instead of guessing."}])

(defn- keyword-label [value]
  (if (keyword? value)
    (if-let [ns-name (namespace value)]
      (str ns-name "/" (name value))
      (name value))
    (str value)))

(defn route-rows
  "Project the current source inventory into the fields a new actor needs for routing."
  [project]
  (mapv (fn [source]
          {:route/id (:source/id source)
           :route/path (:source/path source)
           :route/repository (or (:source/repository source) "(root-owned)")
           :route/role (:source/role source)
           :route/ownership (:source/ownership source)
           :route/actionable? (boolean (:source/actionable? source))})
        (:project/sources project)))

(defn guide
  "Return the complete data projection used by human-facing onboarding surfaces."
  [project]
  {:guide/project (:project/id project)
   :guide/generated-from "src/foresight/project.cljc"
   :guide/steps steps
   :guide/routes (route-rows project)
   :guide/precedents precedents})

(defn- markdown-cell [value]
  (-> (keyword-label value)
      (str/replace "|" "\\|")
      (str/replace "\n" " ")))

(defn- render-step [index {:step/keys [title instruction commands]}]
  (str "### " (inc index) ". " title "\n\n"
       instruction
       (when (seq commands)
         (str "\n\n```sh\n"
              (str/join "\n" commands)
              "\n```"))))

(defn- render-route
  [{:route/keys [path repository role ownership actionable?]}]
  (str "| `" (markdown-cell path) "`"
       " | " (markdown-cell repository)
       " | `" (markdown-cell role) "`"
       " | `" (markdown-cell ownership) "`"
       " | " (if actionable? "yes" "no")
       " |"))

(defn- render-precedent
  [{:precedent/keys [repository pr revision lesson]}]
  (str "| [#" pr "](https://github.com/" repository "/pull/" pr ")"
       " | `" revision "`"
       " | " (markdown-cell lesson)
       " |"))

(defn markdown
  "Render the new-actor guide from the semantic project declaration.

   This is a projection, not a second source inventory. If a route changes,
   change foresight.project and regenerate/print this view."
  [project]
  (let [{:guide/keys [steps routes precedents]} (guide project)]
    (str "# Foresight new-actor guide\n\n"
         "> This view is generated from `src/foresight/project.cljc` plus "
         "revision-bound Foresight precedents. Do not hand-maintain a competing "
         "source inventory.\n\n"
         "A new actor should be able to route work without prior chat memory. "
         "Foresight owns constellation-level orientation and learned cross-repository "
         "practice; each child still owns its local behavior and process.\n\n"
         "## Work loop\n\n"
         (str/join "\n\n" (map-indexed render-step steps))
         "\n\n## Current repository routes\n\n"
         "| Path | Repository | Role | Ownership | Actionable here? |\n"
         "| --- | --- | --- | --- | --- |\n"
         (str/join "\n" (map render-route routes))
         "\n\n## Working-history precedents\n\n"
         "These merged Foresight changes are examples of the learning loop succeeding. "
         "Their merge revisions preserve the exact historical evidence; they do not "
         "make every child-specific detail universal law.\n\n"
         "| PR | Merge revision | Durable lesson |\n"
         "| --- | --- | --- |\n"
         (str/join "\n" (map render-precedent precedents))
         "\n\n## Authority order\n\n"
         "1. Git objects and the checked-out gitlink establish *which revision* is under discussion.\n"
         "2. The selected repository's current code, tests, ADRs, `AGENTS.md`, and `README.md` establish local facts.\n"
         "3. `src/foresight/project.cljc` establishes the root's semantic inventory and routing metadata.\n"
         "4. Foresight archaeology and evidence records preserve recovered and candidate cross-repository knowledge.\n"
         "5. Explicit promotion turns a reusable candidate into common Foresight law or a generated projection.\n\n"
         "Similarity, repetition, merge status, or a convenient routing label never silently becomes authority.\n")))
