(ns alpha.law.reaction-registry-shape
  (:require [alpha.law.artifact :as artifact]))

(defn errors [registry]
  (->> registry
       (mapcat (fn [[id reaction]]
                 (cond-> []
                   (not= id (:reaction/id reaction))
                   (conj {:law/id :reaction/registry-id-mismatch
                          :registry/id id
                          :reaction/id (:reaction/id reaction)})
                   (not (artifact/reaction-shape? reaction))
                   (conj {:law/id :reaction/invalid-shape
                          :registry/id id}))))
       (sort-by #(str (:registry/id %)))
       vec))

(defn validate [registry]
  (let [problems (errors registry)]
    {:ok (empty? problems) :registry registry :errors problems}))
