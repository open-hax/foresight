(ns alpha.law.event-materialization
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.event-draft :as draft]
            [clojure.string :as str]
            [malli.core :as m]))

(defn result
  "Materialize a draft only when the runtime supplies lawful identity and time."
  [value event-id at]
  (cond
    (not (draft/valid? value))
    {:ok false :reason :invalid-event-draft}

    (not (m/validate artifact/Id event-id))
    {:ok false :reason :invalid-event-id :event/id event-id}

    (not (and (string? at) (not (str/blank? at))))
    {:ok false :reason :invalid-event-time :event/at at}

    :else
    (let [event (assoc value :event/id event-id :event/at at)]
      (if (artifact/event? event)
        {:ok true :event event}
        {:ok false :reason :invalid-materialized-event :event event}))))

(defn materialize
  "Compatibility convenience: return the materialized Event, or nil on failure."
  [value event-id at]
  (let [outcome (result value event-id at)]
    (when (:ok outcome)
      (:event outcome))))
