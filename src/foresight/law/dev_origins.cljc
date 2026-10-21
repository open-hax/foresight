;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.law.dev-origins
  "Pure fork-map law; GitHub network facts are supplied by the caller."
  (:require [clojure.string :as str]))

(defn- github-repo [url]
  (second (re-matches #"(?:git@github.com:|https://github.com/)([^/]+/[^/]+)"
                      (str/replace (or url "") #"[.]git$" ""))))

(defn- duplicates [values]
  (->> values frequencies (keep (fn [[value n]] (when (> n 1) value))) (sort-by str)))

(defn- org-repo? [repo]
  (or (str/starts-with? (or repo "") "open-hax/")
      (str/starts-with? (or repo "") "octave-commons/")))

(defn validate
  "Validate dev-origin `rows` against `.gitmodules` `modules` and the caller's
  `network-roots` facts (org upstream -> GitHub fork-network root; an absent
  upstream is its own root). Coverage is keyed by submodule path: a module is
  covered when it points at an org repository, or at the dev origin of the row
  for its path."
  [rows modules network-roots]
  (let [row-by-path (into {} (map (juxt :source/path identity)) rows)
        module-repos (into {} (map (fn [{:keys [path url]}] [path (github-repo url)])) modules)
        org-modules (into {}
                          (filter (fn [[path repo]]
                                    (or (org-repo? repo)
                                        (and (some? repo)
                                             (= repo (:dev/origin (get row-by-path path)))))))
                          module-repos)
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
                      (when (and (contains? module-repos path)
                                 (not (contains? #{upstream fork} (get module-repos path))))
                        [(error :dev-origin/upstream-mismatch row)])
                      (when-not (and (string? fork) (re-matches #"riatzukiza/[A-Za-z0-9_.-]+" fork))
                        [(error :dev-origin/personal-owner row)])
                      (when-not (and (string? root) (re-matches #"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+" root))
                        [(error :dev-origin/network-root row)])
                      (when (and (string? root) (not= root (get network-roots upstream upstream)))
                        [(error :dev-origin/network-root-mismatch row)]))))
                 rows))]
    {:valid? (empty? errors) :errors (vec errors)}))
