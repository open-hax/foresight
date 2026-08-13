(ns alpha.law.artifact-change
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.artifact-observation :as observation]))

(defn changed-keys [before after]
  (vec (sort-by str
                (filter #(not= (get before %) (get after %))
                        (set (concat (keys before) (keys after)))))))

(defn derive [before after]
  (cond
    (not (artifact/artifact? before)) {:ok false :reason :invalid-before}
    (not (artifact/artifact? after)) {:ok false :reason :invalid-after}
    (not= (:artifact/id before) (:artifact/id after)) {:ok false :reason :identity-mismatch}
    (= before after) {:ok true :changed? false}
    :else {:ok true
           :changed? true
           :event {:event/type :artifact/changed
                   :event/subject (observation/artifact-ref after)
                   :event/data {:artifact/kind (:artifact/kind after)
                                :artifact/changed-keys (changed-keys before after)
                                :artifact/previous-status (:artifact/status before)
                                :artifact/status (:artifact/status after)}}}))
