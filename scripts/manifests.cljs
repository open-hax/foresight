;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns manifests
  "Deterministic root composition; tracked child manifests are inputs, never rewritten."
  (:require [cljs.reader :as edn]
            [cljs.pprint :as pprint]
            [clojure.string :as str]
            [foresight.project :as project]
            [nbb.core :as nbb]
            [workspace :as workspace]
            ["crypto" :as crypto]
            ["fs" :as fs]
            ["path" :as path]))

(def root (path/resolve (path/dirname nbb/*file*) ".."))
(defn read-text [relative] (fs/readFileSync (path/join root relative) "utf8"))
(defn read-edn [relative] (edn/read-string (read-text relative)))
(defn digest [text] (.digest (.update (crypto/createHash "sha256") text) "hex"))
(defn stable [value]
  (cond
    (map? value) (into (sorted-map-by #(compare (str %1) (str %2)))
                       (map (fn [[k v]] [k (stable v)])) value)
    (vector? value) (mapv stable value)
    (sequential? value) (map stable value)
    :else value))
(defn edn-text [value]
  (str/replace (with-out-str (pprint/pprint (stable value))) #"[\t ]+\n" "\n"))
(defn json-text [value] (str (js/JSON.stringify (clj->js (stable value)) nil 2) "\n"))

(defn source-owner [sources directory]
  (first (filter #(or (= directory (:source/path %))
                      (str/starts-with? directory (str (:source/path %) "/"))) sources)))

(defn verify-consumed-source! [source directories]
  (let [relative (:source/path source)
        cwd (:absolute (workspace/inspect-source-path! root relative))
        _ (when-not (workspace/plain-path-stat (path/join cwd ".git"))
            (throw (ex-info "Child source is not initialized" {:path relative})))
        ownership (workspace/run-captured cwd ["git" "rev-parse" "--show-toplevel"])
        _ (when-not (and (zero? (:exit ownership)) (= cwd (:stdout ownership)))
            (throw (ex-info "Child Git ownership does not match its declared path" {:path relative :result ownership})))
        recorded (workspace/run-captured root ["git" "ls-files" "--stage" "--" relative])
        pin (second (re-matches #"160000 ([0-9a-f]{40}) 0\t[^\n]+" (:stdout recorded)))
        current (workspace/run-captured cwd ["git" "rev-parse" "HEAD"])
        _ (when-not (and (zero? (:exit recorded)) pin (zero? (:exit current)) (= pin (:stdout current)))
            (throw (ex-info "Consumed child revision differs from the root gitlink"
                            {:path relative :recorded pin :actual (:stdout current)})))
        package-paths (mapv #(path/relative cwd (path/join root %)) directories)
        status (workspace/run-captured cwd (into ["git" "status" "--porcelain=v1" "--untracked-files=all" "--"] package-paths))]
    (when-not (and (zero? (:exit status)) (str/blank? (:stdout status)))
      (throw (ex-info "Consumed child package has uncommitted source changes"
                      {:path relative :packages directories :status (:stdout status)})))
    {:source/path relative :git/sha pin :packages (vec (sort directories))}))

(defn package-input [relative]
  {:path relative :sha256 (digest (read-text relative))
   :manifest (js->clj (js/JSON.parse (read-text relative)))})

(defn duplicate-names [packages]
  (->> packages
       (group-by #(get-in % [:manifest "name"]))
       (keep (fn [[package entries]]
               (when (and package (> (count entries) 1))
                 {:name package :paths (mapv :path entries)})))
       (sort-by :name) vec))

(defn resolve-dependencies [declarations overrides]
  (reduce-kv
   (fn [result dependency entries]
     (let [versions (set (map :version entries))
           override (get overrides dependency)]
       (when (and override (or (str/blank? (:version override))
                              (str/blank? (:reason override))))
         (throw (ex-info "Dependency override needs a version and explanation" {:dependency dependency})))
       (when (and (> (count versions) 1) (nil? override))
         (throw (ex-info "Conflicting selected dependency versions" {:dependency dependency :declarations entries})))
       (assoc result dependency (or (:version override) (first versions)))))
   (sorted-map) (group-by :name declarations)))

(defn outputs [policy packages inputs clio-deps clio-nbb revisions]
  (let [selected-paths (set (map #(str % "/package.json") (:node-projects policy)))
        selected (filter #(contains? selected-paths (:path %)) packages)
        _ (when-not (= selected-paths (set (map :path selected)))
            (throw (ex-info "A selected workspace package is missing" {:expected selected-paths})))
        _ (when (seq (duplicate-names selected))
            (throw (ex-info "Selected package names must be unique" {:duplicates (duplicate-names selected)})))
        declarations (concat
                      (for [package selected
                            [dependency version] (get-in package [:manifest "dependencies"])]
                        {:name dependency :version version :path (:path package)})
                      (for [[dependency version] (:tools policy)]
                        {:name dependency :version version :path "workspace.edn"}))
        resolved (resolve-dependencies declarations (:dependency-overrides policy))
        clio (:clio policy)
        local-libraries (into {} (map (fn [[lib directory]] [lib {:local/root directory}]))
                              (:local-libraries policy))
        source-paths ["src" (str clio "/src")]
        metadata {:workspace/version 1 :node-projects (:node-projects policy)
                  :inputs inputs
                  :source-revisions revisions
                  :packages (mapv (fn [p] (merge (select-keys p [:path :sha256])
                                                {:name (get-in p [:manifest "name"])
                                                 :package-manager (get-in p [:manifest "packageManager"])})) packages)
                  :duplicate-package-names (duplicate-names packages)
                  :selected-dependency-declarations declarations
                  :local-libraries (:local-libraries policy)
                  :dependency-overrides (:dependency-overrides policy)}]
    {"package.json" (json-text {"name" "@open-hax/foresight-workspace" "version" "0.0.0"
                                "private" true "packageManager" (:package-manager policy)
                                "scripts" (:root-scripts policy) "devDependencies" resolved})
     "pnpm-workspace.yaml" (str "# Generated by scripts/manifests.cljs; edit workspace.edn.\npackages:\n"
                                (apply str (map #(str "  - " (pr-str %) "\n") (:node-projects policy))))
     "deps.edn" (edn-text {:paths ["src"] :deps (:deps clio-deps)
                            :aliases {:local {:extra-deps local-libraries
                                              :override-deps local-libraries}}})
     "nbb.edn" (edn-text {:paths (into ["scripts" "test"] source-paths) :deps (:deps clio-nbb)})
     "shadow-cljs.edn" (edn-text {:source-paths (conj source-paths "test")
                                  :dependencies (mapv (fn [[lib coordinate]] [lib (:mvn/version coordinate)])
                                                      (sort-by (comp str key) (:deps clio-deps)))
                                  :builds {:local {:target :node-library :output-to "dist/local.cjs"
                                                   :exports {:ledgerSummary 'foresight.infra.local/ledger-summary}}
                                           :test {:target :node-test :output-to "dist/local-test.cjs"
                                                  :ns-regexp "foresight.infra.local-test$" :autorun false}}})
     "workspace-manifests.edn" (edn-text metadata)}))

(defn generate []
  (let [policy (read-edn "workspace.edn")
        sources (filter :source/actionable? (project/submodule-sources))
        directories (sort (set (concat (:node-projects policy) (vals (:local-libraries policy)) [(:clio policy)])))
        _ (doseq [directory directories]
            (workspace/inspect-source-path! root directory)
            (when-not (or (= directory "devtools") (source-owner sources directory))
              (throw (ex-info "Composition input is not an actionable child package or root devtools" {:path directory}))))
        revisions (->> directories
                       (group-by #(source-owner sources %))
                       (keep (fn [[source selected]] (when source (verify-consumed-source! source selected))))
                       (sort-by :source/path) vec)
        paths (mapv (fn [directory]
                      (let [file (str directory "/package.json")]
                        (when-not (workspace/plain-file? (path/join root file))
                          (throw (ex-info "Selected Node package requires package.json" {:path directory})))
                        file))
                    (sort (:node-projects policy)))
        local-deps (mapv (fn [[lib directory]]
                          (when-not (and (symbol? lib) (source-owner sources directory))
                            (throw (ex-info "Local library must belong to an actionable child" {:library lib :path directory})))
                          (workspace/inspect-source-path! root directory)
                          (let [file (str directory "/deps.edn")]
                            (when-not (workspace/plain-file? (path/join root file))
                              (throw (ex-info "Local library requires deps.edn" {:library lib :path directory})))
                            file))
                        (:local-libraries policy))
        input-paths (concat [".gitmodules" "workspace.edn" "scripts/manifests.cljs" "src/foresight/project.cljc"]
                            paths
                            local-deps
                            [(str (:clio policy) "/deps.edn") (str (:clio policy) "/nbb.edn")])
        inputs (into (sorted-map) (map (fn [p] [p (digest (read-text p))])) input-paths)]
    (outputs policy (mapv package-input paths) inputs
             (read-edn (str (:clio policy) "/deps.edn"))
             (read-edn (str (:clio policy) "/nbb.edn")) revisions)))

(defn drift [generated]
  (->> generated
       (keep (fn [[relative content]]
               (when (or (not (fs/existsSync (path/join root relative)))
                         (not= content (read-text relative))) relative)))
       sort vec))

(defn -main [& args]
  (try
    (let [mode (first args)
          _ (when-not (and (= 1 (count args)) (#{"--write" "--check"} mode))
              (throw (js/Error. "Usage: nbb scripts/manifests.cljs --write|--check")))
          generated (generate)
          changed (drift generated)]
      (if (= "--write" mode)
        (do (doseq [[relative content] generated]
              (fs/writeFileSync (path/join root relative) content))
            (println "Generated" (count generated) "root manifests") 0)
        (if (seq changed)
          (do (js/console.error "Generated manifest drift:" (str/join ", " changed)) 1)
          (do (println "PASS generated manifests") 0))))
    (catch :default error
      (js/console.error (ex-message error) (pr-str (ex-data error))) 2)))

(when (= nbb/*file* (nbb/invoked-file))
  (set! (.-exitCode js/process) (apply -main *command-line-args*)))
