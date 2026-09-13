;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns manifests-test
  (:require [cljs.test :as test :refer [deftest is]]
            [cljs.reader :as edn]
            [manifests :as manifests]
            [foresight.project :as project]
            [workspace :as workspace]
            ["node:fs" :as fs]
            ["node:os" :as os]
            ["node:path" :as path]))

(defn- write-fixture! [directory file text]
  (let [target (path/join directory file)]
    (fs/mkdirSync (path/dirname target) #js {:recursive true})
    (fs/writeFileSync target text)))

(defn- git! [directory & args]
  (let [result (workspace/run-captured directory
                                      (into ["git" "-c" "user.name=Fixture" "-c" "user.email=fixture@example.invalid"] args))]
    (when-not (zero? (:exit result)) (throw (ex-info "Fixture Git command failed" result)))
    (:stdout result)))

(defn- with-composition-fixture [f]
  (let [directory (fs/mkdtempSync (path/join (os/tmpdir) "foresight-manifests-"))
        child (path/join directory "eta-mu")
        policy {:node-projects ["devtools" "eta-mu/packages/clio"]
                :clio "eta-mu/packages/clio" :package-manager "pnpm@10.14.0"
                :local-libraries {'io.github.open-hax/clio "eta-mu/packages/clio"}}
        sources [{:source/path "eta-mu" :source/actionable? true}]]
    (try
      (doseq [[file content] {"workspace.edn" (pr-str policy)
                             ".gitmodules" "[submodule \"eta-mu\"]\npath = eta-mu\nurl = https://example.invalid/eta-mu.git\n"
                             "scripts/manifests.cljs" "; fixture generator input\n"
                             "src/foresight/project.cljc" "; fixture source input\n"
                             "devtools/package.json" "{\"name\":\"devtools\"}"
                             "eta-mu/packages/clio/package.json" "{\"name\":\"clio\"}"
                             "eta-mu/packages/clio/deps.edn" "{:paths [\"src\"] :deps {}}"
                             "eta-mu/packages/clio/nbb.edn" "{:paths [\"src\"] :deps {}}"
                             "eta-mu/packages/clio/src/example.cljc" "(ns example)\n"
                             "eta-mu/examples/unselected/package.json" "{\"name\":\"unselected\"}"}]
        (write-fixture! directory file content))
      (git! directory "init" "--quiet")
      (git! child "init" "--quiet")
      (git! child "add" ".")
      (git! child "commit" "--quiet" "-m" "Fixture source")
      (let [sha (git! child "rev-parse" "HEAD")]
        (git! directory "update-index" "--add" "--cacheinfo" "160000" sha "eta-mu"))
      (with-redefs [manifests/root directory project/submodule-sources (constantly sources)]
        (f {:directory directory :child child :sources sources}))
      (finally (fs/rmSync directory #js {:recursive true :force true})))))

(deftest composition-does-not-require-or-read-unselected-packages
  (with-composition-fixture
    (fn [{:keys [directory sources]}]
      (with-redefs [project/submodule-sources
                    (constantly (conj sources {:source/path "uninitialized" :source/actionable? true}))]
        (let [before (manifests/generate)
              metadata (edn/read-string (get before "workspace-manifests.edn"))]
          (is (= ["devtools/package.json" "eta-mu/packages/clio/package.json"]
                 (mapv :path (:packages metadata))))
          (write-fixture! directory "eta-mu/examples/unselected/package.json" "{\"name\":\"changed-unselected\"}")
          (is (= before (manifests/generate))))))))

(deftest composition-refuses-selected-source-and-gitlink-drift
  (with-composition-fixture
    (fn [{:keys [directory child]}]
      (is (map? (manifests/generate)))
      (write-fixture! directory "eta-mu/packages/clio/src/example.cljc" "(ns changed)\n")
      (is (thrown? js/Error (manifests/generate)) "Uncommitted consumed source must fail")
      (git! child "add" ".")
      (git! child "commit" "--quiet" "-m" "Unpromoted child change")
      (is (thrown? js/Error (manifests/generate)) "A clean different child commit must fail")
      (git! directory "update-index" "--add" "--cacheinfo" "160000" (git! child "rev-parse" "HEAD") "eta-mu")
      (is (map? (manifests/generate)))
      (write-fixture! directory "eta-mu/packages/clio/src/shadow.cljs" "(ns shadow)\n")
      (is (thrown? js/Error (manifests/generate)) "Untracked consumed source must fail"))))

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

(deftest local-composition-overrides-the-consumers-actual-library-symbols
  (let [generated (manifests/generate)
        deps (edn/read-string (get generated "deps.edn"))
        metadata (edn/read-string (get generated "workspace-manifests.edn"))
        expected {'io.github.open-hax/clio {:local/root "eta-mu/packages/clio"}
                  'io.github.open-hax/axxium {:local/root "eta-mu/packages/axxium"}}]
    (is (= expected (get-in deps [:aliases :local :extra-deps])))
    (is (= expected (get-in deps [:aliases :local :override-deps])))
    (is (contains? (:inputs metadata) "eta-mu/packages/axxium/deps.edn"))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (if (test/successful? summary) 0 1)))
(test/run-tests 'manifests-test)
