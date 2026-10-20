;; SPDX-License-Identifier: GPL-3.0-or-later
(ns project
  (:require [foresight.law.lineage :as lineage-law]
            [foresight.law.dev-origins :as dev-origins]
            [foresight.law.project :as law]
            [foresight.lineage :as lineage]
            [foresight.onboarding :as onboarding]
            [foresight.project :as project-model]
            [clojure.edn :as edn]
            [nbb.core :as nbb]
            [workspace :as workspace]
            ["fs" :as fs]
            ["path" :as path]))

(def root
  (path/resolve (path/dirname nbb/*file*) ".."))

(defn current-gitmodules []
  (-> (fs/readFileSync (path/join root ".gitmodules") "utf8")
      workspace/parse-gitmodules))

(defn print-repos! []
  (prn (mapv #(select-keys %
                           [:source/id
                            :source/path
                            :source/repository
                            :source/type
                            :source/ownership
                            :source/role
                            :source/actionable?])
             (:project/sources project-model/project)))
  0)

(defn print-lineage! []
  (prn lineage/sources)
  0)

(defn print-guide! []
  (print (onboarding/markdown project-model/project))
  0)

(defn validate! []
  (let [project-result
        (law/validate-project project-model/project (current-gitmodules))
        lineage-result
        (lineage-law/validate-inventory lineage/sources)
        origins-result
        (dev-origins/validate
         (edn/read-string (fs/readFileSync (path/join root "config/dev-origins.edn") "utf8"))
         (current-gitmodules))
        valid? (and (:valid? project-result)
                    (:valid? lineage-result)
                    (:valid? origins-result))]
    (if valid?
      (do
        (println "PASS" (name (:project/id project-result))
                 (count (:project/sources project-model/project))
                 "workspace sources"
                 (count (project-model/submodule-sources)) "submodules"
                 (count (:project/invariants project-model/project)) "invariants"
                 (:source/count lineage-result) "lineage sources"
                 (:claim/count lineage-result) "provisional claims")
        0)
      (do
        (binding [*out* *err*]
          (println "FAIL" (name (:project/id project-result)))
          (doseq [error (concat (:errors project-result)
                                (:errors lineage-result)
                                (:errors origins-result))]
            (prn error)))
        1))))

(defn -main [& args]
  (try
    (case (first args)
      "show" (do (prn project-model/project) 0)
      "repos" (print-repos!)
      "lineage" (print-lineage!)
      "guide" (print-guide!)
      "validate" (validate!)
      (throw (js/Error.
              (str "Unknown command: " (or (first args) "<missing>")
                   ". Expected show, repos, lineage, guide, or validate."))))
    (catch :default error
      (binding [*out* *err*]
        (println (.-message error)))
      2)))

(when (= nbb/*file* (nbb/invoked-file))
  (set! (.-exitCode js/process) (apply -main *command-line-args*)))
