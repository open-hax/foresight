(ns foresight.river-city.infra
  (:require [clio.domain.schema :as clio-schema]
            [foresight.river-city.domain :as domain]
            [foresight.river-city.law :as law]
            [foresight.river-city.shape :as shape]))

(defn current-revision
  "Materialize the current River City Clio schema revision with a caller-supplied
   SHA-256 function."
  [sha256]
  (clio-schema/materialize sha256 shape/catalog))

(defn assert-resource!
  [resource-file]
  (when-not (shape/valid-resource? resource-file)
    (throw (ex-info "Invalid River City Katamorph resource file"
                    {:river-city/error :invalid-resource
                     :resource resource-file})))
  resource-file)

(defn read-resource-ledgers
  "Read referenced partitions using Clio's host reader supplied by the caller."
  [read-ledger resource-file]
  (assert-resource! resource-file)
  (let [entry (law/resource-entry resource-file)]
    (into {}
          (map (fn [[role {:ledger/keys [path]}]]
                 [role (read-ledger path)]))
          (:river-city/ledgers entry))))

(defn resource-events
  [read-ledger resource-file]
  (let [role->events (read-resource-ledgers read-ledger resource-file)]
    (domain/validate-local-ledgers! resource-file role->events)
    (vec (vals role->events))))

(defn project-resource
  [{:keys [read-ledger revisions]} resource-file]
  (let [partitions (resource-events read-ledger resource-file)]
    (domain/project-portwatch revisions partitions)))

(defn append-plan
  "Return the path/event pair a host adapter passes to Clio append-event!.
   River City never implements another persistence transport."
  [resource-file role event]
  (assert-resource! resource-file)
  (when-not (law/event-belongs-to-role? role event)
    (throw (ex-info "Event does not satisfy the requested River City ledger role"
                    {:river-city/error :event-role-mismatch
                     :role role
                     :event event})))
  {:ledger/path (get-in (law/resource-entry resource-file)
                        [:river-city/ledgers role :ledger/path])
   :event event})
