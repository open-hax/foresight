(ns alpha.law.markdown.profile-registry-validation
  (:require [alpha.law.markdown.selection :as selection]))

(defn errors [registry]
  (->> registry
       (mapcat
        (fn [[registry-id profile]]
          (cond-> []
            (not= registry-id (:profile/id profile))
            (conj {:law/id :profile/registry-id-mismatch
                   :registry/id registry-id
                   :profile/id (:profile/id profile)})
            (not (selection/valid-profile? profile))
            (conj {:law/id :profile/invalid-profile
                   :registry/id registry-id}))))
       (sort-by #(str (:registry/id %)))
       vec))

(defn validate [registry]
  (let [problems (errors registry)]
    {:ok (empty? problems)
     :registry registry
     :errors problems}))
