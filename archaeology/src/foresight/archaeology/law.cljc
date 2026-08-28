(ns foresight.archaeology.law
  (:require [clio.law.event :as clio-event]))

(def ledger-event-types
  {:run :archaeology/run-recorded
   :findings :archaeology/finding-recorded
   :actions :archaeology/action-recorded
   :evidence :archaeology/evidence-recorded
   :relations :archaeology/relation-recorded})

(def required-ledger-roles
  (set (keys ledger-event-types)))

(def derived-collection-keys
  #{:event/parents
    :archaeology/consumes
    :archaeology/findings
    :archaeology/actions
    :evidence/refs})

(defn resource-ledger-roles-valid?
  "A run resource names exactly the logical ledgers that compose its projection."
  [resource]
  (= required-ledger-roles
     (set (keys (:archaeology/ledgers resource)))))

(defn unique-ledger-paths?
  "One resource may not alias two semantic ledger roles to the same file."
  [resource]
  (let [paths (map :ledger/path (vals (:archaeology/ledgers resource)))]
    (= (count paths) (count (set paths)))))

(defn reference-only-resource?
  "Persisted Katamorph resources contain references and projection identity,
   never materialized ledger collections."
  [resource]
  (not-any? #(contains? resource %) derived-collection-keys))

(defn event-belongs-to-role?
  [role event]
  (and (= (get ledger-event-types role) (:event/type event))
       (clio-event/event-identity-valid? event)))

(defn ledger-role-valid?
  "Every physical line in one semantic ledger has the role's event type.
   Stream continuity, duplicate ids, missing causes, and cycles remain Clio laws."
  [role events]
  (every? #(event-belongs-to-role? role %) events))

(defn resource-valid?
  [resource]
  (and (resource-ledger-roles-valid? resource)
       (unique-ledger-paths? resource)
       (reference-only-resource? resource)))

(defn parent-continuity-valid?
  "Every parent run must be addressed by at least one semantic continuity
   relation. Root runs have no parents and therefore need no continuity rows."
  [projection]
  (let [parents (set (:event/parents projection))
        consumed-parents (->> (:archaeology/consumes projection)
                              (map :event/id)
                              set)]
    (or (empty? parents)
        (every? consumed-parents parents))))
