(ns alpha.law.event-materialization-test
  (:require [alpha.law.artifact-observation :as observation]
            [alpha.law.event-materialization :as materialization]
            [clojure.test :refer [deftest is testing]]))

(def artifact
  {:artifact/id :document/id-1
   :artifact/kind :document})

(def draft
  (observation/observed artifact))

(deftest caller-supplies-event-identity-and-time
  (let [event (materialization/materialize
               draft
               :event/id-1
               "2026-08-13T23:00:00Z")]
    (is (= :event/id-1 (:event/id event)))
    (is (= "2026-08-13T23:00:00Z" (:event/at event)))
    (is (= :artifact/observed (:event/type event)))))
