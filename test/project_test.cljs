;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns project-test
  (:require [cljs.test :as test :refer [deftest is testing]]
            [clojure.string :as str]
            [foresight.law.project :as law]
            [foresight.law.dev-origins :as dev-origins]
            [foresight.onboarding :as onboarding]
            [foresight.project :as project]
            [clojure.edn :as edn]
            ["crypto" :as crypto]
            ["fs" :as fs]
            [workspace :as workspace]))

(def network-roots
  (edn/read-string (fs/readFileSync "config/dev-origin-network-roots.edn" "utf8")))

(deftest extracted-children-have-routing-and-gate-entries
  (let [agents (fs/readFileSync "AGENTS.md" "utf8")
        catalog (edn/read-string (fs/readFileSync "config/quality-gates.edn" "utf8"))]
    (doseq [path ["kanban-orchestrator" "clio" "chat-ui" "rheos"
                 "session-mycology" "sol" "osmos" "receipt-river" "axxium"
                   "openplanner"]]
      (is (str/includes? agents (str "| `" path "` | open-hax/" path " |")) path)
      (is (seq (get-in catalog [:catalog/repositories path :repository/gates])) path))))

(deftest declares-the-source-constellation
  (let [sources (:project/sources project/project)
        actual-submodules (workspace/current-submodules)
        declared-submodules (project/submodule-sources)]
    (is (= (set (map :path actual-submodules))
           (set (map :source/path declared-submodules))))
    (is (= (set (map :url actual-submodules))
           (set (map :source/url declared-submodules))))
    (is (= #{".agents" "clobber" "eta"}
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

(deftest dev-origin-map-law
  (let [modules [{:path "shx" :url "git@github.com:octave-commons/shx.git"}
                 {:path "opencode" :url "git@github.com:open-hax/opencode.git"}]
        roots {"open-hax/opencode" "anomalyco/opencode"}
        required #{"shx" "opencode"}
        rows [{:source/path "shx" :org/upstream "octave-commons/shx"
               :dev/origin "riatzukiza/shx" :network/root "octave-commons/shx"}
              {:source/path "opencode" :org/upstream "open-hax/opencode"
               :dev/origin "riatzukiza/mojomast-opencode"
               :network/root "anomalyco/opencode"}]]
    (is (:valid? (dev-origins/validate rows modules roots required)))
    (doseq [[changed expected]
            [[(pop rows) :dev-origin/missing]
             [(conj rows (assoc (first rows) :source/path "extra")) :dev-origin/unknown-submodule]
             [(assoc-in rows [1 :dev/origin] "riatzukiza/shx") :dev-origin/duplicate-fork]
             [(assoc-in rows [1 :network/root] "octave-commons/shx") :dev-origin/duplicate-network]
             [(assoc-in rows [1 :org/upstream] "open-hax/missing") :dev-origin/upstream-mismatch]
             [(assoc-in rows [0 :dev/origin] "open-hax/shx") :dev-origin/personal-owner]]]
      (is (some #(= expected (:law/id %)) (:errors (dev-origins/validate changed modules roots required)))
          (str expected)))))

(deftest dev-origin-coverage-is-keyed-by-path-not-owner
  ;; Codex PRRT_kwDOT3TWPM6rqMQW: once .gitmodules points a module at its
  ;; personal dev origin, the row must still be required and accepted.
  (let [roots {"open-hax/opencode" "anomalyco/opencode"}
        required #{"shx" "opencode"}
        rows [{:source/path "shx" :org/upstream "octave-commons/shx"
               :dev/origin "riatzukiza/shx" :network/root "octave-commons/shx"}
              {:source/path "opencode" :org/upstream "open-hax/opencode"
               :dev/origin "riatzukiza/mojomast-opencode"
               :network/root "anomalyco/opencode"}]
        dev-modules [{:path "shx" :url "git@github.com:riatzukiza/shx.git"}
                     {:path "opencode" :url "https://github.com/riatzukiza/mojomast-opencode.git"}]
        ids (fn [rows modules]
              (set (map :law/id (:errors (dev-origins/validate rows modules roots required)))))]
    (testing "dev-origin URLs satisfy their rows"
      (is (:valid? (dev-origins/validate rows dev-modules roots required))))
    (testing "a dev-pointed module whose row is removed is still missing (CodeRabbit PRRT_kwDOU4VJMM6u4m7H)"
      ;; Required paths come from the manifest, not from the rows themselves.
      (is (contains? (ids (pop rows) dev-modules) :dev-origin/missing)))
    (testing "an org-pointed module without a row is still missing beside dev-pointed ones"
      (is (contains? (ids (pop rows) (assoc-in dev-modules [1 :url] "git@github.com:open-hax/opencode.git"))
                     :dev-origin/missing)))
    (testing "a row whose path is not a submodule is still unknown"
      (is (contains? (ids (conj rows (assoc (first rows) :source/path "extra")) dev-modules)
                     :dev-origin/unknown-submodule)))
    (testing "a module pointing at neither upstream nor dev origin is a mismatch"
      (is (contains? (ids rows (assoc-in dev-modules [0 :url] "git@github.com:riatzukiza/other.git"))
                     :dev-origin/upstream-mismatch)))
    (testing "an unrelated personal module without a row is not an org module"
      (is (:valid? (dev-origins/validate
                    rows (conj dev-modules {:path ".agents" :url "git@github.com:riatzukiza/.agents.git"})
                    roots required))))
    (testing "a nested canonical path cannot cover itself through its own row (CodeRabbit PRRT_kwDOU4VJMM6u5uIo)"
      ;; .agents points at riatzukiza/.agents; a row naming that origin must
      ;; not turn the excluded path into covered coverage.
      (is (contains? (ids (conj rows {:source/path ".agents" :org/upstream "open-hax/.agents"
                                      :dev/origin "riatzukiza/.agents" :network/root "open-hax/.agents"})
                          (conj dev-modules {:path ".agents" :url "git@github.com:riatzukiza/.agents.git"}))
                     :dev-origin/unknown-submodule)))))

(deftest dev-origin-network-root-is-checked-against-supplied-facts
  ;; Codex PRRT_kwDOT3TWPM6rqMQa / CodeRabbit PRRT_kwDOT3TWPM6uiWm8.
  (let [modules [{:path "shx" :url "git@github.com:octave-commons/shx.git"}
                 {:path "opencode" :url "git@github.com:open-hax/opencode.git"}]
        roots {"open-hax/opencode" "anomalyco/opencode"}
        required #{"shx" "opencode"}
        rows [{:source/path "shx" :org/upstream "octave-commons/shx"
               :dev/origin "riatzukiza/shx" :network/root "octave-commons/shx"}
              {:source/path "opencode" :org/upstream "open-hax/opencode"
               :dev/origin "riatzukiza/mojomast-opencode"
               :network/root "anomalyco/opencode"}]
        ids (fn [rows roots]
              (set (map :law/id (:errors (dev-origins/validate rows modules roots required)))))]
    (testing "a well-formed but wrong root is refused"
      (is (contains? (ids (assoc-in rows [0 :network/root] "someone/shx") roots)
                     :dev-origin/network-root-mismatch)))
    (testing "the opencode exception comes from the supplied facts, not the law"
      (is (contains? (ids rows {}) :dev-origin/network-root-mismatch))
      (is (contains? (ids (assoc-in rows [1 :network/root] "open-hax/opencode") roots)
                     :dev-origin/network-root-mismatch)))
    (testing "the checked-in facts keep only the opencode exception"
      (is (= {"open-hax/opencode" "anomalyco/opencode"} network-roots)))))

(deftest checked-in-dev-origins-cover-current-submodules
  (let [rows (edn/read-string (fs/readFileSync "config/dev-origins.edn" "utf8"))
        result (dev-origins/validate rows (workspace/current-submodules) network-roots
                                     (project/dev-origin-required-paths))]
    (is (:valid? result) (pr-str (:errors result)))
    (is (= 23 (count rows)))
    (is (= "riatzukiza/mojomast-opencode"
           (:dev/origin (first (filter #(= "opencode" (:source/path %)) rows)))))))

(deftest dev-origin-required-paths-come-from-the-project-model
  ;; Independently owned submodules need a row; the nested canonical .agents does not.
  (let [required (project/dev-origin-required-paths)]
    (is (= 23 (count required)))
    (is (not (contains? required ".agents")))
    (is (contains? required "opencode"))))

(def kanban-org-base
  "open-hax/foresight base fcfc2d17f28640203066ddfe0a22f87db7372a32: its
  .ημ/kanban-events/ledger.edn blob, which fork history must keep as a prefix."
  {:commit "fcfc2d17f28640203066ddfe0a22f87db7372a32"
   :blob "2b5d16ddab10b50a786eb9f121bba29e9194e0e7"
   :bytes 143351})

(defn- git-blob-id [buffer]
  (-> (crypto/createHash "sha1")
      (.update (str "blob " (.-length buffer) "\u0000"))
      (.update buffer)
      (.digest "hex")))

(deftest kanban-ledger-preserves-the-org-base-as-a-prefix
  ;; CodeRabbit PRRT_kwDOT3TWPM6uiWm1: fork events go after the base history.
  (let [ledger (fs/readFileSync ".ημ/kanban-events/ledger.edn")
        {:keys [blob bytes]} kanban-org-base]
    (is (<= bytes (.-length ledger)))
    (is (= blob (git-blob-id (.subarray ledger 0 bytes))))))

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
