(ns alpha.law.markdown.profile-registry
  (:require [alpha.law.markdown.profile-registry-validation :as validation]
            [alpha.law.markdown.selection :as selection]))

(defn conflicts [registries]
  (->> registries
       (mapcat keys)
       frequencies
       (keep (fn [[profile-id n]]
               (when (> n 1) profile-id)))
       (sort-by str)
       vec))

(defn compose [& registries]
  (let [invalid-errors (mapcat #(-> % validation/validate :errors) registries)
        duplicate-ids (conflicts registries)]
    (cond
      (seq invalid-errors)
      {:ok false :reason :invalid-registry :errors (vec invalid-errors)}

      (seq duplicate-ids)
      {:ok false :reason :profile-id-conflict :conflicts duplicate-ids}

      :else
      {:ok true :registry (apply merge registries) :conflicts []})))

(defn select
  ([registry document] (select registry document nil))
  ([registry document caller-context]
   (let [checked (validation/validate registry)]
     (if-not (:ok checked)
       {:ok false :reason :invalid-registry :errors (:errors checked)}
       (selection/select (vec (vals registry)) document caller-context)))))
