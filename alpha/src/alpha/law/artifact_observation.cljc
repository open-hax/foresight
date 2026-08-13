(ns alpha.law.artifact-observation
  (:require [alpha.law.artifact :as artifact]))

(defn artifact-ref [value]
  {:ref/type :artifact
   :ref/id (:artifact/id value)
   :artifact/kind (:artifact/kind value)})

(defn observed [value]
  (when (artifact/artifact? value)
    {:event/type :artifact/observed
     :event/subject (artifact-ref value)
     :event/data (cond-> {:artifact/kind (:artifact/kind value)}
                   (contains? value :artifact/status)
                   (assoc :artifact/status (:artifact/status value)))}))
