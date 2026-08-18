;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns project-test
  (:require [cljs.test :as test :refer [deftest is testing]]
            [foresight.law.project :as law]
            [foresight.project :as project]
            [workspace :as workspace]))

(deftest declares-the-source-constellation
  (let [sources (:project/sources project/project)]
    (is (= 14 (count sources)))
    (is (= 13 (count (project/submodule-sources))))
    (is (= 2 (count (project/consolidation-inputs))))
    (is (= 12 (count (filter :source/actionable? sources))))
    (is (= #{:alpha :eta}
           (set (map :component/id (:project/native-components project/project)))))
    (is (= #{:agents :truth :bitch-tracker :calliope :epiphany :eta-mu
             :katamorph :knoxx :muse :opencode :proxx :services :uxx :eta}
           (set (map :source/id sources))))))

(deftest declared-project-is-structurally-lawful
  (let [result (law/validate-project project/project)]
    (is (:valid? result))
    (is (empty? (:errors result)))))

(deftest checked-in-gitmodules-match-project-law
  (let [result (law/validate-project project/project (workspace/current-submodules))]
    (is (:valid? result) (pr-str (:errors result)))))

(deftest gitmodule-drift-is-visible
  (let [actual (assoc-in (project/gitmodule-declarations) [0 :url]
                         "git@example.invalid/changed.git")
        result (law/validate-project project/project actual)]
    (is (false? (:valid? result)))
    (is (some #(= :foresight/gitmodule-url-matches (:law/id %))
              (:errors result)))))

(deftest duplicate-source-paths-are-illegal
  (let [first-path (get-in project/project [:project/sources 0 :source/path])
        changed (assoc-in project/project [:project/sources 1 :source/path] first-path)
        result (law/validate-project changed)]
    (is (false? (:valid? result)))
    (is (some #(= :foresight/source-path-unique (:law/id %))
              (:errors result)))))

(deftest source-invariant-references-must-resolve
  (let [changed (update-in project/project [:project/sources 0 :source/invariants]
                           conj :missing/law)
        result (law/validate-project changed)]
    (is (false? (:valid? result)))
    (is (some #(and (= :foresight/source-invariant-resolves (:law/id %))
                    (= :missing/law (:actual %)))
              (:errors result)))))

(deftest actionable-authority-is-declarative-and-narrow
  (let [changed (-> project/project
                    (assoc-in [:project/sources 13 :source/actionable?] true)
                    (assoc-in [:project/sources 13 :source/consolidation?] false))
        result (law/validate-project changed)]
    (is (false? (:valid? result)))
    (is (some #(= :foresight/actionable-source-is-independent-submodule
                  (:law/id %))
              (:errors result)))))

(deftest project-path-law-matches-workspace-intent
  (doseq [path ["eta" ".agents" "nested/repo" "nested\\repo"]]
    (is (law/confined-relative-path? path) path))
  (doseq [path [nil "" " " "." ".." "../repo" "repo/../other"
                "/absolute" "\\absolute" "C:\\absolute" "nested//repo"
                "nested/./repo" "nested/repo/"]]
    (is (false? (boolean (law/confined-relative-path? path))) (pr-str path))))

(deftest every-executable-invariant-has-an-implemented-check
  (doseq [invariant (:project/invariants project/project)
          :when (= :executable (:invariant/enforcement invariant))]
    (testing (str (:invariant/id invariant))
      (is (contains? law/executable-checks (:invariant/check invariant))))))

(defn test-exit-code [summary]
  (if (test/successful? summary) 0 1))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (test-exit-code summary)))

(test/run-tests 'project-test)
