;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns evidence-test
  (:require [cljs.test :as test :refer [deftest is]]
            [foresight.evidence :as evidence]))

(def valid-catalog
  {:catalog/version 1
   :catalog/repositories
   {"repo"
    {:repository/path "repo"
     :repository/gates
     [{:gate/id :repo/unit
       :gate/kind :unit
       :gate/execution :local
       :gate/command ["tool" "test"]
       :gate/source "repo/README.md"}
      {:gate/id :repo/integration
       :gate/kind :integration
       :gate/execution :local
       :gate/command ["tool" "integration"]
       :gate/source "repo/README.md"}
      {:gate/id :repo/live
       :gate/kind :live-smoke
       :gate/execution :external
       :gate/source "repo/.github/workflows/deploy.yml"
       :gate/reason "Requires the target host"}]}}})

(def test-catalog-identity
  {:catalog/path "config/quality-gates.edn"
   :catalog/sha256 (apply str (repeat 64 "a"))})

(defn recorded-result [gate-id outcome revision]
  (let [gate (or (some #(when (= gate-id (:gate/id %)) %)
                       (get-in valid-catalog
                               [:catalog/repositories "repo"
                                :repository/gates]))
                 {:gate/execution :local
                  :gate/command ["tool" "test"]
                  :gate/source "repo/README.md"})]
    (cond-> {:gate/id gate-id
             :result/outcome outcome
             :result/execution (:gate/execution gate)
             :result/catalog test-catalog-identity
             :result/source {:source/path (:gate/source gate)
                             :source/repository "repo"
                             :source/revision revision}
             :result/revision revision}
      (:gate/command gate)
      (assoc :result/command (:gate/command gate))

      (and (= :local (:gate/execution gate))
           (= :passed outcome))
      (assoc :result/exit 0))))

(deftest validates-gate-catalogs
  (is (evidence/valid-catalog? valid-catalog))
  (is (= :gate/command
         (:error
          (first
           (evidence/catalog-errors
            (assoc-in valid-catalog
                      [:catalog/repositories "repo" :repository/gates 0 :gate/command]
                      ["tool" ""]))))))
  (is (= :gate/reason
         (:error
          (first
           (evidence/catalog-errors
            (update-in valid-catalog
                       [:catalog/repositories "repo" :repository/gates 2]
                       dissoc :gate/reason)))))))

(deftest rejects-duplicate-gate-identities
  (let [duplicate (assoc-in valid-catalog
                            [:catalog/repositories "other"]
                            {:repository/path "other"
                             :repository/gates
                             [(get-in valid-catalog
                                      [:catalog/repositories "repo"
                                       :repository/gates 0])]})]
    (is (= {:error :gate/id-duplicates :gate/ids [:repo/unit]}
           (last (evidence/catalog-errors duplicate))))))

(deftest malformed-gate-collections-return-structured-errors
  (let [malformed (assoc-in valid-catalog
                            [:catalog/repositories "repo" :repository/gates]
                            42)]
    (is (= :repository/gates
           (:error (first (evidence/catalog-errors malformed)))))
    (is (false? (evidence/valid-catalog? malformed)))
    (is (false? (evidence/promotion-ready?
                 malformed test-catalog-identity "revision-a"
                 #{:repo/unit}
                 [(recorded-result :repo/unit :passed "revision-a")])))))

(deftest catalog-repositories-must-be-actionable-direct-submodules
  (is (empty? (evidence/catalog-inventory-errors valid-catalog #{"repo"})))
  (is (= [{:error :catalog/repository-not-actionable-submodule
           :repository "repo"}]
         (evidence/catalog-inventory-errors valid-catalog #{"other"}))))

(deftest selects-repositories-and-kinds
  (is (= [:repo/unit]
         (mapv :gate/id
               (evidence/select-gates valid-catalog ["repo"] #{:unit}))))
  (is (= [] (evidence/select-gates valid-catalog ["missing"] #{:unit}))))

(deftest unavailable-and-blocked-never-pass
  (doseq [outcome [:failed :blocked :unavailable]]
    (is (false? (evidence/satisfied? {:result/outcome outcome}))))
  (is (false? (evidence/satisfied? {:result/outcome :not-applicable
                                    :result/reason "No browser surface"})))
  (is (evidence/satisfied? {:result/outcome :not-applicable
                            :result/reason "No browser surface"
                            :result/approved-by "review/42"})))

(deftest validates-result-outcomes-and-not-applicable-approval
  (let [passed (recorded-result :repo/unit :passed "revision-a")]
    (is (evidence/valid-result? passed))
    (is (= :result/command
           (:error (first (evidence/result-errors
                           (dissoc passed :result/command))))))
    (is (= :result/catalog
           (:error (first (evidence/result-errors
                           (assoc-in passed
                                     [:result/catalog :catalog/sha256]
                                     "not-a-digest"))))))
    (is (= :result/source-revision
           (:error (first (evidence/result-errors
                           (assoc-in passed
                                     [:result/source :source/revision]
                                     "revision-b"))))))
    (is (= :result/local-passed-exit
           (:error (first (filter #(= :result/local-passed-exit (:error %))
                                  (evidence/result-errors
                                   (assoc passed :result/exit 7)))))))
    (is (= :result/local-nonpass-exit
           (:error (first (filter #(= :result/local-nonpass-exit (:error %))
                                  (evidence/result-errors
                                   (assoc passed
                                          :result/outcome :failed))))))))
  (is (= :result/outcome
         (:error (first (evidence/result-errors
                         (recorded-result :repo/unit :greenish
                                          "revision-a"))))))
  (is (= :result/not-applicable-approval
         (:error (first (evidence/result-errors
                         (assoc (recorded-result :repo/e2e
                                                :not-applicable
                                                "revision-a")
                                :result/reason "No user surface")))))))

(deftest summary-keeps-the-strongest-non-pass-visible
  (is (= {:result/outcome :blocked
          :result/counts {:blocked 1 :passed 1 :unavailable 1}
          :result/satisfied? false}
         (evidence/summarize-results
          [(recorded-result :repo/unit :passed "revision-a")
           (recorded-result :repo/e2e :unavailable "revision-a")
           (recorded-result :repo/live :blocked "revision-a")])))
  (is (= {:result/outcome :unavailable
          :result/counts {}
          :result/satisfied? false}
         (evidence/summarize-results [])))
  (is (= {:result/outcome :failed
          :result/counts {}
          :result/satisfied? false
          :result/errors [{:error :results/type :results 42}]}
         (evidence/summarize-results 42))))

(deftest invalid-result-data-fails-the-summary
  (let [summary (evidence/summarize-results
                 [(recorded-result :repo/unit :unknown "revision-a")])]
    (is (= :failed (:result/outcome summary)))
    (is (false? (:result/satisfied? summary)))
    (is (= :result/outcome (get-in summary [:result/errors 0 :error])))))

(deftest malformed-result-values-return-structured-errors
  (is (= [{:error :result/type :result 42}]
         (evidence/result-errors 42)))
  (is (false? (evidence/valid-result? 42)))
  (let [summary (evidence/summarize-results [42])]
    (is (= :failed (:result/outcome summary)))
    (is (= :result/type (get-in summary [:result/errors 0 :error])))))

(deftest promotion-is-closed-over-required-gates
  (let [revision "abc123"
        passed [(recorded-result :repo/unit :passed revision)
                (recorded-result :repo/integration :passed revision)]]
    (is (evidence/promotion-ready?
         valid-catalog test-catalog-identity revision
         #{:repo/unit :repo/integration} passed))
    (is (false? (evidence/promotion-ready?
                 valid-catalog test-catalog-identity revision
                 #{:repo/unit :repo/e2e} passed)))
    (is (false? (evidence/promotion-ready?
                 valid-catalog test-catalog-identity revision
                 #{:repo/unit}
                 [(recorded-result :repo/unit :unavailable revision)])))
    (is (false? (evidence/promotion-ready?
                 valid-catalog test-catalog-identity revision #{} [])))
    (is (false? (evidence/promotion-ready?
                 valid-catalog test-catalog-identity revision 42 [])))
    (is (false? (evidence/promotion-ready?
                 valid-catalog test-catalog-identity revision #{} 42)))))

(deftest promotion-requires-one-unambiguous-target-revision
  (let [unit (recorded-result :repo/unit :passed "revision-a")
        integration (recorded-result :repo/integration :passed "revision-b")]
    (is (false? (evidence/promotion-ready?
                 valid-catalog test-catalog-identity "revision-a"
                 #{:repo/unit :repo/integration}
                 [unit integration])))
    (is (false? (evidence/promotion-ready?
                 valid-catalog test-catalog-identity
                 "" #{:repo/unit} [unit])))
    (is (false? (evidence/promotion-ready?
                 valid-catalog test-catalog-identity
                 "revision-a" #{:repo/unit} [unit unit])))))

(deftest promotion-results-must-match-the-trusted-catalog-snapshot
  (let [revision "revision-a"
        result (recorded-result :repo/unit :passed revision)
        ready? #(evidence/promotion-ready?
                 valid-catalog test-catalog-identity revision
                 #{:repo/unit} [%])]
    (is (ready? result))
    (is (false? (ready? (assoc result :result/command ["true"]))))
    (is (false? (ready? (assoc result :result/exit 7))))
    (is (false? (ready? (assoc result :result/execution :external))))
    (is (false? (ready? (assoc result
                               :result/catalog
                               (assoc test-catalog-identity
                                      :catalog/sha256
                                      (apply str (repeat 64 "b")))))))
    (is (false? (ready? (assoc-in result
                                  [:result/source :source/path]
                                  "repo/forged.edn"))))
    (is (false? (ready? (assoc-in result
                                  [:result/source :source/repository]
                                  "other"))))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (if (test/successful? summary) 0 1)))

(test/run-tests 'evidence-test)
