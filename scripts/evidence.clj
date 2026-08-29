;; SPDX-License-Identifier: GPL-3.0-or-later
(ns evidence
  (:require [cljs.core :refer [clj->js]]
            [cljs.reader :as reader]
            [clojure.string :as str]
            [foresight.evidence :as law]
            [nbb.core :as nbb]
            [workspace :as workspace]
            ["child_process" :as child-process]
            ["fs" :as fs]
            ["path" :as path]))

(def root
  (path/resolve (path/dirname nbb/*file*) ".."))

(def catalog-file
  (path/join root "config" "quality-gates.edn"))

(defn read-catalog []
  (reader/read-string (fs/readFileSync catalog-file "utf8")))

(defn validate-catalog! [catalog]
  (when-let [errors (seq (law/catalog-errors catalog))]
    (throw (js/Error. (str "Invalid quality gate catalog: " (pr-str errors)))))
  catalog)

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

(defn require-repositories! [catalog only]
  (when-not (seq only)
    (throw (js/Error. "Choose repositories with --only <paths>")))
  (let [known (set (keys (:catalog/repositories catalog)))
        unknown (seq (remove known only))]
    (when unknown
      (throw (js/Error. (str "Repositories have no mapped gates: "
                             (str/join ", " unknown))))))
  (let [inventory (workspace/inventory)
        selected (workspace/select-repos inventory {:only only :all? false})
        available (filterv #(and (:exists %) (:initialized %)) selected)
        execution-paths (into {}
                              (map (fn [{:keys [repo absolute]}]
                                     [(:path repo) absolute]))
                              (workspace/execution-paths! available))]
    (into {}
          (map (fn [{:keys [path exists initialized head]}]
                 [path (if-let [absolute (get execution-paths path)]
                         {:absolute absolute :revision head}
                         {:unavailable-reason
                          (cond
                            (not exists) "Repository checkout is missing"
                            (not initialized) "Repository checkout is not initialized"
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

(defn run-gate! [{:keys [absolute unavailable-reason revision]} gate]
  (println "START" (str (:gate/id gate)) (name (:gate/kind gate)))
  (let [result
        (case (:gate/execution gate)
          :local (if absolute
                   (local-result absolute gate)
                   {:gate/id (:gate/id gate)
                    :result/outcome :unavailable
                    :result/reason unavailable-reason})
          :workflow-only {:gate/id (:gate/id gate)
                          :result/outcome :unavailable
                          :result/reason (:gate/reason gate)}
          :external {:gate/id (:gate/id gate)
                     :result/outcome :blocked
                     :result/reason (:gate/reason gate)})
        result (cond-> result
                 (law/nonblank-string? revision)
                 (assoc :result/revision revision))]
    (println (str/upper-case (name (:result/outcome result)))
             (str (:gate/id gate))
             (or (:result/reason result) ""))
    (println "RESULT" (pr-str result))
    result))

(defn result-exit [{:result/keys [outcome]}]
  (case outcome
    :passed 0
    :failed 1
    :unavailable 3
    :blocked 4
    :not-applicable 0
    2))

(defn list-gates! [catalog {:keys [only kinds]}]
  (let [paths (or only (set (keys (:catalog/repositories catalog))))
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

(defn run-selected-gates! [catalog {:keys [only kinds]}]
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
                            (run-gate! (get paths repository-path) gate)))
                        gates)
          summary (law/summarize-results results)]
      (println "SUMMARY" (pr-str summary))
      (reduce max 0 (map result-exit results)))))

(defn -main [& args]
  (try
    (let [[command & option-args] args
          catalog (validate-catalog! (read-catalog))
          options (parse-args option-args)]
      (case command
        "validate" (do (println "PASS quality gate catalog") 0)
        "list" (list-gates! catalog options)
        "run" (run-selected-gates! catalog options)
        (throw (js/Error. (str "Unknown command: " (or command "<missing>"))))))
    (catch :default error
      (binding [*out* *err*] (println (.-message error)))
      2)))

(when (= nbb/*file* (nbb/invoked-file))
  (set! (.-exitCode js/process) (apply -main *command-line-args*)))
