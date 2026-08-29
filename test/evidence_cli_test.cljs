;; SPDX-License-Identifier: GPL-3.0-or-later
(ns evidence-cli-test
  (:require [cljs.test :as test :refer [deftest is]]
            [evidence :as cli]
            [foresight.evidence :as law]))

(deftest parses-explicit-repository-and-kind-selection
  (is (= {:only #{"katamorph"}
          :kinds #{:unit :static}}
         (cli/parse-args ["--only" "katamorph"
                          "--kind" "unit,static"])))
  (is (thrown-with-msg? js/Error #"Unknown gate kinds"
                        (cli/parse-args ["--kind" "pretend-e2e"]))))

(deftest validates-the-checked-in-catalog
  (let [catalog (cli/read-catalog)]
    (is (law/valid-catalog? catalog))
    (is (identical? catalog (cli/validate-catalog! catalog)))))

(deftest local-process-results-do-not-hide-failures
  (with-redefs [cli/spawn-result (fn [& _] #js {:status 0})]
    (is (= :passed
           (:result/outcome
            (cli/local-result "/repo" {:gate/id :repo/unit
                                        :gate/command ["test"]})))))
  (with-redefs [cli/spawn-result (fn [& _] #js {:status 7})]
    (is (= {:gate/id :repo/unit
            :result/outcome :failed
            :result/exit 7}
           (cli/local-result "/repo" {:gate/id :repo/unit
                                      :gate/command ["test"]})))))

(deftest missing-checkouts-and-external-hosts-remain-non-passes
  (is (= :unavailable
         (:result/outcome
          (cli/run-gate! {:unavailable-reason "not initialized"}
                         {:gate/id :repo/unit
                          :gate/kind :unit
                          :gate/execution :local
                          :gate/command ["test"]}))))
  (is (= :blocked
         (:result/outcome
          (cli/run-gate! {}
                         {:gate/id :repo/live
                          :gate/kind :live-smoke
                          :gate/execution :external
                          :gate/reason "needs host"})))))

(deftest process-exit-contract-keeps-non-passes-nonzero
  (is (zero? (cli/result-exit {:result/outcome :passed})))
  (is (= 1 (cli/result-exit {:result/outcome :failed})))
  (is (= 3 (cli/result-exit {:result/outcome :unavailable})))
  (is (= 4 (cli/result-exit {:result/outcome :blocked}))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (if (test/successful? summary) 0 1)))

(test/run-tests 'evidence-cli-test)
