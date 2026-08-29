(ns workspace
  (:require [cljs.core :refer [clj->js js->clj]]
            [clojure.string :as str]
            [nbb.core :as nbb]
            ["child_process" :as child-process]
            ["fs" :as fs]
            ["path" :as path]))

(def root
  (path/resolve (path/dirname nbb/*file*) ".."))

(def lock-managers
  {"pnpm-lock.yaml" "pnpm"
   "package-lock.json" "npm"
   "npm-shrinkwrap.json" "npm"
   "yarn.lock" "yarn"
   "bun.lock" "bun"
   "bun.lockb" "bun"})

(def consolidation-inputs
  [{:name ".agents"
    :path ".agents"
    :expected-url "git@github.com:riatzukiza/.agents.git"
    :source-type "consolidation-input"
    :ownership "nested-git-canonical"
    :role "skill-catalog"
    :actionable false}
   {:name "eta"
    :path "eta"
    :source-type "consolidation-input"
    :ownership "workspace-root"
    :role "clojure-harness"
    :actionable false}])

(def protected-consolidation-paths
  (into #{} (map :path) consolidation-inputs))

(def git-managed-consolidation-inputs
  (into {} (keep (fn [{:keys [path expected-url] :as source}]
                   (when expected-url [path source])))
        consolidation-inputs))

(defn parse-gitmodules [content]
  (let [finish #(if (:name %2) (conj %1 %2) %1)]
    (apply finish
           (reduce
            (fn [[repos current] line]
              (let [line (str/trim line)]
                (if-let [[_ name] (re-matches #"\[submodule \"([^\"]+)\"\]" line)]
                  [(finish repos current) {:name name}]
                  (if-let [[_ key value] (re-matches #"(path|url)\s*=\s*(.+)" line)]
                    [repos (assoc current (keyword key) value)]
                    [repos current]))))
            [[] {}]
            (str/split-lines content)))))

(defn declared-manager [package-manager]
  (when (string? package-manager)
    (when-let [[_ manager major]
               (re-matches
                #"^(pnpm|npm|yarn|bun)@(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-(?:0|[1-9]\d*|\d*[A-Za-z-][0-9A-Za-z-]*)(?:\.(?:0|[1-9]\d*|\d*[A-Za-z-][0-9A-Za-z-]*))*)?(?:\+[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?$"
                package-manager)]
      (when-not (and (= "yarn" manager) (< (js/Number major) 2))
        manager))))

(defn select-manager [package-manager lockfiles]
  (if (nil? package-manager)
    (let [managers (into #{} (keep lock-managers) lockfiles)]
      (when (and (= 1 (count managers)) (not= #{"yarn"} managers))
        (first managers)))
    (declared-manager package-manager)))

(defn file-exists? [file]
  (fs/existsSync file))

(defn lexical-source-path [base declared-path]
  (when-not (and (string? declared-path) (not (str/blank? declared-path)))
    (throw (js/Error. "Source path must be a nonblank relative path")))
  (let [segments (str/split declared-path #"[\\/]")]
    (when (or (path/isAbsolute declared-path)
              (some #{"" "." ".."} segments))
      (throw (js/Error. (str "Source path is not a confined direct path: " declared-path))))
    (let [absolute (path/resolve base declared-path)
          relative (path/relative base absolute)]
      (when (or (str/blank? relative)
                (path/isAbsolute relative)
                (= ".." relative)
                (str/starts-with? relative (str ".." path/sep)))
        (throw (js/Error. (str "Source path escapes workspace: " declared-path))))
      {:absolute absolute :segments segments})))

(defn inspect-source-path! [base declared-path]
  (let [{:keys [absolute segments]} (lexical-source-path base declared-path)]
    (loop [current base [segment & more] segments]
      (if-not segment
        (let [stat (fs/lstatSync absolute #js {:throwIfNoEntry false})]
          {:absolute absolute
           :exists (boolean stat)
           :device (when stat (.-dev stat))
           :inode (when stat (.-ino stat))})
        (let [candidate (path/join current segment)
              stat (fs/lstatSync candidate #js {:throwIfNoEntry false})]
          (cond
            (nil? stat) {:absolute absolute :exists false}
            (.isSymbolicLink stat)
            (throw (js/Error. (str "Source path contains a symlink: " declared-path)))
            :else (recur candidate more)))))))

(defn plain-path-stat [file]
  (let [stat (fs/lstatSync file #js {:throwIfNoEntry false})]
    (when (and stat (.isSymbolicLink stat))
      (throw (js/Error. (str "Direct source metadata is a symlink: " file))))
    stat))

(defn plain-file? [file]
  (boolean (some-> (plain-path-stat file) .isFile)))

(defn plain-directory? [file]
  (boolean (some-> (plain-path-stat file) .isDirectory)))

(defn read-json [file]
  (when (plain-file? file)
    (js->clj (js/JSON.parse (fs/readFileSync file "utf8"))
             :keywordize-keys true)))

(defn spawn-sync [program args options]
  (child-process/spawnSync program args options))

(defn spawn-failure-message [result]
  (or (some-> (.-error result) .-message)
      (when (nil? (.-status result))
        (str "process terminated without an exit status"
             (when-let [signal (.-signal result)] (str " (signal " signal ")"))))))

(defn result-exit [result]
  (if (number? (.-status result)) (.-status result) 1))

(defn run-captured [cwd command]
  (let [result (spawn-sync
                (first command)
                (clj->js (rest command))
                #js {:cwd cwd :encoding "utf8" :shell false})]
    {:exit (result-exit result)
     :error (spawn-failure-message result)
     :stdout (str/trim (or (.-stdout result) ""))
     :stderr (str/trim (or (.-stderr result) ""))}))

(defn command-failure [{:keys [exit error stderr]}]
  (when-not (zero? exit)
    (or error (not-empty stderr) (str "exit " exit))))

(defn git-state [repo-dir]
  (if-not (plain-path-stat (path/join repo-dir ".git"))
    {:initialized false}
    (let [head (run-captured repo-dir ["git" "rev-parse" "HEAD"])
          initialized (zero? (:exit head))
          branch (run-captured repo-dir ["git" "branch" "--show-current"])
          status (run-captured repo-dir ["git" "status" "--short"])
          git-errors (cond-> {}
                       (command-failure head) (assoc :head (command-failure head))
                       (and initialized (command-failure branch))
                       (assoc :branch (command-failure branch))
                       (and initialized (command-failure status))
                       (assoc :status (command-failure status)))]
      {:initialized initialized
       :head (when initialized (:stdout head))
       :branch (when (zero? (:exit branch)) (:stdout branch))
       :dirty (when (zero? (:exit status)) (not (str/blank? (:stdout status))))
       :git-errors git-errors})))

(defn source-inventory [{:keys [path actionable] :as source}]
  (let [{:keys [absolute exists device inode]} (inspect-source-path! root path)
        root-facts {:exists exists :device device :inode inode}]
    (if-not actionable
      ;; Consolidation inputs are classified from root-owned declarations only.
      ;; Looking below their root would turn inventory into execution authority.
      (merge source root-facts
             {:manifests []
              :lockfiles []
              :package-manager nil
              :manager nil
              :scripts []})
      (let [repo-dir absolute
            package-file (path/join repo-dir "package.json")
            package-json (read-json package-file)
            lockfiles (->> (keys lock-managers)
                           (filter #(plain-file? (path/join repo-dir %)))
                           sort vec)
            package-manager (:packageManager package-json)]
        (merge source root-facts
               {:manifests (cond-> []
                             (plain-file? package-file) (conj "package.json")
                             (plain-file? (path/join repo-dir "deps.edn")) (conj "deps.edn")
                             (plain-file? (path/join repo-dir "bb.edn")) (conj "bb.edn"))
                :lockfiles lockfiles
                :package-manager package-manager
                :manager (select-manager package-manager lockfiles)
                :scripts (->> package-json :scripts keys (map name) sort vec)}
               (git-state repo-dir))))))

(defn repo-inventory [source]
  (if-let [consolidation (git-managed-consolidation-inputs (:path source))]
    (source-inventory (merge consolidation source {:git-managed true}))
    (source-inventory
     (merge {:source-type "git-submodule"
             :ownership "independent-repository"
             :role "repository"
             :actionable true}
            source))))

(defn protected-consolidation-path? [declared-path]
  (let [candidate (:absolute (lexical-source-path root declared-path))]
    (boolean
     (some
      (fn [protected]
        (let [relative (path/relative (path/resolve root protected) candidate)]
          (or (str/blank? relative)
              (and (not (path/isAbsolute relative))
                   (not= ".." relative)
                   (not (str/starts-with? relative (str ".." path/sep)))))))
      protected-consolidation-paths))))

(defn allowed-consolidation-submodule? [{:keys [path url]}]
  (when-let [{:keys [expected-url]} (git-managed-consolidation-inputs path)]
    (= expected-url url)))

(defn validate-submodules! [submodules]
  (let [paths (mapv :path submodules)
        duplicates (->> paths frequencies (keep (fn [[path count]] (when (> count 1) path))) seq)
        protected (seq (remove allowed-consolidation-submodule?
                               (filter #(protected-consolidation-path? (:path %)) submodules)))]
    (when duplicates
      (throw (js/Error. (str "Duplicate submodule paths: " (str/join ", " duplicates)))))
    (when protected
      (throw (js/Error. (str "Submodule paths collide with consolidation inputs: "
                            (str/join ", " (map :path protected))))))
    (doseq [{:keys [path]} submodules]
      (inspect-source-path! root path))
    submodules))

(defn current-submodules []
  (-> (fs/readFileSync (path/join root ".gitmodules") "utf8")
      parse-gitmodules
      validate-submodules!))

(defn inventory []
  (let [submodules (mapv repo-inventory (current-submodules))
        managed-paths (into #{} (map :path) submodules)]
    (into submodules
          (comp (remove #(managed-paths (:path %))) (map source-inventory))
          consolidation-inputs)))

(defn markdown-report [repos]
  (str "# Foresight Workspace Report\n\n"
       "| Path | Source | Ownership | Manifests | Manager | Lockfiles | Scripts | Git | Actions |\n"
       "|---|---|---|---|---|---|---|---|---|\n"
       (str/join
        "\n"
         (map (fn [{:keys [path source-type ownership manifests manager lockfiles scripts
                           initialized dirty git-errors actionable]}]
                (str "| " path " | " source-type " | " ownership " | "
                     (or (not-empty (str/join ", " manifests)) "-") " | "
                     (if (some #{"package.json"} manifests)
                       (or manager "unresolved")
                       "n/a") " | "
                     (or (not-empty (str/join ", " lockfiles)) "-") " | "
                     (or (not-empty (str/join ", " scripts)) "-") " | "
                     (if-not actionable
                       "policy-skipped"
                       (cond
                         (seq git-errors) (str "error (" (str/join ", " (map name (keys git-errors))) ")")
                         initialized (if dirty "dirty" "clean")
                         :else "uninitialized")) " | "
                     (if actionable "explicit-root-only" "inventory-only") " |"))
             repos))
       "\n"))

(defn write-report! [repos]
  (let [report-dir (path/join root "reports")]
    (fs/mkdirSync report-dir #js {:recursive true})
    (fs/writeFileSync (path/join report-dir "workspace.json")
                      (str (js/JSON.stringify (clj->js repos) nil 2) "\n"))
    (fs/writeFileSync (path/join report-dir "workspace.md")
                      (markdown-report repos))))

(defn parse-args [args]
  (loop [remaining (vec args) options {:only nil :all? false}]
    (if-let [arg (first remaining)]
      (case arg
        "--all" (recur (subvec remaining 1) (assoc options :all? true))
        "--only" (if-let [value (second remaining)]
                   (let [only (into #{} (remove str/blank?)
                                    (map str/trim (str/split value #",")))]
                     (when (empty? only)
                       (throw (js/Error. "--only requires at least one repository")))
                     (recur (subvec remaining 2) (assoc options :only only)))
                   (throw (js/Error. "--only requires a comma-separated value")))
        (throw (js/Error. (str "Unknown argument: " arg))))
      options)))

(defn select-repos [repos {:keys [only all?]}]
  (when (= (boolean only) all?)
    (throw (js/Error. "Choose exactly one of --only <paths> or --all")))
  (let [selected
        (if all?
          (filterv :actionable repos)
          (let [known (into #{} (map :path) repos)
                unknown (seq (remove known only))]
            (when unknown
              (throw (js/Error. (str "Unknown repositories: " (str/join ", " unknown)))))
            (filterv #(only (:path %)) repos)))]
    (when (empty? selected)
      (throw (js/Error. "No repositories selected")))
    (when-let [inventory-only (seq (remove :actionable selected))]
      (throw (js/Error. (str "Inventory-only sources cannot execute actions: "
                            (str/join ", " (map :path inventory-only))))))
    selected))

(defn executable-source? [submodule-paths repo]
  (and (= "git-submodule" (:source-type repo))
       (= "independent-repository" (:ownership repo))
       (true? (:actionable repo))
       (contains? submodule-paths (:path repo))))

(defn execution-paths! [repos]
  (let [submodule-paths (into #{}
                              (comp (remove #(git-managed-consolidation-inputs (:path %)))
                                    (map :path))
                              (current-submodules))]
    (mapv
     (fn [{:keys [path device inode] :as repo}]
       (when-not (executable-source? submodule-paths repo)
         (throw (js/Error. (str "Source is not an executable direct submodule: " path))))
       (when-not (and (number? device) (number? inode))
         (throw (js/Error. (str "Executable source lacks inventory identity: " path))))
       (let [{current-path :absolute current-exists :exists
              current-device :device current-inode :inode}
             (inspect-source-path! root path)]
         (when-not current-exists
           (throw (js/Error. (str "Executable source path is missing: " path))))
         (when (and device inode
                    (or (not= device current-device) (not= inode current-inode)))
           (throw (js/Error. (str "Executable source identity changed after inventory: " path))))
         {:repo repo :absolute current-path}))
     repos)))

(defn install-command [{:keys [manager lockfiles]}]
  (case manager
    "pnpm" (when (some #{"pnpm-lock.yaml"} lockfiles)
             ["pnpm" "install" "--frozen-lockfile"])
    "npm" (when (some #{"package-lock.json" "npm-shrinkwrap.json"} lockfiles)
            ["npm" "ci"])
    "yarn" (when (some #{"yarn.lock"} lockfiles)
             ["yarn" "install" "--immutable"])
    "bun" (when (some #{"bun.lock" "bun.lockb"} lockfiles)
            ["bun" "install" "--frozen-lockfile"])
    nil))

(defn script-command [{:keys [manager scripts]} action]
  (when (and manager (some #{action} scripts))
    (case manager
      "npm" ["npm" "run" action]
      "pnpm" ["pnpm" "run" action]
      "yarn" ["yarn" "run" action]
      "bun" ["bun" "run" action])))

(defn action-command [repo action]
  (if (= action "install")
    (install-command repo)
    (script-command repo action)))

(defn run-action! [repos action]
  (when-let [inventory-only (seq (remove :actionable repos))]
    (throw (js/Error. (str "Inventory-only sources cannot execute actions: "
                          (str/join ", " (map :path inventory-only))))))
  (let [_ (execution-paths! repos)
        results
        (mapv
         (fn [{:keys [path] :as repo}]
           (if-not (and (:exists repo) (:initialized repo))
              (do
                (println "SKIP" path action "repository is not initialized")
                {:path path :status "SKIP" :exit 3})
               (if-let [command (action-command repo action)]
                 (do
                   (println "START" path (str/join " " command))
                  (let [{cwd :absolute} (first (execution-paths! [repo]))
                        result (spawn-sync
                                (first command)
                                (clj->js (rest command))
                                #js {:cwd cwd
                                     :stdio "inherit"
                                     :shell false})
                        error (spawn-failure-message result)
                        exit (result-exit result)
                        status (if (zero? exit) "PASS" "FAIL")]
                    (when error
                      (binding [*out* *err*] (println "ERROR" path action error)))
                    (println status path action)
                    {:path path :status status :exit exit}))
                (do
                  (println "SKIP" path action "unavailable")
                  {:path path :status "SKIP" :exit 3}))))
         repos)]
    (cond
      (some #(= "FAIL" (:status %)) results) 1
      (some #(= "SKIP" (:status %)) results) 3
      :else 0)))

(defn run-jscpd! [repos]
  (let [paths (mapv :path repos)]
    (when (empty? paths)
      (throw (js/Error. "jscpd requires at least one repository")))
    (when-let [inventory-only (seq (remove :actionable repos))]
      (throw (js/Error. (str "Inventory-only sources cannot execute jscpd: "
                            (str/join ", " (map :path inventory-only))))))
    (when-let [uninitialized (seq (remove #(and (:exists %) (:initialized %)) repos))]
      (throw (js/Error. (str "jscpd requires initialized repositories: "
                            (str/join ", " (map :path uninitialized))))))
    (let [validated-paths (mapv :absolute (execution-paths! repos))
          command (into ["jscpd" "--config" ".jscpd.json"]
                        validated-paths)]
      (println "START" (str/join " " command))
      (let [result (spawn-sync
                   (first command)
                   (clj->js (rest command))
                   #js {:cwd root :stdio "inherit" :shell false})]
        (when-let [error (spawn-failure-message result)]
          (binding [*out* *err*] (println "ERROR jscpd" error)))
        (result-exit result)))))

(defn -main [& args]
  (try
    (let [[command & option-args] args
          repos (inventory)]
      (case command
        "inventory" (do (println (js/JSON.stringify (clj->js repos) nil 2)) 0)
        "report" (do (write-report! repos)
                     (println (markdown-report repos))
                     0)
        ("install" "build" "test" "lint")
        (run-action! (select-repos repos (parse-args option-args)) command)
        "jscpd" (run-jscpd! (select-repos repos (parse-args option-args)))
        (throw (js/Error. (str "Unknown command: " (or command "<missing>"))))))
    (catch :default error
      (binding [*out* *err*]
        (println (.-message error)))
      2)))

(when (= nbb/*file* (nbb/invoked-file))
  (set! (.-exitCode js/process) (apply -main *command-line-args*)))
