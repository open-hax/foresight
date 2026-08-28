(ns foresight.river-city.law
  (:require [clio.law.event :as clio-event]
            [river-city.shape.portwatch :as portwatch]))

(def ledger-event-types
  {:observations portwatch/event-type})

(def required-ledger-roles
  (set (keys ledger-event-types)))

(def derived-keys
  #{:river-city/rows
    :river-city/source-events})

(defn resource-entry
  [resource-file]
  (first (:resources resource-file)))

(defn resource-ledger-roles-valid?
  [resource-file]
  (= required-ledger-roles
     (set (keys (:river-city/ledgers (resource-entry resource-file))))))

(defn declared-ledger-event-types-valid?
  [resource-file]
  (every? (fn [[role ledger]]
            (= (get ledger-event-types role)
               (:ledger/event-type ledger)))
          (:river-city/ledgers (resource-entry resource-file))))

(defn unique-ledger-paths?
  [resource-file]
  (let [paths (map :ledger/path
                   (vals (:river-city/ledgers (resource-entry resource-file))))]
    (= (count paths) (count (set paths)))))

(defn reference-only-resource?
  [resource-file]
  (let [entry (resource-entry resource-file)]
    (not-any? #(contains? entry %) derived-keys)))

(defn event-belongs-to-role?
  [role event]
  (and (= (get ledger-event-types role) (:event/type event))
       (clio-event/event-identity-valid? event)))

(defn ledger-role-valid?
  [role events]
  (every? #(event-belongs-to-role? role %) events))

(defn resource-valid?
  [resource-file]
  (and (= 1 (count (:resources resource-file)))
       (resource-ledger-roles-valid? resource-file)
       (declared-ledger-event-types-valid? resource-file)
       (unique-ledger-paths? resource-file)
       (reference-only-resource? resource-file)))
