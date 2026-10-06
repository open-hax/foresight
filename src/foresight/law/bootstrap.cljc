;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.law.bootstrap
  "Portable bootstrap boundary shapes. Git and host observations are data."
  (:require [foresight.evidence :as evidence]
            [foresight.law.project :as project-law]))

(def planning-input-shape
  {:required #{:bootstrap/root-revision :bootstrap/project
               :bootstrap/manifest :bootstrap/gitlinks}
   :gitlink/required #{:path :mode :revision}
   :gitlink/mode "160000"})

(def assessment-input-shape
  {:checkout/required #{:source/path :source/revision :checkout/initialized?
                        :checkout/fetch :bootstrap/root-revision}
   :gate/required #{:gate/id :gate/outcome :bootstrap/root-revision}
   :required-gates :nonempty-keyword-set
   :passing-outcome :passed})

(defn records? [value]
  (and (sequential? value) (every? map? value)))

(defn project-shape? [value]
  (and (map? value)
       (keyword? (:project/id value))
       (every? #(records? (get value %))
               [:project/sources :project/native-components :project/invariants])))

(defn required-gates? [value]
  (and (set? value) (seq value) (every? keyword? value)))

(defn revision? [value]
  (and (evidence/git-commit-id? value)
       (boolean (re-find #"[1-9a-f]" value))))

(defn child? [value]
  (and (map? value)
       (keyword? (:source/id value))
       (project-law/confined-relative-path? (:source/path value))
       (evidence/nonblank-string? (:source/repository value))
       (revision? (:source/revision value))
       (boolean? (:source/actionable? value))
       (boolean? (:source/consolidation? value))
       (not (and (:source/consolidation? value) (:source/actionable? value)))))

(defn planned? [value]
  (and (map? value)
       (= :planned (:bootstrap/status value))
       (revision? (:bootstrap/root-revision value))
       (records? (:bootstrap/children value))
       (seq (:bootstrap/children value))
       (every? child? (:bootstrap/children value))
       (empty? (project-law/duplicates (map :source/path (:bootstrap/children value))))
       (empty? (project-law/duplicates (map :source/id (:bootstrap/children value))))))
