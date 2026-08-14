(ns alpha.law.artifact-event-test
  (:require [alpha.law.artifact-change :as change]
            [alpha.law.artifact-observation :as observation]
            [alpha.law.event-draft :as draft]
            [clojure.test :refer [deftest is]]))

(def before
  {:artifact/id :document/id-1
   :artifact/kind :document
   :artifact/status :draft})

(def after
  (assoc before :artifact/status :review))

(deftest observation-is-a-valid-draft
  (is (draft/valid? (observation/observed before))))

(deftest changes-are-identity-bound
  (let [result (change/derive before after)]
    (is (:ok result))
    (is (:changed? result))
    (is (= [:artifact/status]
           (get-in result [:event :event/data :artifact/changed-keys]))))
  (is (= {:ok true :changed? false}
         (change/derive before before)))
  (is (= :identity-mismatch
         (:reason (change/derive before
                                 (assoc after :artifact/id :document/id-2))))))
