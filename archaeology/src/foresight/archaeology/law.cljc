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

(defn resource-entry
  "Archaeology resource files deliberately contain exactly one Katamorph
   resource entry, so the manifest's required :resources vector never becomes
   another accumulating ledger."
  [resource-file]
  (first (:resources resource-file)))

(defn resource-ledger-roles-valid?
  [resource-file]
  (= required-ledger-roles
     (set (keys (:archaeology/ledgers (resource-entry resource-file))))))

(defn unique-ledger-paths?
  "One resource may not alias two semantic ledger roles to the same file."
  [resource-file]
  (let [paths (map :ledger/path
                   (vals (:archaeology/ledgers (resource-entry resource-file))))]
    (= (count paths) (count (set paths)))))

(defn reference-only-resource?
  "Persisted Katamorph resource entries contain references and projection
   identity, never materialized archaeology collections."
  [resource-file]
  (let [entry (resource-entry resource-file)]
    (not-any? #(contains? entry %) derived-collection-keys)))

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
  [resource-file]
  (and (= 1 (count (:resources resource-file)))
       (resource-ledger-roles-valid? resource-file)
       (unique-ledger-paths? resource-file)
       (reference-only-resource? resource-file)))

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
