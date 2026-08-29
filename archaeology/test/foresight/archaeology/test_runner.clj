;; SPDX-License-Identifier: GPL-3.0-or-later
(ns foresight.archaeology.test-runner
  (:require [clojure.test :as test]
            [foresight.archaeology.domain-test]))

(defn -main [& _]
  (let [summary (test/run-tests 'foresight.archaeology.domain-test)]
    (shutdown-agents)
    (System/exit (if (test/successful? summary) 0 1))))
