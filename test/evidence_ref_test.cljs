;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns evidence-ref-test
  (:require [cljs.test :as test :refer [deftest is testing]]
            [foresight.law.evidence-ref :as law]
            [foresight.shape.evidence-ref :as evidence-ref]))

(deftest source-neutral-reference-fixtures
  (doseq [reference
          [(evidence-ref/git-object
            {:authority :epiphany
             :repository "open-hax/foresight"
             :revision "abc123"
             :blob-oid "def456"
             :path "docs/history.md"
             :heading-path ["History"]
             :section-ordinal 2
             :extractor-version "markdown-v1"
             :epistemic-tier :observed})
           (evidence-ref/rheos-card :rheos :foresight
                                    "054e1f6f-9186-4101-bbad-6666affb6925")
           (evidence-ref/rheos-event :rheos :foresight "event-42")
           (evidence-ref/rheos-workflow :rheos :promethean "v1")
           (evidence-ref/clio-event :clio "agent-events" "01J-event")
           (evidence-ref/skill :skill-catalog "session-mycology" "rev-7")
           (evidence-ref/skill-graph-node :skill-catalog "agent-skills" "rheos" "rev-7")]]
    (is (law/valid-reference? reference) (pr-str reference))))

(deftest local-path-never-defines-identity
  (let [reference {:evidence-ref/version 1
                   :evidence-ref/kind :rheos/card
                   :evidence-ref/authority :rheos
                   :evidence-ref/identity {:board-id :foresight
                                           :card-id "card-1"
                                           :path "docs/agile/kanban/card.md"}
                   :evidence-ref/freshness {:status :unknown}}
        errors (law/reference-errors reference)]
    (is (some #(= :evidence-ref/local-path-not-identity (:law/id %)) errors))))

(deftest selectors-may-carry-non-authoritative-placement
  (let [reference (evidence-ref/git-object
                   {:authority :epiphany
                    :repository "open-hax/foresight"
                    :revision "abc123"
                    :path "README.md"})]
    (is (= {:path "README.md"} (:evidence-ref/selector reference)))
    (is (not (contains? (:evidence-ref/identity reference) :path)))))

(deftest resolver-statuses-remain-distinct
  (let [reference (evidence-ref/rheos-card :rheos :foresight "card-1")]
    (is (law/valid-resolver-outcome?
         (evidence-ref/resolver-outcome :resolved reference :value {:title "Card"})))
    (doseq [status [:stale :unavailable :unsupported]]
      (testing (name status)
        (is (law/valid-resolver-outcome?
             (evidence-ref/resolver-outcome
              status reference
              :diagnostic {:code (keyword "resolver" (name status))})))))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (if (test/successful? summary) 0 1)))

(test/run-tests 'evidence-ref-test)
