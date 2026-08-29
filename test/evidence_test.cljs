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
      {:gate/id :repo/live
       :gate/kind :live-smoke
       :gate/execution :external
       :gate/source "repo/.github/workflows/deploy.yml"
       :gate/reason "Requires the target host"}]}}})

(def test-catalog-identity
  {:catalog/path "config/quality-gates.edn"
   :catalog/sha256 (apply str (repeat 64 "a"))})

(defn recorded-result [gate-id outcome revision]
  {:gate/id gate-id
   :result/outcome outcome
   :result/execution :local
   :result/command ["tool" "test"]
   :result/catalog test-catalog-identity
   :result/source {:source/path "repo/package.json"
                   :source/repository "repo"
                   :source/revision revision}
   :result/revision revision})

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
                       [:catalog/repositories "repo" :repository/gates 1]
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
                                     "revision-b")))))))
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
  (is (= :unavailable (:result/outcome (evidence/summarize-results [])))))

(deftest invalid-result-data-fails-the-summary
  (let [summary (evidence/summarize-results
                 [(recorded-result :repo/unit :unknown "revision-a")])]
    (is (= :failed (:result/outcome summary)))
    (is (false? (:result/satisfied? summary)))
    (is (= :result/outcome (get-in summary [:result/errors 0 :error])))))

(deftest promotion-is-closed-over-required-gates
  (let [revision "abc123"
        passed [(recorded-result :repo/unit :passed revision)
                (recorded-result :repo/integration :passed revision)]]
    (is (evidence/promotion-ready?
         revision #{:repo/unit :repo/integration} passed))
    (is (false? (evidence/promotion-ready?
                 revision #{:repo/unit :repo/e2e} passed)))
    (is (false? (evidence/promotion-ready?
                 revision
                 #{:repo/unit}
                 [(recorded-result :repo/unit :unavailable revision)])))))

(deftest promotion-requires-one-unambiguous-target-revision
  (let [unit (recorded-result :repo/unit :passed "revision-a")
        integration (recorded-result :repo/integration :passed "revision-b")]
    (is (false? (evidence/promotion-ready?
                 "revision-a" #{:repo/unit :repo/integration}
                 [unit integration])))
    (is (false? (evidence/promotion-ready?
                 "" #{:repo/unit} [unit])))
    (is (false? (evidence/promotion-ready?
                 "revision-a" #{:repo/unit} [unit unit])))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (if (test/successful? summary) 0 1)))

(test/run-tests 'evidence-test)
