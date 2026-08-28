(ns foresight.archaeology.shape
  (:require [clio.law.schema :as clio-schema]
            [foresight.archaeology.law :as law]
            [malli.core :as m]))

(def uuid-string
  [:string {:min 36 :max 36}])

(def nonblank-string
  [:string {:min 1}])

(def run-data
  [:map {:closed true}
   [:run/id uuid-string]
   [:run/topic nonblank-string]
   [:run/summary nonblank-string]])

(def finding-data
  [:map {:closed true}
   [:finding/id :keyword]
   [:finding/shapes [:set :keyword]]
   [:finding/current-name nonblank-string]
   [:finding/candidate-name nonblank-string]
   [:finding/claim nonblank-string]])

(def action-data
  [:map {:closed true}
   [:action/id :keyword]
   [:action/type :keyword]
   [:action/status [:enum :open :done :superseded :rejected]]
   [:action/description nonblank-string]])

(def evidence-data
  [:map {:closed true}
   [:evidence/id :keyword]
   [:evidence/repo nonblank-string]
   [:evidence/path nonblank-string]
   [:evidence/commit nonblank-string]
   [:evidence/blob nonblank-string]
   [:evidence/url nonblank-string]])

(def relation-data
  [:map {:closed true}
   [:relation/id :keyword]
   [:relation/kind [:enum :finding/evidence :run/consumes]]
   [:relation/from-kind :keyword]
   [:relation/from-id [:or :keyword :string]]
   [:relation/to-kind :keyword]
   [:relation/to-id [:or :keyword :string]]
   [:relation/action-id {:optional true} :keyword]
   [:relation/status {:optional true}
    [:enum :continues :consumes :supersedes :rejects :acknowledges]]])

(def catalog
  {:archaeology/run-recorded
   (clio-schema/event-schema :archaeology/run-recorded run-data)

   :archaeology/finding-recorded
   (clio-schema/event-schema :archaeology/finding-recorded finding-data)

   :archaeology/action-recorded
   (clio-schema/event-schema :archaeology/action-recorded action-data)

   :archaeology/evidence-recorded
   (clio-schema/event-schema :archaeology/evidence-recorded evidence-data)

   :archaeology/relation-recorded
   (clio-schema/event-schema :archaeology/relation-recorded relation-data)})

(def ledger-ref
  [:map {:closed true}
   [:ledger/path nonblank-string]
   [:ledger/event-type :keyword]])

(def run-resource-entry
  "A single registered :document resource carrying archaeology reference
   facets. :document/id makes this consumable by Katamorph's existing manifest
   grammar without adding a Foresight-specific resource kind to Katamorph."
  [:map {:closed true}
   [:document/id :keyword]
   [:document/type [:= :foresight.archaeology/run]]
   [:archaeology/run-id uuid-string]
   [:archaeology/schema
    [:map {:closed true}
     [:catalog/path nonblank-string]
     [:history/path nonblank-string]]]
   [:archaeology/ledgers
    [:map {:closed true}
     [:run ledger-ref]
     [:findings ledger-ref]
     [:actions ledger-ref]
     [:evidence ledger-ref]
     [:relations ledger-ref]]]
   [:archaeology/projection [:= :foresight.archaeology/run]]])

(def resource-file
  "One-entry Katamorph namespace manifest. The required :resources vector is
   structural manifest syntax, not a ledger: growing records remain ND-EDN."
  [:map {:closed true}
   [:namespace [:= :foresight.archaeology]]
   [:resources [:tuple run-resource-entry]]])

(def consume-projection
  [:map {:closed true}
   [:event/id uuid-string]
   [:action/id :keyword]
   [:relation
    [:enum :continues :consumes :supersedes :rejects :acknowledges]]])

(def finding-projection
  [:map {:closed true}
   [:finding/id :keyword]
   [:finding/shapes [:set :keyword]]
   [:finding/current-name nonblank-string]
   [:finding/candidate-name nonblank-string]
   [:finding/claim nonblank-string]
   [:evidence [:vector :keyword]]])

(def run-projection
  [:map {:closed true}
   [:event/id uuid-string]
   [:event/parents [:vector uuid-string]]
   [:archaeology/topic nonblank-string]
   [:archaeology/summary nonblank-string]
   [:archaeology/consumes [:vector consume-projection]]
   [:archaeology/findings [:vector finding-projection]]
   [:archaeology/actions [:vector action-data]]
   [:evidence/refs [:vector evidence-data]]])

(defn valid-resource?
  [value]
  (and (m/validate resource-file value)
       (law/resource-valid? value)))

(defn valid-run-projection?
  [value]
  (and (m/validate run-projection value)
       (law/parent-continuity-valid? value)))
