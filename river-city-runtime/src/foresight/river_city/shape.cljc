(ns foresight.river-city.shape
  (:require [clio.law.schema :as clio-schema]
            [foresight.river-city.law :as law]
            [malli.core :as m]
            [river-city.domain.portwatch :as portwatch-domain]
            [river-city.shape.portwatch :as portwatch]))

(def nonblank-string
  [:string {:min 1}])

(def uuid-string
  [:string {:min 36 :max 36}])

(def catalog
  {portwatch/event-type
   (clio-schema/event-schema portwatch/event-type portwatch/observation-data)})

(def ledger-ref
  [:map {:closed true}
   [:ledger/path nonblank-string]
   [:ledger/event-type :keyword]])

(def source-resource-entry
  [:map {:closed true}
   [:document/id :keyword]
   [:document/type [:= :foresight.river-city/source]]
   [:river-city/source [:= portwatch/source-id]]
   [:river-city/schema
    [:map {:closed true}
     [:catalog/path nonblank-string]
     [:history/path nonblank-string]]]
   [:river-city/ledgers
    [:map {:closed true}
     [:observations ledger-ref]]]
   [:river-city/projection [:= portwatch-domain/projection-type]]])

(def resource-file
  [:map {:closed true}
   [:namespace [:= :foresight.river-city]]
   [:resources [:tuple source-resource-entry]]])

(def projection-row
  [:merge portwatch/observation-data
   [:map {:closed true}
    [:event/id uuid-string]
    [:event/stream nonblank-string]
    [:event/seq [:int {:min 1}]]]])

(def portwatch-projection
  [:map {:closed true}
   [:projection/type [:= portwatch-domain/projection-type]]
   [:source-events [:vector uuid-string]]
   [:rows [:vector projection-row]]])

(defn valid-resource?
  [value]
  (and (m/validate resource-file value)
       (law/resource-valid? value)))

(defn valid-projection?
  [value]
  (m/validate portwatch-projection value))
