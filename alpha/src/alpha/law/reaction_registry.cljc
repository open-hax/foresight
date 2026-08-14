(ns alpha.law.reaction-registry
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.reaction :as reaction]
            [alpha.law.reaction-registry-shape :as shape]))

(defn conflicts [registries]
  (->> registries
       (mapcat keys)
       frequencies
       (keep (fn [[id n]] (when (> n 1) id)))
       (sort-by str)
       vec))

(defn compose [& registries]
  (let [errors (vec (mapcat #(-> % shape/validate :errors) registries))
        duplicate-ids (conflicts registries)]
    (cond
      (seq errors) {:ok false :reason :invalid-registry :errors errors}
      (seq duplicate-ids) {:ok false :reason :reaction-id-conflict :conflicts duplicate-ids}
      :else {:ok true :registry (apply merge {} registries) :conflicts []})))

(defn binding-errors [operations registry]
  (->> registry
       (keep (fn [[id value]]
               (when-not (artifact/reaction? operations value)
                 {:law/id :reaction/operation-unregistered
                  :reaction/id id
                  :operation/id (get-in value [:reaction/do :operation/id])})))
       (sort-by #(str (:reaction/id %)))
       vec))

(defn select [operations registry event subject-artifact]
  (let [checked (shape/validate registry)
        binding-errors (when (:ok checked) (binding-errors operations registry))]
    (cond
      (not (:ok checked)) {:ok false :reason :invalid-registry :errors (:errors checked)}
      (seq binding-errors) {:ok false :reason :unbound-reaction-registry :errors binding-errors}
      :else {:ok true
             :reactions (reaction/select operations (vec (vals registry)) event subject-artifact)})))
