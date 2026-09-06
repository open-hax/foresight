;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns project-test
  (:require [cljs.test :as test :refer [deftest is testing]]
            [clojure.string :as str]
            [foresight.law.project :as law]
            [foresight.onboarding :as onboarding]
            [foresight.project :as project]
            [workspace :as workspace]))

(deftest declares-the-source-constellation
  (let [sources (:project/sources project/project)
        actual-submodules (workspace/current-submodules)
        declared-submodules (project/submodule-sources)]
    (is (= (set (map :path actual-submodules))
           (set (map :source/path declared-submodules))))
    (is (= (set (map :url actual-submodules))
           (set (map :source/url declared-submodules))))
    (is (= #{".agents" "eta"}
           (set (map :source/path (project/consolidation-inputs)))))
    (is (= #{:alpha :archaeology :eta}
           (set (map :component/id (:project/native-components project/project)))))
    (is (= (count sources)
           (+ (count declared-submodules)
              (count (remove #(= :git-submodule (:source/type %)) sources)))))))

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
  (let [changed (update project/project :project/sources
                        (fn [sources]
                          (mapv (fn [source]
                                  (if (= :eta (:source/id source))
                                    (-> source
                                        (assoc :source/actionable? true)
                                        (assoc :source/consolidation? false))
                                    source))
                                sources)))
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
    (is (not (law/confined-relative-path? path)) (pr-str path))))

(deftest every-executable-invariant-has-an-implemented-check
  (doseq [invariant (:project/invariants project/project)
          :when (= :executable (:invariant/enforcement invariant))]
    (testing (str (:invariant/id invariant))
      (is (contains? law/executable-checks (:invariant/check invariant))))))

(deftest onboarding-routes-cover-sources-and-native-components
  (let [routes (onboarding/route-rows project/project)
        rendered (onboarding/markdown project/project)
        source-paths (set (map :source/path (:project/sources project/project)))
        component-paths (set (map :component/path
                                  (:project/native-components project/project)))
        expected-paths (into source-paths component-paths)
        by-path (into {} (map (juxt :route/path identity)) routes)]
    (is (= expected-paths (set (map :route/path routes))))
    (is (= (count expected-paths) (count routes)))
    (doseq [{:source/keys [path repository]} (:project/sources project/project)]
      (is (str/includes? rendered (str "`" path "`")) path)
      (when repository
        (is (str/includes? rendered repository) repository)))
    (doseq [{:component/keys [path role]} (:project/native-components project/project)]
      (is (str/includes? rendered (str "`" path "`")) path)
      (is (= role (:route/native-role (get by-path path))) path))
    (is (= "open-hax/foresight" (:route/repository (get by-path "alpha"))))
    (is (= "open-hax/foresight" (:route/repository (get by-path "archaeology"))))
    (is (= "open-hax/foresight" (:route/repository (get by-path "eta"))))
    (is (= :clojure-harness (:route/source-role (get by-path "eta"))))
    (is (= :transduction-harness (:route/native-role (get by-path "eta"))))
    (is (false? (:route/execution-root? (get by-path "eta"))))
    (is (nil? (:route/execution-root? (get by-path "alpha"))))))

(deftest onboarding-history-is-revision-bound
  (is (seq onboarding/precedents))
  (doseq [{:precedent/keys [repository pr revision lesson]}
          onboarding/precedents]
    (is (= "open-hax/foresight" repository))
    (is (and (integer? pr) (pos? pr)))
    (is (some? (re-matches #"[0-9a-f]{40}" revision)) revision)
    (is (and (string? lesson) (not (str/blank? lesson))))))

(defn test-exit-code [summary]
  (if (test/successful? summary) 0 1))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (test-exit-code summary)))

(test/run-tests 'project-test)
