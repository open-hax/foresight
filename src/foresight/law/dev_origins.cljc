;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.law.dev-origins
  "Pure fork-map law; GitHub network facts are supplied by the caller."
  (:require [clojure.string :as str]))

(defn- github-repo [url]
  (second (re-matches #"(?:git@github.com:|https://github.com/)([^/]+/[^/]+)"
                      (str/replace (or url "") #"[.]git$" ""))))

(defn- duplicates [values]
  (->> values frequencies (keep (fn [[value n]] (when (> n 1) value))) (sort-by str)))

(defn validate [rows modules]
  (let [org-modules (->> modules
                         (map (fn [{:keys [path url]}] [path (github-repo url)]))
                         (filter (fn [[_ repo]] (or (str/starts-with? (or repo "") "open-hax/")
                                                    (str/starts-with? (or repo "") "octave-commons/"))))
                         (into {}))
        paths (set (keys org-modules))
        present (set (map :source/path rows))
        error (fn [id value] {:law/id id :actual value})
        errors (concat
                (map #(error :dev-origin/missing %) (sort (remove present paths)))
                (map #(error :dev-origin/unknown-submodule %) (sort (remove paths present)))
                (map #(error :dev-origin/duplicate-path %) (duplicates (map :source/path rows)))
                (map #(error :dev-origin/duplicate-fork %) (duplicates (map :dev/origin rows)))
                (map #(error :dev-origin/duplicate-network %) (duplicates (map :network/root rows)))
                (mapcat
                 (fn [row]
                   (let [path (:source/path row)
                         upstream (:org/upstream row)
                         fork (:dev/origin row)
                         root (:network/root row)]
                     (concat
                      (when (and (contains? paths path) (not= upstream (get org-modules path)))
                        [(error :dev-origin/upstream-mismatch row)])
                      (when-not (and (string? fork) (re-matches #"riatzukiza/[A-Za-z0-9_.-]+" fork))
                        [(error :dev-origin/personal-owner row)])
                      (when-not (and (string? root) (re-matches #"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+" root))
                        [(error :dev-origin/network-root row)]))))
                 rows))]
    {:valid? (empty? errors) :errors (vec errors)}))
