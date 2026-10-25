;; SPDX-License-Identifier: GPL-3.0-or-later
(ns antigravity-rollout
  "NBB filesystem adapter for explicitly scoped, pinned Antigravity review rollout."
  (:require [clojure.string :as str]
            [foresight.antigravity-review :as review]
            [foresight.project :as project]
            [nbb.core :as nbb]
            ["child_process" :as child-process]
            ["fs" :as fs]
            ["path" :as path]))

(def default-root (path/resolve (path/dirname nbb/*file*) ".."))

(defn- git-output [checkout & args]
  (let [result (child-process/spawnSync
                "git" (into-array (concat ["-C" checkout] args))
                #js {:encoding "utf8" :timeout 10000})]
    (when (= 0 (.-status result)) (str/trim (.-stdout result)))))

(defn- github-repository [url]
  (some->> (re-matches #"^(?:git@github\.com:|https://github\.com/|ssh://git@github\.com/)([^/]+/[^/]+?)(?:\.git)?/?$" url)
           second))

(defn- file-state [checkout revision runtime-revision workflow-repository]
  (let [segments [".github" "workflows" "antigravity-review.yml"]]
    (loop [current checkout [segment & more] segments]
      (if-not segment
        {:workflow-status (review/workflow-status
                           (fs/readFileSync current "utf8") revision runtime-revision
                           workflow-repository)}
        (let [candidate (path/join current segment)
              stat (fs/lstatSync candidate #js {:throwIfNoEntry false})]
          (cond
            (nil? stat) {:workflow-status :absent}
            (.isSymbolicLink stat) {:workflow-status :unsafe-path :unsafe-path candidate}
            (and more (not (.isDirectory stat)))
            {:workflow-status :unsafe-path :unsafe-path candidate}
            (and (not more) (not (.isFile stat)))
            {:workflow-status :unsafe-path :unsafe-path candidate}
            :else (recur candidate more)))))))

(defn inspect-target
  "Inspect only a declared checkout and the sole generated workflow path."
  [root checkouts runtime-revision {:keys [path repository workflow-repo workflow-revision] :as target}]
  (let [checkout (path/resolve (or (get checkouts path) (path/join root path)))
        stat (fs/lstatSync checkout #js {:throwIfNoEntry false})
        base (assoc target :checkout checkout)]
    (cond
      (nil? stat) (assoc base :checkout-status :absent)
      (.isSymbolicLink stat) (assoc base :checkout-status :unsafe-path)
      (not (.isDirectory stat)) (assoc base :checkout-status :not-a-directory)
      :else
      (let [toplevel (git-output checkout "rev-parse" "--show-toplevel")
            remote-names (some-> (git-output checkout "remote") str/split-lines)
            remote-repositories
            (into #{} (keep #(some-> (git-output checkout "remote" "get-url" %)
                                    github-repository)) remote-names)]
        (cond
          (not= (fs/realpathSync checkout) toplevel)
          (assoc base :checkout-status :not-a-checkout-root)
          (not (contains? remote-repositories repository))
          (assoc base :checkout-status :repository-mismatch
                 :actual-repositories remote-repositories)
          :else (merge base {:checkout-status :available}
                       (file-state checkout workflow-revision runtime-revision workflow-repo)))))))

(defn plan
  "Return observable checkout and workflow states without executing package commands."
  [{:keys [root checkouts revision runtime-revision] :as options}]
  {:revision revision
   :octave-revision (:octave-revision options)
   :runtime-revision (or runtime-revision revision)
   :workflow-path review/workflow-path
   :targets (mapv (partial inspect-target (path/resolve (or root default-root))
                           checkouts (or runtime-revision revision))
                  (review/route-targets
                    (review/select-targets (review/targets project/sources) options)
                    options))})

(defn- writable? [{:keys [checkout-status workflow-status]}]
  (and (= :available checkout-status)
       (contains? #{:absent :matching :revision-mismatch} workflow-status)))

(defn apply-plan!
  "Preflight the entire selected scope, then create/update only recognized managed callers."
  [{:keys [runtime-revision targets] :as rollout}]
  (when-not (every? writable? targets)
    (throw (ex-info "Rollout refused: selected checkout unavailable, unsafe, or unmanaged"
                    {:targets (filterv (complement writable?) targets)})))
  (assoc rollout :targets
           (mapv (fn [{:keys [checkout workflow-status workflow-repo workflow-revision] :as target}]
                   (if (= :matching workflow-status)
                     (assoc target :action :unchanged)
                     (let [file (path/join checkout review/workflow-path)]
                       (fs/mkdirSync (path/dirname file) #js {:recursive true})
                       (fs/writeFileSync file (review/render-workflow workflow-revision
                                                                     runtime-revision workflow-repo)
                                         #js {:encoding "utf8"})
                       (assoc target :action (if (= :absent workflow-status) :created :updated)
                              :workflow-status :matching))))
                 targets)))

(defn -main
  "Run the rollout CLI; report validation errors as exit 2 and check drift as exit 1."
  [& args]
  (try
    (let [{:keys [command] :as options} (review/parse-options args)
          rollout (plan options)
          result (if (= "apply" command) (apply-plan! rollout) rollout)]
      (prn result)
      (if (and (= "check" command)
               (not-every? #(and (= :available (:checkout-status %))
                                (= :matching (:workflow-status %)))
                           (:targets result)))
        1
        0))
    (catch :default error
      (binding [*out* *err*]
        (println (.-message error))
        (when-let [data (ex-data error)] (prn data)))
      2)))

(when (= nbb/*file* (nbb/invoked-file))
  (set! (.-exitCode js/process) (apply -main *command-line-args*)))
