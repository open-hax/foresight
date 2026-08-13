(ns alpha.law.event-materialization
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.event-draft :as draft]))

(defn materialize [value event-id at]
  (when (draft/valid? value)
    (let [event (cond-> (assoc value :event/id event-id)
                  at (assoc :event/at at))]
      (when (artifact/event? event) event))))
