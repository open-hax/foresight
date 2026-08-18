;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.law.project
  "Pure validation laws for the Foresight project declaration. Runtime checkout
   facts are supplied by adapters; this namespace performs no I/O."
  (:require [clojure.string :as str]))

(def executable-checks
  #{:source-ids-unique
    :source-paths-unique
    :source-paths-confined
    :gitmodules-match-project
    :consolidation-inputs-inventory-only
    :actionable-only-independent-submodules
    :source-invariants-resolve})

(defn duplicates [values]
  (->> values
       frequencies
       (keep (fn [[value n]] (when (> n 1) value)))
       (sort-by pr-str)
       vec))

(defn confined-relative-path? [value]
  (and (string? value)
       (not (str/blank? value))
       (not (str/starts-with? value "/"))
       (not (str/starts-with? value "\\"))
       (not (re-find #"^[A-Za-z]:[\\/]" value))
       (not (re-find #"(^|[\\/])\.{1,2}([\\/]|$)" value))
       (not (re-find #"[\\/]{2,}" value))
       (not (re-find #"[\\/]$" value))))

(defn- law-error
  ([law-id path message]
   {:law/id law-id :path path :message message})
  ([law-id path message expected actual]
   {:law/id law-id
    :path path
    :message message
    :expected expected
    :actual actual}))

(defn- duplicate-errors [law-id key-path values]
  (mapv #(law-error law-id key-path "Duplicate project identity" nil %)
        (duplicates values)))

(defn structural-errors [{:project/keys [sources native-components invariants]}]
  (let [invariant-ids (into #{} (map :invariant/id) invariants)
        invariant-refs (concat
                        (mapcat :source/invariants sources)
                        (mapcat :component/invariants native-components))]
    (vec
     (concat
      (duplicate-errors :foresight/source-id-unique
                        [:project/sources :source/id]
                        (map :source/id sources))
      (duplicate-errors :foresight/source-path-unique
                        [:project/sources :source/path]
                        (map :source/path sources))
      (duplicate-errors :foresight/component-id-unique
                        [:project/native-components :component/id]
                        (map :component/id native-components))
      (duplicate-errors :foresight/component-path-unique
                        [:project/native-components :component/path]
                        (map :component/path native-components))
      (duplicate-errors :foresight/invariant-id-unique
                        [:project/invariants :invariant/id]
                        (map :invariant/id invariants))
      (keep-indexed
       (fn [idx source]
         (when-not (confined-relative-path? (:source/path source))
           (law-error :foresight/source-path-confined
                      [:project/sources idx :source/path]
                      "Source path must be a confined direct relative path"
                      :confined-relative-path
                      (:source/path source))))
       sources)
      (keep-indexed
       (fn [idx source]
         (when (and (= :git-submodule (:source/type source))
                    (or (str/blank? (:source/repository source))
                        (str/blank? (:source/url source))))
           (law-error :foresight/git-submodule-identifiable
                      [:project/sources idx]
                      "Git submodules require repository identity and URL"
                      #{:source/repository :source/url}
                      (select-keys source [:source/repository :source/url]))))
       sources)
      (keep-indexed
       (fn [idx source]
         (when (and (:source/consolidation? source)
                    (:source/actionable? source))
           (law-error :foresight/consolidation-input-inventory-only
                      [:project/sources idx :source/actionable?]
                      "Consolidation inputs cannot be actionable"
                      false
                      true)))
       sources)
      (keep-indexed
       (fn [idx source]
         (when (and (:source/actionable? source)
                    (not (and (= :git-submodule (:source/type source))
                              (= :independent-repository (:source/ownership source)))))
           (law-error :foresight/actionable-source-is-independent-submodule
                      [:project/sources idx]
                      "Actionable sources must be independently owned direct Git submodules"
                      {:source/type :git-submodule
                       :source/ownership :independent-repository}
                      (select-keys source [:source/type :source/ownership]))))
       sources)
      (keep-indexed
       (fn [idx invariant]
         (let [check (:invariant/check invariant)]
           (when (and (= :executable (:invariant/enforcement invariant))
                      (not (contains? executable-checks check)))
             (law-error :foresight/executable-invariant-known
                        [:project/invariants idx :invariant/check]
                        "Executable invariant names a check implemented by the project law"
                        executable-checks
                        check))))
       invariants)
      (keep-indexed
       (fn [idx invariant-id]
         (when-not (contains? invariant-ids invariant-id)
           (law-error :foresight/source-invariant-resolves
                      [:project/invariant-references idx]
                      "Source/component invariant reference must resolve"
                      :declared-invariant
                      invariant-id)))
       invariant-refs)))))

(defn- submodule-index [submodules]
  (into {} (map (juxt :path identity)) submodules))

(defn submodule-drift-errors
  [{:project/keys [sources]} actual-submodules]
  (let [expected (->> sources
                      (filter #(= :git-submodule (:source/type %)))
                      (map (fn [source]
                             {:name (:source/name source)
                              :path (:source/path source)
                              :url (:source/url source)}))
                      vec)
        duplicate-actual-paths (duplicates (map :path actual-submodules))
        expected-by-path (submodule-index expected)
        actual-by-path (submodule-index actual-submodules)
        expected-paths (set (keys expected-by-path))
        actual-paths (set (keys actual-by-path))]
    (vec
     (concat
      (map #(law-error :foresight/gitmodule-path-unique
                       [:gitmodules :path]
                       "Direct .gitmodules paths must be unique"
                       nil
                       %)
           duplicate-actual-paths)
      (map #(law-error :foresight/gitmodule-source-present
                       [:gitmodules %]
                       "Declared project submodule is missing from .gitmodules"
                       (get expected-by-path %)
                       nil)
           (sort (clojure.set/difference expected-paths actual-paths)))
      (map #(law-error :foresight/gitmodule-source-declared
                       [:gitmodules %]
                       ".gitmodules contains an undeclared direct submodule"
                       nil
                       (get actual-by-path %))
           (sort (clojure.set/difference actual-paths expected-paths)))
      (keep
       (fn [path]
         (let [expected-source (get expected-by-path path)
               actual-source (get actual-by-path path)]
           (when (and actual-source
                      (not= (:url expected-source) (:url actual-source)))
             (law-error :foresight/gitmodule-url-matches
                        [:gitmodules path :url]
                        "Submodule URL differs from the project declaration"
                        (:url expected-source)
                        (:url actual-source)))))
       (sort expected-paths))
      (keep
       (fn [path]
         (let [expected-source (get expected-by-path path)
               actual-source (get actual-by-path path)]
           (when (and actual-source
                      (not= (:name expected-source) (:name actual-source)))
             (law-error :foresight/gitmodule-name-matches
                        [:gitmodules path :name]
                        "Submodule name differs from the project declaration"
                        (:name expected-source)
                        (:name actual-source)))))
       (sort expected-paths))))))

(defn project-errors
  ([project]
   (structural-errors project))
  ([project actual-submodules]
   (into (structural-errors project)
         (submodule-drift-errors project actual-submodules))))

(defn validate-project
  ([project]
   (let [errors (project-errors project)]
     {:valid? (empty? errors)
      :project/id (:project/id project)
      :errors errors}))
  ([project actual-submodules]
   (let [errors (project-errors project actual-submodules)]
     {:valid? (empty? errors)
      :project/id (:project/id project)
      :errors errors})))

(defn assert-project!
  ([project]
   (let [result (validate-project project)]
     (if (:valid? result)
       project
       (throw (ex-info "Foresight project law failed" result)))))
  ([project actual-submodules]
   (let [result (validate-project project actual-submodules)]
     (if (:valid? result)
       project
       (throw (ex-info "Foresight project law failed" result))))))
