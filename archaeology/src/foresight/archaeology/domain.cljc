(ns foresight.archaeology.domain
  (:require [clio.domain.canonicalize :as canonicalize]
            [clio.domain.projection :as projection]
            [foresight.archaeology.law :as law]
            [foresight.archaeology.shape :as shape]))

(def empty-state
  {:run-events {}
   :findings {}
   :actions {}
   :evidence {}
   :relations {}})

(defn- index-event
  [state bucket event]
  (assoc-in state [bucket (:event/id event)] event))

(defn apply-event
  "Pure archaeology fold over Clio's canonical event order. Run events are
   indexed by semantic run id; all repeatable facts stay indexed by immutable
   event id so later runs may reuse stable finding/action/evidence ids without
   overwriting historical facts."
  [state event]
  (let [data (:event/data event)]
    (case (:event/type event)
      :archaeology/run-recorded
      (assoc-in state [:run-events (:run/id data)] event)

      :archaeology/finding-recorded
      (index-event state :findings event)

      :archaeology/action-recorded
      (index-event state :actions event)

      :archaeology/evidence-recorded
      (index-event state :evidence event)

      :archaeology/relation-recorded
      (index-event state :relations event)

      state)))

(defn canonical-history
  "Clio is authoritative for partition union, historical schema validation,
   id/stream conflicts, missing causes, cycle detection, and deterministic order."
  [revisions ledgers]
  (canonicalize/canonicalize revisions ledgers))

(defn history-state
  [canonical]
  (projection/state canonical empty-state apply-event))

(defn- run-subject
  [run-id]
  (str "archaeology:" run-id))

(defn- events-for-run
  [state bucket run-id]
  (let [subject (run-subject run-id)]
    (->> (get state bucket)
         vals
         (filter #(= subject (:event/subject %))))))

(defn- relation-data
  [state run-id kind]
  (->> (events-for-run state :relations run-id)
       (map :event/data)
       (filter #(= kind (:relation/kind %)))))

(defn- finding-evidence
  [state run-id]
  (reduce
   (fn [index relation]
     (update index
             (:relation/from-id relation)
             (fnil conj #{})
             (:relation/to-id relation)))
   {}
   (relation-data state run-id :finding/evidence)))

(defn- consumes-from-relations
  "A :run/consumes relation uses scalar endpoints in storage; the convenient
   continuity vector is reconstructed here."
  [state run-id]
  (->> (relation-data state run-id :run/consumes)
       (filter #(= run-id (:relation/from-id %)))
       (map (fn [relation]
              {:event/id (str (:relation/to-id relation))
               :action/id (:relation/action-id relation)
               :relation (:relation/status relation)}))
       (sort-by (juxt :event/id (comp str :action/id)))
       vec))

(defn compose-run
  "Build the convenient aggregate shape for one run from normalized ledgers.
   Every collection in the result is derived. Historical facts from other runs
   remain in the canonical history but are excluded by :event/subject."
  [state run-id]
  (let [run-event (get-in state [:run-events run-id])]
    (when-not run-event
      (throw (ex-info "Unknown archaeology run"
                      {:archaeology/error :unknown-run
                       :run/id run-id})))
    (let [run-data (:event/data run-event)
          evidence-by-finding (finding-evidence state run-id)
          findings (->> (events-for-run state :findings run-id)
                        (map :event/data)
                        (map (fn [finding]
                               (assoc finding
                                      :evidence
                                      (->> (get evidence-by-finding (:finding/id finding) #{})
                                           (sort-by str)
                                           vec))))
                        (sort-by (comp str :finding/id))
                        vec)
          actions (->> (events-for-run state :actions run-id)
                       (map :event/data)
                       (sort-by (comp str :action/id))
                       vec)
          evidence (->> (events-for-run state :evidence run-id)
                        (map :event/data)
                        (sort-by (comp str :evidence/id))
                        vec)
          result {:event/id run-id
                  :event/parents (vec (:event/causes run-event))
                  :archaeology/topic (:run/topic run-data)
                  :archaeology/summary (:run/summary run-data)
                  :archaeology/consumes (consumes-from-relations state run-id)
                  :archaeology/findings findings
                  :archaeology/actions actions
                  :evidence/refs evidence}]
      (when-not (shape/valid-run-projection? result)
        (throw (ex-info "Derived archaeology projection violates its shape or laws"
                        {:archaeology/error :invalid-projection
                         :run/id run-id
                         :projection result})))
      result)))

(defn project-run
  [revisions ledgers run-id]
  (-> (canonical-history revisions ledgers)
      history-state
      (compose-run run-id)))

(defn ledger-paths
  "Stable role order for infrastructure callers; file order has no semantic authority."
  [resource]
  (mapv (fn [role]
          (get-in resource [:archaeology/ledgers role :ledger/path]))
        [:run :findings :actions :evidence :relations]))

(defn validate-local-ledgers!
  [resource role->events]
  (doseq [[role events] role->events]
    (when-not (law/ledger-role-valid? role events)
      (throw (ex-info "Ledger contains an event of the wrong semantic role"
                      {:archaeology/error :ledger-role-mismatch
                       :role role
                       :events events}))))
  resource)
