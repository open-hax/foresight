;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.lineage
  "Revision-bound ancestral sources that inform Foresight without becoming
   workspace roots, execution authorities, or accepted project law.")

(def promethean-revision
  "06a8b83312ea70dcde6d2e423369b410e6d0d3f2")

(def sources
  [{:source/id :promethean
    :source/type :historical-prototype
    :source/repository "octave-commons/promethean"
    :source/revision promethean-revision
    :source/role :ancestral-prototype
    :source/actionable? false
    :source/workspace-authority :none
    :source/execution-authority :none
    :source/semantic-status :proposed/ancestral-prototype
    :source/epistemic-status :provisional
    :source/evidence
    [{:evidence/kind :repository-document
      :evidence/path "README.md"
      :evidence/revision promethean-revision
      :evidence/supports
      [:promethean/modular-cognitive-architecture
       :promethean/embodied-perception-action-loop
       :promethean/independent-services
       :promethean/immutable-functional-core
       :promethean/agent-directed-kanban
       :promethean/operating-system-identity]}]
    :source/claims
    [{:claim/id :promethean/owner-sovereignty
      :claim/status :live
      :claim/epistemic-status :provisional
      :claim/recovery :interpretation
      :claim/statement
      "The owner remains sovereign; human acceptance governs promotion into binding intent."
      :claim/carried-by
      [:eta-mu/autonomy
       :epiphany/explicit-human-promotion]}

     {:claim/id :promethean/intent-compiler
      :claim/status :live-with-revision
      :claim/epistemic-status :provisional
      :claim/recovery :interpretation
      :claim/statement
      "AI may compile human expression into candidate intent, plans, contracts, and actions."
      :claim/revision
      "Agents compile candidate plans; inference does not become accepted intent without an explicit authority boundary."}

     {:claim/id :promethean/learn-once
      :claim/status :live-with-revision
      :claim/epistemic-status :provisional
      :claim/recovery :interpretation
      :claim/statement
      "Reusable capability includes the conditions and evidence that make reuse truthful."
      :claim/revision
      "Preserve solutions, failures, context, contracts, and evidence; reuse only when current conditions satisfy the recorded contract."}

     {:claim/id :promethean/context-field
      :claim/status :recurring-pressure
      :claim/epistemic-status :provisional
      :claim/recovery :interpretation
      :claim/statement
      "Context must influence routing, attention, and action selection."
      :claim/carried-by
      [:openplanner/semantic-graph
       :openplanner/epistemic-kernel]}

     {:claim/id :promethean/modular-intent
      :claim/status :live
      :claim/epistemic-status :provisional
      :claim/recovery :interpretation
      :claim/statement
      "Intent should select independently bounded capabilities without collapsing their ownership."
      :claim/carried-by
      [:eta-mu/skill-registry
       :foresight/independent-capability-constellation]}

     {:claim/id :promethean/eidolon-physics
      :claim/status :rejected-implementation
      :claim/epistemic-status :provisional
      :claim/recovery :interpretation
      :claim/statement
      "The dynamic Eidolon physics implementation is historical evidence, not constitutional law."
      :claim/surviving-proposition
      "Context must influence routing; no particular physics simulation is law."}]}])

(def source-index
  (into {} (map (juxt :source/id identity)) sources))

(def claim-index
  (into {}
        (map (juxt :claim/id identity))
        (mapcat :source/claims sources)))

(defn source-by-id [source-id]
  (get source-index source-id))

(defn claim-by-id [claim-id]
  (get claim-index claim-id))

(defn historical-prototypes []
  (filterv #(= :historical-prototype (:source/type %)) sources))

(defn inventory-summary []
  (let [claims (mapcat :source/claims sources)]
    {:source/count (count sources)
     :source/actionable-count (count (filter :source/actionable? sources))
     :claim/count (count claims)
     :claim/status-counts (frequencies (map :claim/status claims))
     :claim/epistemic-status-counts
     (frequencies (map :claim/epistemic-status claims))}))
