;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.evidence
  (:require [clojure.string :as str]))

(def gate-kinds
  #{:static :unit :integration :e2e :coverage :security :build :live-smoke
    :independent-review})

(def executions #{:local :workflow-only :external})

(def outcomes #{:passed :failed :blocked :unavailable :not-applicable})

(def outcome-precedence
  {:failed 5
   :blocked 4
   :unavailable 3
   :not-applicable 2
   :passed 1})

(defn nonblank-string? [value]
  (and (string? value) (not (str/blank? value))))

(defn command? [value]
  (and (vector? value) (seq value) (every? nonblank-string? value)))

(defn sha256? [value]
  (and (nonblank-string? value)
       (boolean (re-matches #"[0-9a-f]{64}" value))))

(defn catalog-identity? [value]
  (and (map? value)
       (nonblank-string? (:catalog/path value))
       (sha256? (:catalog/sha256 value))))

(defn source-identity? [value]
  (and (map? value)
       (nonblank-string? (:source/path value))
       (nonblank-string? (:source/repository value))
       (or (nil? (:source/revision value))
           (nonblank-string? (:source/revision value)))))

(defn gate-errors
  [repository-path gate]
  (cond-> []
    (not (keyword? (:gate/id gate)))
    (conj {:error :gate/id :repository repository-path :gate gate})

    (not (contains? gate-kinds (:gate/kind gate)))
    (conj {:error :gate/kind :repository repository-path :gate gate})

    (not (contains? executions (:gate/execution gate)))
    (conj {:error :gate/execution :repository repository-path :gate gate})

    (not (nonblank-string? (:gate/source gate)))
    (conj {:error :gate/source :repository repository-path :gate gate})

    (and (= :local (:gate/execution gate))
         (not (command? (:gate/command gate))))
    (conj {:error :gate/command :repository repository-path :gate gate})

    (and (not= :local (:gate/execution gate))
         (not (nonblank-string? (:gate/reason gate))))
    (conj {:error :gate/reason :repository repository-path :gate gate})))

(defn repository-errors
  [catalog-key repository]
  (let [path (:repository/path repository)
        gates (:repository/gates repository)]
    (into
     (cond-> []
       (not= catalog-key path)
       (conj {:error :repository/path
              :catalog-key catalog-key
              :repository/path path})

       (not (vector? gates))
       (conj {:error :repository/gates :repository catalog-key}))
     (mapcat #(gate-errors catalog-key %))
     (if (vector? gates) gates []))))

(defn duplicate-gate-ids [repositories]
  (->> repositories
       vals
       (mapcat (fn [repository]
                 (let [gates (:repository/gates repository)]
                   (if (vector? gates) gates []))))
       (keep :gate/id)
       frequencies
       (keep (fn [[gate-id count]] (when (> count 1) gate-id)))
       sort
       vec))

(defn catalog-errors
  [catalog]
  (let [repositories (:catalog/repositories catalog)
        duplicates (when (map? repositories)
                     (duplicate-gate-ids repositories))]
    (cond-> []
      (not= 1 (:catalog/version catalog))
      (conj {:error :catalog/version :value (:catalog/version catalog)})

      (not (map? repositories))
      (conj {:error :catalog/repositories})

      (map? repositories)
      (into (mapcat (fn [[catalog-key repository]]
                      (repository-errors catalog-key repository))
                    repositories))

      (seq duplicates)
      (conj {:error :gate/id-duplicates :gate/ids duplicates}))))

(defn valid-catalog? [catalog]
  (empty? (catalog-errors catalog)))

(defn catalog-inventory-errors [catalog actionable-submodule-paths]
  (let [actionable-submodule-paths (set actionable-submodule-paths)]
    (->> (:catalog/repositories catalog)
         keys
         (remove actionable-submodule-paths)
         sort
         (mapv (fn [repository-path]
                 {:error :catalog/repository-not-actionable-submodule
                  :repository repository-path})))))

(defn select-gates
  ([catalog repository-paths]
   (select-gates catalog repository-paths gate-kinds))
  ([catalog repository-paths kinds]
   (let [repositories (:catalog/repositories catalog)]
     (->> repository-paths
          (mapcat #(get-in repositories [% :repository/gates] []))
          (filter #(contains? kinds (:gate/kind %)))
          vec))))

(defn explicit-not-applicable? [result]
  (and (= :not-applicable (:result/outcome result))
       (nonblank-string? (:result/reason result))
       (nonblank-string? (:result/approved-by result))))

(defn exit-code? [value]
  (and (integer? value) (not (neg? value))))

(defn result-errors [result]
  (let [execution (:result/execution result)
        source (:result/source result)
        revision (:result/revision result)]
    (cond-> []
    (not (keyword? (:gate/id result)))
    (conj {:error :result/gate-id :result result})

    (not (contains? outcomes (:result/outcome result)))
    (conj {:error :result/outcome :result result})

    (and (= :not-applicable (:result/outcome result))
         (not (explicit-not-applicable? result)))
    (conj {:error :result/not-applicable-approval :result result})

    (not (contains? executions execution))
    (conj {:error :result/execution :result result})

    (and (= :local execution)
         (not (command? (:result/command result))))
    (conj {:error :result/command :result result})

    (and (contains? result :result/exit)
         (not (exit-code? (:result/exit result))))
    (conj {:error :result/exit :result result})

    (and (= :local execution)
         (= :passed (:result/outcome result))
         (not= 0 (:result/exit result)))
    (conj {:error :result/local-passed-exit :result result})

    (and (= :local execution)
         (not= :passed (:result/outcome result))
         (= 0 (:result/exit result)))
    (conj {:error :result/local-nonpass-exit :result result})

    (and (not= :local execution)
         (contains? result :result/exit))
    (conj {:error :result/nonlocal-exit :result result})

    (not (catalog-identity? (:result/catalog result)))
    (conj {:error :result/catalog :result result})

    (not (source-identity? source))
    (conj {:error :result/source :result result})

    (and (= :passed (:result/outcome result))
         (not (nonblank-string? revision)))
    (conj {:error :result/passed-revision :result result})

    (and (nonblank-string? revision)
         (not= revision (:source/revision source)))
    (conj {:error :result/source-revision :result result}))))

(defn valid-result? [result]
  (empty? (result-errors result)))

(defn satisfied? [result]
  (or (= :passed (:result/outcome result))
      (explicit-not-applicable? result)))

(defn gate-index [catalog]
  (into {}
        (mapcat (fn [[repository-path repository]]
                  (map (fn [gate]
                         [(:gate/id gate)
                          {:repository/path repository-path
                           :gate gate}])
                       (:repository/gates repository))))
        (:catalog/repositories catalog)))

(defn result-matches-gate?
  [catalog-identity target-revision result
   {:keys [repository/path gate]}]
  (and (= catalog-identity (:result/catalog result))
       (= (:gate/execution gate) (:result/execution result))
       (= (:gate/command gate) (:result/command result))
       (= {:source/path (:gate/source gate)
           :source/repository path
           :source/revision target-revision}
          (:result/source result))))

(defn summarize-results [results]
  (let [errors (vec (mapcat result-errors results))]
    (if (seq errors)
      {:result/outcome :failed
       :result/counts (frequencies (map :result/outcome results))
       :result/satisfied? false
       :result/errors errors}
      (let [counts (frequencies (map :result/outcome results))
            overall (if (seq results)
                      (->> results
                           (map :result/outcome)
                           (apply max-key outcome-precedence))
                      :unavailable)]
        {:result/outcome overall
         :result/counts (into (sorted-map) counts)
         :result/satisfied? (and (seq results) (every? satisfied? results))}))))

(defn promotion-ready?
  [catalog catalog-identity target-revision required-gate-ids results]
  (let [required-gate-ids (set required-gate-ids)
        required-results (filterv #(contains? required-gate-ids (:gate/id %))
                                  results)
        gate-id-counts (frequencies (map :gate/id required-results))
        by-id (into {} (map (juxt :gate/id identity)) required-results)
        trusted-gates (gate-index catalog)]
    (and (valid-catalog? catalog)
         (catalog-identity? catalog-identity)
         (nonblank-string? target-revision)
         (seq required-gate-ids)
         (every? #(contains? trusted-gates %) required-gate-ids)
         (every? #(= 1 (get gate-id-counts % 0)) required-gate-ids)
         (every? (fn [gate-id]
                   (when-let [result (get by-id gate-id)]
                     (and (valid-result? result)
                          (satisfied? result)
                          (= target-revision (:result/revision result))
                          (result-matches-gate?
                           catalog-identity
                           target-revision
                           result
                           (get trusted-gates gate-id)))))
                 required-gate-ids))))
