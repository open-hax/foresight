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
(defn edn-text [value] (with-out-str (pprint/pprint (stable value))))
(defn json-text [value] (str (js/JSON.stringify (clj->js (stable value)) nil 2) "\n"))

(defn tracked-manifests [source]
  (let [relative (:source/path source)
        cwd (:absolute (workspace/inspect-source-path! root relative))
        _ (when-not (workspace/plain-path-stat (path/join cwd ".git"))
            (throw (ex-info "Child source is not initialized" {:path relative})))
        ownership (workspace/run-captured cwd ["git" "rev-parse" "--show-toplevel"])
        _ (when-not (and (zero? (:exit ownership)) (= cwd (:stdout ownership)))
            (throw (ex-info "Child Git ownership does not match its declared path" {:path relative :result ownership})))
        result (workspace/run-captured cwd ["git" "ls-files" "--" "package.json" "**/package.json"])]
    (when-not (zero? (:exit result))
      (throw (ex-info "Cannot inventory child manifests" {:path relative :result result})))
    (->> (str/split-lines (:stdout result))
         (remove str/blank?)
         (map #(str relative "/" %))
         ;; Git lists symlinks as files; do not follow one across ownership boundaries.
         (filter #(workspace/plain-file? (path/join root %)))
         sort vec)))

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

(defn outputs [policy packages inputs clio-deps clio-nbb]
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
        source-paths ["src" (str clio "/src")]
        deps-aliases (into {}
                           (keep (fn [source]
                                   (let [p (:source/path source)]
                                     (when (and (:source/actionable? source)
                                                (workspace/plain-file? (path/join root p "deps.edn")))
                                       [(keyword "module" p)
                                        {:extra-deps {(symbol "foresight.module" p) {:local/root p}}}]))))
                           (project/submodule-sources))
        metadata {:workspace/version 1 :node-projects (:node-projects policy)
                  :inputs inputs
                  :packages (mapv (fn [p] (merge (select-keys p [:path :sha256])
                                                {:name (get-in p [:manifest "name"])
                                                 :package-manager (get-in p [:manifest "packageManager"])})) packages)
                  :duplicate-package-names (duplicate-names packages)
                  :selected-dependency-declarations declarations
                  :dependency-overrides (:dependency-overrides policy)}]
    {"package.json" (json-text {"name" "@open-hax/foresight-workspace" "version" "0.0.0"
                                "private" true "packageManager" (:package-manager policy)
                                "scripts" (:root-scripts policy) "devDependencies" resolved})
     "pnpm-workspace.yaml" (str "# Generated by scripts/manifests.cljs; edit workspace.edn.\npackages:\n"
                                (apply str (map #(str "  - " (pr-str %) "\n") (:node-projects policy))))
     "deps.edn" (edn-text {:paths ["src"] :deps (:deps clio-deps)
                            :aliases (assoc deps-aliases :local {:extra-deps {'foresight/clio {:local/root clio}}})})
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
        paths (vec (sort (conj (vec (mapcat tracked-manifests sources)) "devtools/package.json")))
        child-deps (keep (fn [source]
                          (let [p (str (:source/path source) "/deps.edn")]
                            (when (workspace/plain-file? (path/join root p)) p))) sources)
        input-paths (concat [".gitmodules" "workspace.edn" "scripts/manifests.cljs" "src/foresight/project.cljc"]
                            paths child-deps
                            [(str (:clio policy) "/deps.edn") (str (:clio policy) "/nbb.edn")])
        inputs (into (sorted-map) (map (fn [p] [p (digest (read-text p))])) input-paths)]
    (outputs policy (mapv package-input paths) inputs
             (read-edn (str (:clio policy) "/deps.edn"))
             (read-edn (str (:clio policy) "/nbb.edn")))))

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
