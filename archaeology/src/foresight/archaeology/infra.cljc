(ns foresight.archaeology.infra
  (:require [clio.domain.schema :as clio-schema]
            [foresight.archaeology.domain :as domain]
            [foresight.archaeology.law :as law]
            [foresight.archaeology.shape :as shape]))

(defn current-revision
  "Materialize the current Clio schema revision with the caller's SHA-256
   function. The hash function accepts canonical EDN text and returns hex."
  [sha256]
  (clio-schema/materialize sha256 shape/catalog))

(defn assert-resource!
  [resource-file]
  (when-not (shape/valid-resource? resource-file)
    (throw (ex-info "Invalid archaeology Katamorph resource file"
                    {:archaeology/error :invalid-resource
                     :resource resource-file})))
  resource-file)

(defn read-resource-ledgers
  "Portable infrastructure orchestration. `read-ledger` is normally
   clio.infra.ledger/read-ledger in NBB/Node. Keeping it injected lets this
   namespace remain CLJC and prevents a second filesystem implementation."
  [read-ledger resource-file]
  (assert-resource! resource-file)
  (let [entry (law/resource-entry resource-file)]
    (into {}
          (map (fn [[role {:ledger/keys [path]}]]
                 [role (read-ledger path)]))
          (:archaeology/ledgers entry))))

(defn resource-events
  [read-ledger resource-file]
  (let [role->events (read-resource-ledgers read-ledger resource-file)]
    (domain/validate-local-ledgers! resource-file role->events)
    (vec (vals role->events))))

(defn project-resources
  "Compose any number of Katamorph run resource files into one Clio history,
   then derive a target run. Resources are discovery/index objects; ledgers
   remain the immutable authority."
  [{:keys [read-ledger revisions]} resource-files run-id]
  (doseq [resource-file resource-files] (assert-resource! resource-file))
  (let [partitions (->> resource-files
                        (mapcat #(resource-events read-ledger %))
                        vec)]
    (domain/project-run revisions partitions run-id)))

(defn append-plan
  "Return the immutable information a host adapter needs before calling
   clio.infra.ledger/append-event!. This namespace deliberately does not write."
  [resource-file role event]
  (assert-resource! resource-file)
  (when-not (law/event-belongs-to-role? role event)
    (throw (ex-info "Event does not satisfy the requested ledger role"
                    {:archaeology/error :event-role-mismatch
                     :role role
                     :event event})))
  {:ledger/path (get-in (law/resource-entry resource-file)
                        [:archaeology/ledgers role :ledger/path])
   :event event})
