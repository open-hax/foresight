(ns foresight.river-city.test-runner
  (:require [clojure.test :as test]
            [foresight.river-city.domain-test]))

(defn -main [& _]
  (let [result (test/run-tests 'foresight.river-city.domain-test)]
    (when (pos? (+ (:fail result) (:error result)))
      (System/exit 1))))
