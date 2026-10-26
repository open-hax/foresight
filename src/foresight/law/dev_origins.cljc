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
  "Validate dev-origin `rows` against `.gitmodules` `modules`, the caller's
  `network-roots` facts (org upstream -> GitHub fork-network root; an absent
  upstream is its own root) and `required-upstreams`, the manifest-derived map
  of submodule path -> declared org upstream for every path that must have a
  row (independently owned repositories; nested canonical inputs such as
  .agents are left out by the caller). A row's `:org/upstream` must equal the
  declared upstream for its path, so a dev-pointed module cannot smuggle in an
  unrelated upstream and network root.
  Coverage is keyed by submodule path: a path is required when the manifest
  requires it or its module points at an org repository. A required module may
  point at its row's dev origin (see upstream-mismatch), but a row's dev origin
  never adds coverage, so a nested canonical input cannot cover itself.
  Every supplied required path needs a row, even when it is absent from
  `modules`. Required paths never come from the rows, so deleting a row cannot
  hide its path."
  [rows modules network-roots required-upstreams]
  (let [module-repos (into {} (map (fn [{:keys [path url]}] [path (github-repo url)])) modules)
        ;; Coverage never comes from a row's own :dev/origin: a dev-pointed
        ;; module is covered only through required-upstreams, so a nested canonical
        ;; input (.agents -> riatzukiza/.agents) cannot cover itself.
        org-paths (keep (fn [[path repo]] (when (org-repo? repo) path)) module-repos)
        paths (into (set org-paths) (keys required-upstreams))
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
                         declared (get required-upstreams path)
                         fork (:dev/origin row)
                         root (:network/root row)]
                     (concat
                      (when (or (and (some? declared) (not= upstream declared))
                                (and (contains? module-repos path)
                                     (not (contains? #{upstream fork} (get module-repos path)))))
                        [(error :dev-origin/upstream-mismatch row)])
                      (when-not (and (string? fork) (re-matches #"riatzukiza/[A-Za-z0-9_.-]+" fork))
                        [(error :dev-origin/personal-owner row)])
                      (when-not (and (string? root) (re-matches #"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+" root))
                        [(error :dev-origin/network-root row)])
                      (when (and (string? root) (not= root (get network-roots upstream upstream)))
                        [(error :dev-origin/network-root-mismatch row)]))))
                 rows))]
    {:valid? (empty? errors) :errors (vec errors)}))
