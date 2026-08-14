(ns alpha.law.event-materialization-test
  (:require [alpha.law.artifact-observation :as observation]
            [alpha.law.event-materialization :as materialization]
            [clojure.test :refer [deftest is testing]]))

(def artifact
  {:artifact/id :document/1
   :artifact/kind :document})

(def draft
  (observation/observed artifact))

(deftest caller-supplies-event-identity-and-time
  (let [outcome (materialization/result
                 draft
                 :event/1
                 "2026-08-13T23:00:00Z")
        event (:event outcome)]
    (is (:ok outcome))
    (is (= :event/1 (:event/id event)))
    (is (= "2026-08-13T23:00:00Z" (:event/at event)))
    (is (= :artifact/observed (:event/type event)))
    (is (= event
           (materialization/materialize
            draft :event/1 "2026-08-13T23:00:00Z")))))

(deftest runtime-time-is-required-at-materialization
  (doseq [at [nil "" "   "]]
    (let [outcome (materialization/result draft :event/1 at)]
      (is (false? (:ok outcome)))
      (is (= :invalid-event-time (:reason outcome)))
      (is (nil? (materialization/materialize draft :event/1 at))))))

(deftest runtime-identity-must-satisfy-the-event-id-law
  (let [outcome (materialization/result draft {:not :an-id} "2026-08-13T23:00:00Z")]
    (is (false? (:ok outcome)))
    (is (= :invalid-event-id (:reason outcome)))))

(deftest runtime-fields-cannot-arrive-inside-the-draft
  (testing "draft identity stays outside the pure draft"
    (let [outcome (materialization/result
                   (assoc draft :event/id :already-in-draft)
                   :event/1
                   "2026-08-13T23:00:00Z")]
      (is (false? (:ok outcome)))
      (is (= :invalid-event-draft (:reason outcome)))))
  (testing "draft time stays outside the pure draft"
    (let [outcome (materialization/result
                   (assoc draft :event/at "2026-08-13T22:00:00Z")
                   :event/1
                   "2026-08-13T23:00:00Z")]
      (is (false? (:ok outcome)))
      (is (= :invalid-event-draft (:reason outcome))))))
