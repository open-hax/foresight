(ns foresight.river-city.domain
  (:require [clio.domain.canonicalize :as canonicalize]
            [foresight.river-city.law :as law]
            [foresight.river-city.shape :as shape]
            [river-city.domain.portwatch :as portwatch]))

(defn canonical-history
  "Clio owns partition union, historical schema validation, causal/stream laws,
   deduplication, and deterministic replay order."
  [revisions ledgers]
  (canonicalize/canonicalize revisions ledgers))

(defn project-portwatch
  "Derive River City's current maritime view from a Clio-canonical event set."
  [revisions ledgers]
  (let [canonical (canonical-history revisions ledgers)
        projection (portwatch/project (:canonical/events canonical))]
    (when-not (shape/valid-projection? projection)
      (throw (ex-info "Derived River City projection violates its shape"
                      {:river-city/error :invalid-projection
                       :projection projection})))
    projection))

(defn validate-local-ledgers!
  [resource role->events]
  (doseq [[role events] role->events]
    (when-not (law/ledger-role-valid? role events)
      (throw (ex-info "River City ledger contains an event of the wrong role"
                      {:river-city/error :ledger-role-mismatch
                       :role role
                       :events events}))))
  resource)
