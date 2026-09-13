(ns workspace-test
  (:require [cljs.test :as test :refer [deftest is]]
            [workspace :as workspace]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]))

(deftest parses-gitmodules
  (is (= [{:name "one" :path "one" :url "git@example/one.git"}
          {:name "two" :path "nested/two" :url "git@example/two.git"}]
         (workspace/parse-gitmodules
          (str "[submodule \"one\"]\n"
               "  path = one\n"
               "  url = git@example/one.git\n"
               "[submodule \"two\"]\n"
               "  path = nested/two\n"
               "  url = git@example/two.git\n")))))

(deftest selects-package-manager
  (is (= "pnpm" (workspace/select-manager "pnpm@10.14.0" ["bun.lock" "pnpm-lock.yaml"])))
  (is (= "bun" (workspace/select-manager "bun@1.3.14-rc.1+build.7" ["bun.lock"])))
  (is (= "pnpm" (workspace/select-manager "pnpm@1.2.3-01alpha" ["pnpm-lock.yaml"])))
  (is (= "yarn" (workspace/select-manager "yarn@4.9.2" ["yarn.lock"])))
  (is (= "npm" (workspace/select-manager nil ["package-lock.json"])))
  (is (nil? (workspace/select-manager nil ["yarn.lock"])))
  (is (nil? (workspace/select-manager nil ["bun.lock" "pnpm-lock.yaml"])))
  (is (nil? (workspace/select-manager nil [])))
  (doseq [invalid ["deno@2.0.0" "pnpm" "pnpm@" "pnpm@garbage"
                   "pnpm@01.2.3" "pnpm@1.2.3-." "yarn@1.22.22" "" 42]]
    (is (nil? (workspace/select-manager invalid ["pnpm-lock.yaml"])))))

(deftest requires-frozen-install-lock
  (is (= ["pnpm" "install" "--frozen-lockfile"]
         (workspace/install-command {:manager "pnpm" :lockfiles ["pnpm-lock.yaml"]})))
  (is (= ["npm" "ci"]
         (workspace/install-command {:manager "npm" :lockfiles ["package-lock.json"]})))
  (is (nil? (workspace/install-command {:manager "pnpm" :lockfiles []}))))

(deftest executes-only-exact-scripts
  (is (= ["bun" "run" "test"]
         (workspace/script-command {:manager "bun" :scripts ["test" "typecheck"]} "test")))
  (is (nil? (workspace/script-command {:manager "bun" :scripts ["typecheck"]} "lint"))))

(deftest inventory-script-names-are-strings
  (is (every? string? (:scripts (workspace/repo-inventory {:path "opencode"})))))

(deftest inventories-declared-consolidation-inputs
  (let [repos (workspace/inventory)
        by-path (into {} (map (juxt :path identity)) repos)]
    (is (= 15 (count repos)))
    (is (= 15 (count (set (map :path repos)))))
    (is (= #{"git-submodule" "consolidation-input"}
           (set (map :source-type repos))))
    (is (= {:source-type "consolidation-input"
            :ownership "nested-git-canonical"
            :role "skill-catalog"
            :actionable false}
           (select-keys (by-path ".agents")
                        [:source-type :ownership :role :actionable])))
    (is (not (contains? (by-path ".agents") :skill-count)))
    (is (not (contains? (by-path ".agents") :provenance-lock)))
    (is (empty? (:manifests (by-path ".agents"))))
    (is (empty? (:manifests (by-path "eta"))))
    (is (nil? (:initialized (by-path "eta"))))
    (is (nil? (:manager (by-path ".agents"))))))

(deftest review-workflow-never-initializes-inventory-only-sources
  (let [workflow (fs/readFileSync
                  (path/join workspace/root ".github/workflows/eta-mu-review.yml")
                  "utf8")]
    (is (nil? (re-find #"[.]agents" workflow)))
    (is (re-find #"run_gate workspace_laws" workflow))))

(deftest selects-explicit-repositories
  (let [repos [{:path "one" :actionable true}
               {:path "two" :actionable true}
               {:path "catalog" :actionable false}]]
    (is (= (subvec (vec repos) 0 2)
           (workspace/select-repos repos {:all? true :only nil})))
    (is (= [{:path "two" :actionable true}]
           (workspace/select-repos repos {:all? false :only #{"two"}})))
    (is (thrown-with-msg? js/Error #"Choose exactly one"
                          (workspace/select-repos repos {:all? false :only nil})))
    (is (thrown-with-msg? js/Error #"Unknown repositories: missing"
                          (workspace/select-repos repos {:all? false :only #{"missing"}})))
    (is (thrown-with-msg? js/Error #"Inventory-only sources cannot execute actions: catalog"
                          (workspace/select-repos repos {:all? false :only #{"catalog"}})))
    (is (thrown-with-msg? js/Error #"Inventory-only sources cannot execute actions: catalog"
                          (workspace/select-repos repos {:all? false :only #{"one" "catalog"}})))
    (is (thrown-with-msg? js/Error #"No repositories selected"
                          (workspace/select-repos [] {:all? true :only nil})))))

(deftest rejects-empty-only-values
  (doseq [args [["--only"] ["--only" ""] ["--only" "   "] ["--only" " , , "]]]
    (is (thrown? js/Error (workspace/parse-args args)))))

(deftest git-command-failures-are-not-clean
  (with-redefs [workspace/plain-path-stat (constantly #js {})
                workspace/run-captured
                (fn [_ command]
                  (case (second command)
                    "rev-parse" {:exit 0 :stdout "abc" :stderr ""}
                    "branch" {:exit 1 :stdout "" :stderr "branch failed"}
                    "status" {:exit 1 :stdout "" :stderr "status failed"}))]
    (let [state (workspace/git-state "/repo")
          report (workspace/markdown-report [(merge {:path "repo"
                                                       :source-type "git-submodule"
                                                       :ownership "independent-repository"
                                                       :manifests []
                                                       :manager nil
                                                       :lockfiles []
                                                       :scripts []
                                                       :actionable true}
                                                      state)])]
      (is (= #{:branch :status} (set (keys (:git-errors state)))))
      (is (nil? (:dirty state)))
      (is (re-find #"error \(branch, status\)" report))
      (is (not (re-find #"clean" report))))))

(deftest rev-parse-failures-are-visible
  (with-redefs [workspace/plain-path-stat (constantly #js {})
                workspace/run-captured
                (fn [_ command]
                  (if (= "rev-parse" (second command))
                    {:exit 128 :stdout "" :stderr "corrupt repository"}
                    {:exit 0 :stdout "" :stderr ""}))]
    (let [state (workspace/git-state "/repo")]
      (is (false? (:initialized state)))
      (is (= "corrupt repository" (get-in state [:git-errors :head]))))))

(deftest spawn-errors-fail-actions
  (with-redefs [workspace/execution-paths!
                (fn [repos] (mapv #(hash-map :repo % :absolute (path/join workspace/root (:path %))) repos))
                workspace/spawn-sync
                (fn [& _] #js {:status nil :error (js/Error. "spawn ENOENT")})]
    (is (= 1 (workspace/run-action! [{:path "opencode"
                                      :source-type "git-submodule"
                                      :ownership "independent-repository"
                                      :actionable true
                                      :exists true
                                      :initialized true
                                      :manager "bun"
                                      :scripts ["lint"]}]
                                    "lint")))
    (is (= 1 (workspace/run-jscpd! [{:path "opencode"
                                     :source-type "git-submodule"
                                     :ownership "independent-repository"
                                     :actionable true
                                     :exists true
                                     :initialized true}])))))

(deftest duplication-task-invokes-the-bundled-jscpd-entrypoint
  (let [spawned (atom nil)
        repo {:path "opencode"
              :source-type "git-submodule"
              :ownership "independent-repository"
              :actionable true
              :exists true
              :initialized true}]
    (with-redefs [workspace/execution-paths!
                  (fn [_] [{:repo repo :absolute "/workspace/opencode"}])
                  workspace/spawn-sync
                  (fn [command args _options]
                    (reset! spawned [command (js->clj args)])
                    #js {:status 0})]
      (is (zero? (workspace/run-jscpd! [repo])))
      (is (= "jscpd" (first @spawned)))
      (is (= ["--config" ".jscpd.json" "/workspace/opencode"]
             (second @spawned)))
      (is (not-any? #{"npx" "jscpd@4.2.3"} (flatten @spawned))))))

(deftest refuses-uninitialized-repositories
  (let [repo {:path "uninitialized"
              :source-type "git-submodule"
              :ownership "independent-repository"
              :actionable true :exists true :initialized false
              :manager "npm" :scripts ["test"]}]
    (with-redefs [workspace/execution-paths! (fn [repos] (mapv #(hash-map :repo %) repos))]
      (is (= 3 (workspace/run-action! [repo] "test"))))
    (is (thrown-with-msg? js/Error #"requires initialized repositories: uninitialized"
                          (workspace/run-jscpd! [repo])))))

(deftest execution-boundaries-refuse-consolidation-inputs
  (let [source {:path "eta" :actionable false :exists true}]
    (is (thrown-with-msg? js/Error #"Inventory-only sources cannot execute actions: eta"
                          (workspace/run-action! [source] "test")))
    (is (thrown-with-msg? js/Error #"Inventory-only sources cannot execute jscpd: eta"
                          (workspace/run-jscpd! [source])))))

(deftest lexical-paths-stay-confined
  (is (= (path/join workspace/root "opencode")
         (:absolute (workspace/lexical-source-path workspace/root "opencode"))))
  (doseq [invalid ["" "." ".." "../outside" "safe/../../outside"
                   "opencode/../.agents" (path/resolve workspace/root "opencode")]]
    (is (thrown? js/Error (workspace/lexical-source-path workspace/root invalid)))))

(deftest executable-authority-is-not-forgeable
  (let [paths #{"opencode"}
        valid {:path "opencode" :source-type "git-submodule"
               :ownership "independent-repository" :actionable true}]
    (is (workspace/executable-source? paths valid))
    (is (false? (workspace/executable-source? paths (assoc valid :path ".agents"))))
    (is (false? (workspace/executable-source? #{".agents"}
                                               (assoc valid :path ".agents"
                                                      :source-type "consolidation-input"))))
    (is (false? (workspace/executable-source? paths (assoc valid :ownership "workspace-root"))))
    (is (false? (workspace/executable-source? paths (assoc valid :actionable false))))))

(deftest protected-consolidation-descendants-cannot-be-submodules
  (doseq [protected [".agents/skills/example" "eta" "eta/nested-repo"]]
    (is (workspace/protected-consolidation-path? protected))
    (is (thrown-with-msg? js/Error #"collide with consolidation inputs"
                          (workspace/validate-submodules! [{:path protected}]))))
  (is (= [{:path ".agents" :url "git@github.com:riatzukiza/.agents.git"}]
         (workspace/validate-submodules!
          [{:path ".agents" :url "git@github.com:riatzukiza/.agents.git"}])))
  (is (thrown-with-msg? js/Error #"collide with consolidation inputs"
                        (workspace/validate-submodules!
                         [{:path ".agents" :url "git@example/wrong.git"}])))
  (is (false? (workspace/protected-consolidation-path? "eta-mu"))))

(deftest symlinked-source-paths-are-rejected
  (let [fixture (fs/mkdtempSync (path/join (os/tmpdir) "foresight-workspace-"))
        outside (fs/mkdtempSync (path/join (os/tmpdir) "foresight-outside-"))]
    (try
      (fs/symlinkSync outside (path/join fixture "linked"))
      (is (thrown-with-msg? js/Error #"contains a symlink"
                            (workspace/inspect-source-path! fixture "linked")))
      (fs/mkdirSync (path/join fixture "safe"))
      (fs/symlinkSync outside (path/join fixture "safe" "bridge"))
      (is (thrown-with-msg? js/Error #"contains a symlink"
                            (workspace/inspect-source-path! fixture "safe/bridge/repo")))
      (finally
        (fs/rmSync fixture #js {:recursive true :force true})
        (fs/rmSync outside #js {:recursive true :force true})))))

(deftest changed-source-identity-is-rejected-before-spawn
  (let [fixture (fs/mkdtempSync (path/join (os/tmpdir) "foresight-identity-"))
        repo-dir (path/join fixture "repo")
        calls (atom 0)]
    (try
      (fs/writeFileSync (path/join fixture ".gitmodules")
                        "[submodule \"repo\"]\n  path = repo\n  url = git@example/repo.git\n")
      (fs/mkdirSync repo-dir)
      (let [{:keys [device inode]} (workspace/inspect-source-path! fixture "repo")
            repo {:path "repo" :source-type "git-submodule"
                  :ownership "independent-repository" :actionable true
                  :exists true :initialized true :device device :inode inode
                  :manager "npm" :scripts ["test"]}]
        (fs/renameSync repo-dir (path/join fixture "original-repo"))
        (fs/mkdirSync repo-dir)
        (with-redefs [workspace/root fixture
                      workspace/spawn-sync (fn [& _] (swap! calls inc) #js {:status 0})]
          (is (thrown-with-msg? js/Error #"identity changed after inventory"
                                (workspace/run-action! [repo] "test")))
          (is (zero? @calls))))
      (finally
        (fs/rmSync fixture #js {:recursive true :force true})))))

(deftest executable-records-require-inventory-identity
  (let [fixture (fs/mkdtempSync (path/join (os/tmpdir) "foresight-no-identity-"))
        repo-dir (path/join fixture "repo")]
    (try
      (fs/writeFileSync (path/join fixture ".gitmodules")
                        "[submodule \"repo\"]\n  path = repo\n  url = git@example/repo.git\n")
      (fs/mkdirSync repo-dir)
      (with-redefs [workspace/root fixture]
        (is (thrown-with-msg? js/Error #"lacks inventory identity"
                              (workspace/execution-paths!
                               [{:path "repo" :source-type "git-submodule"
                                 :ownership "independent-repository" :actionable true}]))))
      (finally
        (fs/rmSync fixture #js {:recursive true :force true})))))

(deftest forged-consolidation-source-never-spawns
  (let [{:keys [device inode]} (workspace/inspect-source-path! workspace/root ".agents")
        calls (atom 0)
        forged {:path ".agents" :source-type "git-submodule"
                :ownership "independent-repository" :actionable true
                :exists true :initialized true :device device :inode inode
                :manager "npm" :scripts ["test"]}]
    (with-redefs [workspace/spawn-sync (fn [& _] (swap! calls inc) #js {:status 0})]
      (is (thrown? js/Error (workspace/run-action! [forged] "test")))
      (is (thrown? js/Error (workspace/run-jscpd! [forged])))
      (is (zero? @calls)))))

(deftest report-distinguishes-consolidation-inputs
  (let [report (workspace/markdown-report
                [{:path "eta"
                  :source-type "consolidation-input"
                  :ownership "workspace-root"
                  :manifests ["deps.edn" "bb.edn"]
                  :manager nil
                  :lockfiles []
                  :scripts []
                  :actionable false}])]
    (is (re-find #"\| Path \| Source \| Ownership \|" report))
    (is (re-find #"eta \| consolidation-input \| workspace-root" report))
    (is (re-find #"deps.edn, bb.edn \| n/a" report))
    (is (re-find #"policy-skipped \| inventory-only" report))))

(defn test-exit-code [summary]
  (if (test/successful? summary) 0 1))

(deftest test-exit-code-follows-summary
  (is (zero? (test-exit-code {:fail 0 :error 0})))
  (is (= 1 (test-exit-code {:fail 1 :error 0})))
  (is (= 1 (test-exit-code {:fail 0 :error 1}))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (test-exit-code summary)))

(test/run-tests 'workspace-test)
