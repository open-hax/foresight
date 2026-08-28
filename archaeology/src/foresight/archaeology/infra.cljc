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
  [resource]
  (when-not (shape/valid-resource? resource)
    (throw (ex-info "Invalid archaeology Katamorph resource"
                    {:archaeology/error :invalid-resource
                     :resource resource})))
  resource)

(defn read-resource-ledgers
  "Portable infrastructure orchestration. `read-ledger` is normally
   clio.infra.ledger/read-ledger in NBB/Node. Keeping it injected lets this
   namespace remain CLJC and prevents a second filesystem implementation."
  [read-ledger resource]
  (assert-resource! resource)
  (into {}
        (map (fn [[role {:ledger/keys [path]}]]
               [role (read-ledger path)]))
        (:archaeology/ledgers resource)))

(defn resource-events
  [read-ledger resource]
  (let [role->events (read-resource-ledgers read-ledger resource)]
    (domain/validate-local-ledgers! resource role->events)
    (mapv identity (vals role->events))))

(defn project-resources
  "Compose any number of Katamorph run resources into one Clio history, then
   derive a target run. Resources are discovery/index objects; ledgers remain
   the immutable authority."
  [{:keys [read-ledger revisions]} resources run-id]
  (doseq [resource resources] (assert-resource! resource))
  (let [partitions (->> resources
                        (mapcat #(resource-events read-ledger %))
                        vec)]
    (domain/project-run revisions partitions run-id)))

(defn append-plan
  "Return the immutable information a host adapter needs before calling
   clio.infra.ledger/append-event!. This namespace deliberately does not write."
  [resource role event]
  (assert-resource! resource)
  (when-not (law/event-belongs-to-role? role event)
    (throw (ex-info "Event does not satisfy the requested ledger role"
                    {:archaeology/error :event-role-mismatch
                     :role role
                     :event event})))
  {:ledger/path (get-in resource [:archaeology/ledgers role :ledger/path])
   :event event})
