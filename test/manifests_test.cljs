;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns manifests-test
  (:require [cljs.test :as test :refer [deftest is]]
            [manifests :as manifests]))

(deftest selected-version-conflicts-are-actionable
  (let [declarations [{:name "compiler" :version "1.0.0" :path "a/package.json"}
                      {:name "compiler" :version "2.0.0" :path "b/package.json"}]]
    (is (thrown? js/Error (manifests/resolve-dependencies declarations {})))
    (is (thrown? js/Error (manifests/resolve-dependencies declarations {"compiler" {:version "2.0.0"}})))
    (is (= {"compiler" "2.0.0"}
           (manifests/resolve-dependencies declarations
                                          {"compiler" {:version "2.0.0" :reason "Reviewed root tool compatibility"}})))))

(deftest repeated-manifest-names-remain-visible
  (is (= [{:name "same" :paths ["a/package.json" "b/package.json"]}]
         (manifests/duplicate-names [{:path "a/package.json" :manifest {"name" "same"}}
                                    {:path "b/package.json" :manifest {"name" "same"}}]))))

(deftest serialization-is-independent-of-map-insertion-order
  (is (= (manifests/edn-text {:z {:b 1 :a 2} :a [3]})
         (manifests/edn-text {:a [3] :z {:a 2 :b 1}})))
  (is (= (manifests/json-text {"z" 3 "a" {"b" 1 "a" 2}})
         (manifests/json-text {"a" {"a" 2 "b" 1} "z" 3}))))

(deftest real-generated-artifacts-detect-drift
  (let [generated (manifests/generate)]
    (is (= 6 (count generated)))
    (is (empty? (manifests/drift generated)))
    (is (= ["package.json"]
           (manifests/drift (update generated "package.json" str "\n"))))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (if (test/successful? summary) 0 1)))
(test/run-tests 'manifests-test)
