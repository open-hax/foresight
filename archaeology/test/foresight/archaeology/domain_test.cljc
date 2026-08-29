;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.archaeology.domain-test
  (:require [clojure.test :refer [deftest is]]
            [foresight.archaeology.domain :as domain]
            [foresight.archaeology.infra :as infra]
            [foresight.archaeology.law :as law]
            [foresight.archaeology.shape :as shape]
            [malli.core :as m]))

(def run-id "00000000-0000-0000-0000-000000000001")
(def parent-run-id "00000000-0000-0000-0000-000000000099")
(def subject (str "archaeology:" run-id))

(defn event [id type data]
  {:event/id id
   :event/schema {:schema/root (apply str (repeat 64 "a"))
                  :schema/id type
                  :schema/hash (apply str (repeat 64 "b"))}
   :event/type type
   :event/stream (str "test:" (name type))
   :event/seq 1
   :event/causes []
   :event/actor "test"
   :event/subject subject
   :event/at "2026-08-28T00:00:00Z"
   :event/data data})

(def run-event
  (event run-id :archaeology/run-recorded
         {:run/id run-id :run/topic "Normalized ledgers" :run/summary "Derived projection"}))
(def finding-event
  (event "00000000-0000-0000-0000-000000000002" :archaeology/finding-recorded
         {:finding/id :finding/one :finding/shapes #{:law :shape}
          :finding/current-name "aggregate" :finding/candidate-name "projection"
          :finding/claim "Collections are derived."}))
(def action-event
  (event "00000000-0000-0000-0000-000000000003" :archaeology/action-recorded
         {:action/id :action/one :action/type :test :action/status :done
          :action/description "Pin projection behavior."}))
(def evidence-event
  (event "00000000-0000-0000-0000-000000000004" :archaeology/evidence-recorded
         {:evidence/id :evidence/one :evidence/repo "open-hax/foresight"
          :evidence/path "archaeology/test" :evidence/commit "test"
          :evidence/blob "test" :evidence/url "https://example.invalid/evidence"}))
(def relation-event
  (event "00000000-0000-0000-0000-000000000005" :archaeology/relation-recorded
         {:relation/id :relation/one :relation/kind :finding/evidence
          :relation/from-kind :finding :relation/from-id :finding/one
          :relation/to-kind :evidence :relation/to-id :evidence/one}))

(def state
  {:run-events {run-id run-event}
   :findings {(:event/id finding-event) finding-event}
   :actions {(:event/id action-event) action-event}
   :evidence {(:event/id evidence-event) evidence-event}
   :relations {(:event/id relation-event) relation-event}})

(def resource
  {:namespace :foresight.archaeology
   :resources
   [{:document/id :run-test
     :document/type :foresight.archaeology/run
     :archaeology/run-id run-id
     :archaeology/schema {:catalog/path "catalog.edn" :history/path "schemas"}
     :archaeology/ledgers
     {:run {:ledger/path "run.edn" :ledger/event-type :archaeology/run-recorded}
      :findings {:ledger/path "findings.edn" :ledger/event-type :archaeology/finding-recorded}
      :actions {:ledger/path "actions.edn" :ledger/event-type :archaeology/action-recorded}
      :evidence {:ledger/path "evidence.edn" :ledger/event-type :archaeology/evidence-recorded}
      :relations {:ledger/path "relations.edn" :ledger/event-type :archaeology/relation-recorded}}
     :archaeology/projection :foresight.archaeology/run}]})

(deftest compose-run-reconstructs-collections
  (let [projection (domain/compose-run state run-id)]
    (is (= [] (:event/parents projection)))
    (is (= [:evidence/one] (get-in projection [:archaeology/findings 0 :evidence])))
    (is (= [:action/one] (mapv :action/id (:archaeology/actions projection))))
    (is (= [:evidence/one] (mapv :evidence/id (:evidence/refs projection))))))

(deftest unknown-run-fails-before-projection-work
  (let [error (try
                (domain/compose-run (assoc state :findings nil)
                                    "00000000-0000-0000-0000-000000000098")
                nil
                (catch #?(:clj Exception :cljs :default) error error))]
    (is (= :unknown-run (:archaeology/error (ex-data error))))))

(deftest relation-shapes-are-kind-specific
  (let [valid-consume {:relation/id :relation/consume
                       :relation/kind :run/consumes
                       :relation/from-kind :run
                       :relation/from-id run-id
                       :relation/to-kind :run
                       :relation/to-id parent-run-id
                       :relation/action-id :parent/action
                       :relation/status :continues}
        missing-status (dissoc valid-consume :relation/status)
        string-finding-link {:relation/id :relation/bad-finding-link
                             :relation/kind :finding/evidence
                             :relation/from-kind :finding
                             :relation/from-id "finding/one"
                             :relation/to-kind :evidence
                             :relation/to-id :evidence/one}]
    (is (m/validate shape/relation-data (:event/data relation-event)))
    (is (m/validate shape/relation-data valid-consume))
    (is (false? (m/validate shape/relation-data missing-status)))
    (is (false? (m/validate shape/relation-data string-finding-link)))))

(deftest resource-declarations-cannot-relabel-ledger-roles
  (let [mislabeled (assoc-in resource
                             [:resources 0 :archaeology/ledgers :run :ledger/event-type]
                             :archaeology/evidence-recorded)]
    (is (law/declared-ledger-event-types-valid? resource))
    (is (false? (law/declared-ledger-event-types-valid? mislabeled)))
    (is (law/resource-valid? resource))
    (is (false? (law/resource-valid? mislabeled)))
    (is (shape/valid-resource? resource))
    (is (false? (shape/valid-resource? mislabeled)))))

(deftest parent-continuity-is-derived-from-run-consumes-relations
  (let [child-run (assoc run-event :event/causes [parent-run-id])
        consume-event
        (event "00000000-0000-0000-0000-000000000006"
               :archaeology/relation-recorded
               {:relation/id :relation/consume
                :relation/kind :run/consumes
                :relation/from-kind :run
                :relation/from-id run-id
                :relation/to-kind :run
                :relation/to-id parent-run-id
                :relation/action-id :parent/action
                :relation/status :continues})
        with-parent (assoc-in state [:run-events run-id] child-run)
        without-continuity-error
        (try
          (domain/compose-run with-parent run-id)
          nil
          (catch #?(:clj Exception :cljs :default) error error))
        with-continuity
        (assoc-in with-parent [:relations (:event/id consume-event)] consume-event)
        projection (domain/compose-run with-continuity run-id)]
    (is (= :invalid-projection
           (:archaeology/error (ex-data without-continuity-error))))
    (is (= [parent-run-id] (:event/parents projection)))
    (is (= [{:event/id parent-run-id
             :action/id :parent/action
             :relation :continues}]
           (:archaeology/consumes projection)))
    (is (law/parent-continuity-valid? projection))
    (is (shape/valid-run-projection? projection))))

(deftest resource-events-preserves-independent-ledger-partitions
  (let [path->events {"run.edn" [run-event]
                      "findings.edn" [finding-event]
                      "actions.edn" [action-event]
                      "evidence.edn" [evidence-event]
                      "relations.edn" [relation-event]}
        partitions (infra/resource-events path->events resource)]
    (is (vector? partitions))
    (is (= 5 (count partitions)))
    (is (every? vector? partitions))
    (is (= (set (vals path->events)) (set partitions)))
    (is (= #{:archaeology/run-recorded :archaeology/finding-recorded
             :archaeology/action-recorded :archaeology/evidence-recorded
             :archaeology/relation-recorded}
           (set (map :event/type (mapcat identity partitions)))))))