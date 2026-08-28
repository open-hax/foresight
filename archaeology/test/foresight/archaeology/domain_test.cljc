;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.archaeology.domain-test
  (:require [clojure.test :refer [deftest is]]
            [foresight.archaeology.domain :as domain]
            [foresight.archaeology.infra :as infra]))

(def run-id "00000000-0000-0000-0000-000000000001")
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
                                    "00000000-0000-0000-0000-000000000099")
                nil
                (catch #?(:clj Exception :cljs :default) error error))]
    (is (= :unknown-run (:archaeology/error (ex-data error))))))

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
