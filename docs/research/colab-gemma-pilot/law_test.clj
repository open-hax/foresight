;; SPDX-License-Identifier: GPL-3.0-or-later
(require '[clojure.test :refer [deftest is run-tests]])
(load-file (str (.getParent (java.io.File. *file*)) "/budget_law.cljc"))
(def good {:campaign-id research.colab-budget-law/campaign :spent 0 :balance 399 :initial-balance 399 :rate 0 :cleanup-verified true :telemetry-age-seconds 0})
(deftest budget-boundaries
  (is (:admit (research.colab-budget-law/admission good)))
  (doseq [change [{:spent 26.01} {:balance 1} {:balance 370} {:rate 1.01} {:cleanup-verified false} {:cleanup-verified "false"} {:telemetry-age-seconds 31} {:rate nil} {:spent -1} {:campaign-id "reset"}]]
    (is (false? (:admit (research.colab-budget-law/admission (merge good change)))) change))
  (is (:admit (research.colab-budget-law/admission (assoc good :spent 26))))
  (is (false? (:admit (research.colab-budget-law/admission (assoc good :spent 28))))))
(deftest promotion-boundaries
  (is (:promote (research.colab-budget-law/promotion {:stealth true :yoga true :same-inputs true :human-approved true})))
  (doseq [k [:stealth :yoga :same-inputs :human-approved]]
    (is (false? (:promote (research.colab-budget-law/promotion (assoc {:stealth true :yoga true :same-inputs true :human-approved true} k false)))))))
(deftest invalid-promotion-evidence
  (is (false? (:promote (research.colab-budget-law/promotion {:stealth "passed" :yoga true :same-inputs true :human-approved true})))))
(let [r (run-tests)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))
