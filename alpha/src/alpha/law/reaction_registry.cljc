(ns alpha.law.reaction-registry
  (:require [alpha.law.reaction :as reaction]
            [alpha.law.reaction-registry-shape :as shape]
            [katamorph.action.registry :as action-registry]))

(defn conflicts [registries]
  (->> registries (mapcat keys) frequencies
       (keep (fn [[id n]] (when (> n 1) id))) (sort-by str) vec))

(defn compose [& registries]
  (let [errors (vec (mapcat #(-> % shape/validate :errors) registries))
        duplicates (conflicts registries)]
    (cond
      (seq errors) {:ok false :reason :invalid-registry :errors errors}
      (seq duplicates) {:ok false :reason :reaction-id-conflict :conflicts duplicates}
      :else {:ok true :registry (apply merge {} registries) :conflicts []})))

(defn binding-errors [operations registry]
  (->> registry
       (keep (fn [[id value]]
               (let [operation-id (get-in value [:reaction/do :operation/id])]
                 (when-not (action-registry/resolve-action operations operation-id)
                   {:law/id :reaction/operation-unregistered
                    :reaction/id id
                    :operation/id operation-id}))))
       (sort-by #(str (:reaction/id %))) vec))

(defn select [operations registry event subject-artifact]
  (let [checked (shape/validate registry)
        actions (action-registry/validate operations)
        bindings (when (and (:ok checked) (:ok actions))
                   (binding-errors operations registry))
        reactions (->> registry vals (sort-by #(str (:reaction/id %))) vec)]
    (cond
      (not (:ok checked))
      {:ok false :reason :invalid-registry :errors (:errors checked)}

      (not (:ok actions))
      {:ok false :reason :invalid-action-registry :errors (:errors actions)}

      (seq bindings)
      {:ok false :reason :unbound-reaction-registry :errors bindings}

      :else
      {:ok true
       :reactions (reaction/select operations reactions event subject-artifact)})))
