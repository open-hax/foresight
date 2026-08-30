;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns prompt-compiler-test
  (:require [cljs.test :as test :refer [deftest is]]
            [foresight.shape.evidence-ref :as evidence-ref]
            [foresight.shape.prompt-compiler :as compiler]))

(def card-ref
  (evidence-ref/rheos-card :rheos :foresight
                           "809785a6-213f-492a-a840-7174ffcdd865"))

(defn fragment [id contributor precedence content]
  {:prompt-fragment/version 1
   :prompt-fragment/id id
   :prompt-fragment/revision "v1"
   :prompt-fragment/scope :system
   :prompt-fragment/slot :identity
   :prompt-fragment/merge :append
   :prompt-fragment/contributor contributor
   :prompt-fragment/precedence precedence
   :prompt-fragment/order 0
   :prompt-fragment/conditions {:all [{:path [:target] :equals :codex}]}
   :prompt-fragment/targets #{:portable}
   :prompt-fragment/evidence-refs [card-ref]
   :prompt-fragment/content content})

(def fragments
  [(fragment :role/researcher {:kind :role :id "researcher"} 100 "Research carefully.")
   (fragment :actor/primary {:kind :actor :id "primary"} 200 "Act as the primary agent.")
   (fragment :skill/citations {:kind :skill :id "citations"} 300 "Cite evidence.")])

(def authority-decisions
  (mapv (fn [fragment']
          {:prompt-authority/version 1
           :prompt-authority/fragment-id (:prompt-fragment/id fragment')
           :prompt-authority/authority :axxium
           :prompt-authority/status :granted})
        fragments))

(deftest compiles-role-actor-skill-in-explicit-order
  (let [result (compiler/compile-prompt
                {:fragments fragments
                 :authority-decisions authority-decisions
                 :context {:target :codex}
                 :target :codex
                 :token-budget 100})]
    (is (= :compiled (:prompt-compilation/status result)))
    (is (= "Research carefully.\n\nAct as the primary agent.\n\nCite evidence."
           (:prompt-compilation/system result)))
    (is (= [:role/researcher :actor/primary :skill/citations]
           (get-in result [:prompt-compilation/receipt
                           :rendered-prompt-receipt/order])))
    (is (not (contains? (:prompt-compilation/receipt result)
                        :rendered-prompt-receipt/content)))))

(deftest refuses-missing-or-ambiguous-authority
  (let [result (compiler/compile-prompt
                {:fragments fragments
                 :authority-decisions (conj authority-decisions
                                            (first authority-decisions))
                 :context {:target :codex}
                 :target :codex})]
    (is (= :rejected (:prompt-compilation/status result)))
    (is (= :prompt/unauthorized
           (get-in result [:prompt-compilation/diagnostics 0 :diagnostic/code])))
    (is (= [{:prompt-fragment/id :role/researcher
             :reason :authority-not-granted}]
           (get-in result [:prompt-compilation/receipt
                           :rendered-prompt-receipt/excluded])))))

(deftest reports-exclusive-slot-conflicts
  (let [exclusive (mapv #(assoc % :prompt-fragment/merge :exclusive)
                        (take 2 fragments))
        result (compiler/compile-prompt
                {:fragments exclusive
                 :authority-decisions (take 2 authority-decisions)
                 :context {:target :codex}
                 :target :codex})]
    (is (= :rejected (:prompt-compilation/status result)))
    (is (= :prompt/conflict
           (get-in result [:prompt-compilation/diagnostics 0 :diagnostic/code])))))

(deftest reports-budget-without-dropping-fragments
  (let [result (compiler/compile-prompt
                {:fragments fragments
                 :authority-decisions authority-decisions
                 :context {:target :codex}
                 :target :codex
                 :token-budget 2})]
    (is (= :over-budget (:prompt-compilation/status result)))
    (is (= 3 (count (get-in result [:prompt-compilation/receipt
                                    :rendered-prompt-receipt/fragments]))))
    (is (= :prompt/over-budget
           (get-in result [:prompt-compilation/diagnostics 0 :diagnostic/code])))))

(deftest records-condition-and-target-exclusions
  (let [target-only (assoc (first fragments)
                           :prompt-fragment/targets #{:claude})
        inactive (assoc (second fragments)
                        :prompt-fragment/conditions
                        {:all [{:path [:target] :equals :opencode}]})
        result (compiler/compile-prompt
                {:fragments [target-only inactive]
                 :authority-decisions (take 2 authority-decisions)
                 :context {:target :codex}
                 :target :codex})]
    (is (= :compiled (:prompt-compilation/status result)))
    (is (= #{{:prompt-fragment/id :role/researcher
              :reason :target-incompatible}
             {:prompt-fragment/id :actor/primary
              :reason :condition-not-held}}
           (set (get-in result [:prompt-compilation/receipt
                                :rendered-prompt-receipt/excluded]))))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (if (test/successful? summary) 0 1)))

(test/run-tests 'prompt-compiler-test)
