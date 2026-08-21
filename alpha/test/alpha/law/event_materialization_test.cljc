(ns alpha.law.event-materialization-test
  (:require [alpha.law.artifact-observation :as observation]
            [alpha.law.event-materialization :as materialization]
            [clojure.test :refer [deftest is]]))

(deftest caller-supplies-event-identity-and-time
  (let [artifact {:artifact/id :document/1
                  :artifact/kind :document}
        event (materialization/materialize
               (observation/observed artifact)
               :event/1
               "2026-08-13T23:00:00Z")]
    (is (= :event/1 (:event/id event)))
    (is (= "2026-08-13T23:00:00Z" (:event/at event)))
    (is (= :artifact/observed (:event/type event)))))
