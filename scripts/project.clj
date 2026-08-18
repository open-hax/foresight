;; SPDX-License-Identifier: GPL-3.0-or-later
(ns foresight-project
  (:require [foresight.law.project :as law]
            [foresight.project :as project]
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
             (:project/sources project/project)))
  0)

(defn validate! []
  (let [result (law/validate-project project/project (current-gitmodules))]
    (if (:valid? result)
      (do
        (println "PASS" (name (:project/id result))
                 (count (:project/sources project/project)) "sources"
                 (count (project/submodule-sources)) "submodules"
                 (count (:project/invariants project/project)) "invariants")
        0)
      (do
        (binding [*out* *err*]
          (println "FAIL" (name (:project/id result)))
          (doseq [error (:errors result)]
            (prn error)))
        1))))

(defn -main [& args]
  (try
    (case (first args)
      "show" (do (prn project/project) 0)
      "repos" (print-repos!)
      "validate" (validate!)
      (throw (js/Error.
              (str "Unknown command: " (or (first args) "<missing>")
                   ". Expected show, repos, or validate."))))
    (catch :default error
      (binding [*out* *err*]
        (println (.-message error)))
      2)))

(when (= nbb/*file* (nbb/invoked-file))
  (set! (.-exitCode js/process) (apply -main *command-line-args*)))
