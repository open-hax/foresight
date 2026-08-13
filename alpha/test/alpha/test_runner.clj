(ns alpha.test-runner
  (:require [alpha.law.artifact-test]
            [clojure.test :as test]))

(defn -main
  [& _]
  (let [{:keys [fail error]} (test/run-tests 'alpha.law.artifact-test)]
    (when (pos? (+ fail error))
      (System/exit 1))))
