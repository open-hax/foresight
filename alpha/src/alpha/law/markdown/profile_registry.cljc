(ns alpha.law.markdown.profile-registry
  (:require [alpha.law.markdown.profile-registry-validation :as validation]
            [alpha.law.markdown.selection :as selection]))

(defn conflicts [registries]
  (->> registries
       (mapcat keys)
       frequencies
       (keep (fn [[id n]] (when (> n 1) id)))
       (sort-by str)
       vec))

(defn compose [& registries]
  (let [errors (vec (mapcat #(-> % validation/validate :errors) registries))
        duplicate-ids (conflicts registries)]
    (cond
      (seq errors) {:ok false :reason :invalid-registry :errors errors}
      (seq duplicate-ids) {:ok false :reason :profile-id-conflict :conflicts duplicate-ids}
      :else {:ok true :registry (apply merge {} registries) :conflicts []})))

(defn select
  ([registry document] (select registry document nil))
  ([registry document context]
   (let [checked (validation/validate registry)]
     (if (:ok checked)
       (selection/select (vec (vals registry)) document context)
       {:ok false :reason :invalid-registry :errors (:errors checked)}))))
