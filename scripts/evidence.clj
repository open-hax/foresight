;; SPDX-License-Identifier: GPL-3.0-or-later
(ns evidence
  (:require [cljs.core :refer [clj->js]]
            [cljs.reader :as reader]
            [clojure.string :as str]
            [foresight.evidence :as law]
            [foresight.project :as project-model]
            [nbb.core :as nbb]
            [workspace :as workspace]
            ["child_process" :as child-process]
            ["crypto" :as crypto]
            ["fs" :as fs]
            ["path" :as path]))

(def root
  (path/resolve (path/dirname nbb/*file*) ".."))

(def catalog-file
  (path/join root "config" "quality-gates.edn"))

(def catalog-relative-path "config/quality-gates.edn")

(defn sha256 [value]
  (-> (crypto/createHash "sha256")
      (.update value "utf8")
      (.digest "hex")))

(defn read-catalog-bundle []
  (let [contents (fs/readFileSync catalog-file "utf8")]
    {:catalog (reader/read-string contents)
     :catalog-identity {:catalog/path catalog-relative-path
                        :catalog/sha256 (sha256 contents)}}))

(defn read-catalog []
  (:catalog (read-catalog-bundle)))

(defn actionable-submodule-paths []
  (into #{}
        (comp (filter :source/actionable?)
              (map :source/path))
        (project-model/submodule-sources)))

(defn validate-catalog!
  ([catalog]
   (validate-catalog! catalog (actionable-submodule-paths)))
  ([catalog actionable-paths]
   (when-let [errors (seq (into (law/catalog-errors catalog)
                                (law/catalog-inventory-errors
                                 catalog
                                 actionable-paths)))]
    (throw (js/Error. (str "Invalid quality gate catalog: " (pr-str errors)))))
   catalog))

(defn parse-csv [value flag]
  (let [values (into #{} (remove str/blank?)
                     (map str/trim (str/split (or value "") #",")))]
    (when (empty? values)
      (throw (js/Error. (str flag " requires a comma-separated value"))))
    values))

(defn parse-args [args]
  (loop [remaining (vec args)
         options {:only nil :kinds law/gate-kinds}]
    (if-let [arg (first remaining)]
      (case arg
        "--only"
        (if-let [value (second remaining)]
          (recur (subvec remaining 2)
                 (assoc options :only (parse-csv value "--only")))
          (throw (js/Error. "--only requires a comma-separated value")))

        "--kind"
        (if-let [value (second remaining)]
          (let [kinds (into #{} (map keyword) (parse-csv value "--kind"))
                unknown (seq (remove law/gate-kinds kinds))]
            (when unknown
              (throw (js/Error. (str "Unknown gate kinds: "
                                     (str/join ", " (map name unknown))))))
            (recur (subvec remaining 2) (assoc options :kinds kinds)))
          (throw (js/Error. "--kind requires a comma-separated value")))

        (throw (js/Error. (str "Unknown argument: " arg))))
      options)))

(defn require-mapped-repositories! [catalog repository-paths]
  (let [known (set (keys (:catalog/repositories catalog)))
        unknown (seq (remove known repository-paths))]
    (when unknown
      (throw (js/Error. (str "Repositories have no mapped gates: "
                             (str/join ", " unknown)))))
    repository-paths))

(defn require-repositories! [catalog only]
  (when-not (seq only)
    (throw (js/Error. "Choose repositories with --only <paths>")))
  (require-mapped-repositories! catalog only)
  (let [inventory (workspace/inventory)
        selected (workspace/select-repos inventory {:only only :all? false})
        available (filterv #(and (:exists %)
                                 (:initialized %)
                                 (false? (:dirty %))
                                 (empty? (:git-errors %)))
                           selected)
        execution-paths (into {}
                              (map (fn [{:keys [repo absolute]}]
                                     [(:path repo) absolute]))
                              (workspace/execution-paths! available))]
    (into {}
          (map (fn [{:keys [path exists initialized dirty git-errors head]
                     :as repository}]
                 [path (if-let [absolute (get execution-paths path)]
                         {:path path
                          :absolute absolute
                          :revision head
                          :inventory repository}
                         {:path path
                          :unavailable-reason
                          (cond
                            (not exists) "Repository checkout is missing"
                            (not initialized) "Repository checkout is not initialized"
                            (seq git-errors) "Repository Git state could not be verified"
                            (true? dirty) "Repository checkout is dirty"
                            (nil? dirty) "Repository checkout cleanliness is unavailable"
                            :else "Repository checkout is unavailable")
                          :revision head})]))
          selected)))

(defn spawn-result [cwd command]
  (child-process/spawnSync
   (first command)
   (clj->js (rest command))
   #js {:cwd cwd :encoding "utf8" :stdio "inherit" :shell false}))

(defn local-result [cwd gate]
  (let [result (spawn-result cwd (:gate/command gate))
        error (.-error result)
        status (.-status result)]
    (cond
      error {:gate/id (:gate/id gate)
             :result/outcome :unavailable
             :result/reason (.-message error)}
      (nil? status) {:gate/id (:gate/id gate)
                     :result/outcome :failed
                     :result/reason (str "Process ended without an exit status"
                                         (when-let [signal (.-signal result)]
                                           (str " (" signal ")")))}
      (zero? status) {:gate/id (:gate/id gate)
                      :result/outcome :passed
                      :result/exit status}
      :else {:gate/id (:gate/id gate)
             :result/outcome :failed
             :result/exit status})))

(defn execution-path-unavailable-reason
  [{:keys [absolute inventory]}]
  (if-not (map? inventory)
    "Repository inventory identity is unavailable"
    (try
      (let [{current-absolute :absolute}
            (first (workspace/execution-paths! [inventory]))]
        (when-not (= absolute current-absolute)
          "Repository execution path changed after inventory"))
      (catch :default error
        (str "Repository execution path could not be reverified: "
             (.-message error))))))

(defn local-unavailable-reason
  ([{:keys [absolute] :as repository}]
   (or (execution-path-unavailable-reason repository)
       (local-unavailable-reason repository (workspace/git-state absolute))))
  ([{:keys [revision]} {:keys [initialized head dirty git-errors]}]
    (cond
      (not initialized) "Repository checkout became uninitialized"
      (seq git-errors) "Repository Git state could not be reverified"
      (true? dirty) "Repository checkout became dirty"
      (nil? dirty) "Repository checkout cleanliness could not be reverified"
      (not= revision head) "Repository revision changed after inventory"
      :else nil)))

(defn result-provenance
  [{:keys [path revision]} gate catalog-identity]
  (cond-> {:result/execution (:gate/execution gate)
           :result/catalog catalog-identity
           :result/source {:source/path (:gate/source gate)
                           :source/repository path
                           :source/revision revision}}
    (seq (:gate/command gate))
    (assoc :result/command (vec (:gate/command gate)))))

(defn run-local-gate!
  [{:keys [absolute unavailable-reason revision] :as repository} gate]
  (if-not absolute
    {:gate/id (:gate/id gate)
     :result/outcome :unavailable
     :result/reason unavailable-reason}
    (if-let [reason (local-unavailable-reason repository)]
      {:gate/id (:gate/id gate)
       :result/outcome :unavailable
       :result/reason reason}
      (let [attempt (local-result absolute gate)
            path-reason (execution-path-unavailable-reason repository)
            post-state (when-not path-reason (workspace/git-state absolute))
            reason (or path-reason
                       (local-unavailable-reason repository post-state))]
        (if reason
          (cond-> {:gate/id (:gate/id gate)
                   :result/outcome :unavailable
                   :result/reason (str "Evidence rejected after gate execution: " reason)
                   :result/attempt-outcome (:result/outcome attempt)}
            (contains? attempt :result/exit)
            (assoc :result/attempt-exit (:result/exit attempt))

            (law/nonblank-string? (:head post-state))
            (assoc :result/observed-revision (:head post-state)))
          (cond-> attempt
            (law/nonblank-string? revision)
            (assoc :result/revision revision)))))))

(defn run-gate! [{:keys [revision] :as repository} gate catalog-identity]
  (let [provenance (result-provenance repository gate catalog-identity)]
    (println "START"
             (pr-str (merge {:gate/id (:gate/id gate)
                             :gate/kind (:gate/kind gate)}
                            provenance)))
    (let [outcome-result
          (case (:gate/execution gate)
            :local (run-local-gate! repository gate)
            :workflow-only (cond-> {:gate/id (:gate/id gate)
                                    :result/outcome :unavailable
                                    :result/reason (:gate/reason gate)}
                             (law/nonblank-string? revision)
                             (assoc :result/revision revision))
            :external (cond-> {:gate/id (:gate/id gate)
                               :result/outcome :blocked
                               :result/reason (:gate/reason gate)}
                        (law/nonblank-string? revision)
                        (assoc :result/revision revision)))
          result (merge outcome-result provenance)]
      (println (str/upper-case (name (:result/outcome result)))
               (str (:gate/id gate))
               (or (:result/reason result) ""))
      (println "RESULT" (pr-str result))
      result)))

(defn result-exit [{:result/keys [outcome]}]
  (case outcome
    :passed 0
    :failed 1
    :unavailable 3
    :blocked 4
    :not-applicable 0
    2))

(defn list-gates! [catalog {:keys [only kinds]}]
  (let [paths (if only
                (require-mapped-repositories! catalog only)
                (set (keys (:catalog/repositories catalog))))
        gates (law/select-gates catalog (sort paths) kinds)]
    (doseq [gate gates]
      (println (str (:gate/id gate))
               (name (:gate/kind gate))
               (name (:gate/execution gate))
               (str/join " " (:gate/command gate))
               (:gate/source gate)))
    0))

(defn gate-repository-path [catalog gate]
  (some (fn [[repository-path repository]]
          (when (some #(= (:gate/id gate) (:gate/id %))
                      (:repository/gates repository))
            repository-path))
        (:catalog/repositories catalog)))

(defn run-selected-gates! [catalog catalog-identity {:keys [only kinds]}]
  (let [paths (require-repositories! catalog only)
        gates (law/select-gates catalog (sort only) kinds)]
    (when (empty? gates)
      (throw (js/Error. "No mapped gates match the requested repositories and kinds")))
    (let [results (mapv (fn [gate]
                          (let [repository-path (gate-repository-path catalog gate)]
                            (when-not repository-path
                              (throw (js/Error.
                                      (str "Gate repository is not in the catalog: "
                                           (:gate/id gate)))))
                            (run-gate! (get paths repository-path)
                                       gate
                                       catalog-identity)))
                        gates)
          summary (law/summarize-results results)]
      (println "SUMMARY" (pr-str summary))
      (reduce max 0 (map result-exit results)))))

(defn -main [& args]
  (try
    (let [[command & option-args] args
          {:keys [catalog catalog-identity]} (read-catalog-bundle)
          catalog (validate-catalog! catalog)
          options (parse-args option-args)]
      (case command
        "validate" (do (println "PASS quality gate catalog") 0)
        "list" (list-gates! catalog options)
        "run" (run-selected-gates! catalog catalog-identity options)
        (throw (js/Error. (str "Unknown command: " (or command "<missing>"))))))
    (catch :default error
      (binding [*out* *err*] (println (.-message error)))
      2)))

(when (= nbb/*file* (nbb/invoked-file))
  (set! (.-exitCode js/process) (apply -main *command-line-args*)))
