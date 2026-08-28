(ns foresight.river-city.domain-test
  (:require [clojure.test :refer [deftest is]]
            [foresight.river-city.shape :as shape]
            [river-city.domain.portwatch :as portwatch-domain]
            [river-city.shape.portwatch :as portwatch]))

(def resource
  {:namespace :foresight.river-city
   :resources
   [{:document/id :source-imf-portwatch
     :document/type :foresight.river-city/source
     :river-city/source :source/imf-portwatch
     :river-city/schema
     {:catalog/path ".ημ/river-city/catalog.edn"
      :history/path ".ημ/river-city/schemas"}
     :river-city/ledgers
     {:observations
      {:ledger/path ".ημ/river-city/ledgers/imf-portwatch.edn"
       :ledger/event-type :river-city/portwatch-observed}}
     :river-city/projection :river-city/maritime-portwatch}]})

(def observation
  {:source/id :source/imf-portwatch
   :source/record-id 42
   :source/date 1787875200000
   :port/id 7
   :port/name "Strait of Hormuz"
   :vessels {:total 10}
   :capacity {:total 900000}})

(defn event [id seq data]
  {:event/id id
   :event/type portwatch/event-type
   :event/stream (portwatch-domain/stream-id data)
   :event/seq seq
   :event/data data})

(deftest resource-is-reference-only-and-valid
  (is (shape/valid-resource? resource))
  (is (= :river-city/portwatch-observed
         (get-in resource [:resources 0 :river-city/ledgers :observations :ledger/event-type]))))

(deftest correction-projection-is-validated
  (let [old (event "00000000-0000-4000-8000-000000000001" 1 observation)
        corrected (event "00000000-0000-4000-8000-000000000002"
                         2 (assoc-in observation [:vessels :total] 11))
        projection (portwatch-domain/project [old corrected])]
    (is (shape/valid-projection? projection))
    (is (= 2 (count (:source-events projection))))
    (is (= 1 (count (:rows projection))))
    (is (= 11 (get-in projection [:rows 0 :vessels :total])))))
