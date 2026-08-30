;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.law.lineage
  "Pure laws for revision-bound ancestral evidence. Historical sources may
   inform Foresight, but they never gain workspace or execution authority from
   appearing in this inventory."
  (:require [clojure.string :as str]
            [foresight.law.project :as project-law]))

(def allowed-source-types
  #{:historical-prototype})

(def allowed-source-semantic-statuses
  #{:proposed/ancestral-prototype})

(def allowed-epistemic-statuses
  #{:provisional})

(def allowed-claim-statuses
  #{:live
    :live-with-revision
    :recurring-pressure
    :rejected-implementation})

(defn duplicates [values]
  (->> values
       frequencies
       (keep (fn [[value n]] (when (> n 1) value)))
       (sort-by pr-str)
       vec))

(defn full-commit-sha? [value]
  (and (string? value)
       (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- nonblank-string? [value]
  (and (string? value)
       (not (str/blank? value))))

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
  (mapv #(law-error law-id key-path "Duplicate lineage identity" nil %)
        (duplicates values)))

(defn- claim-errors [source-index claim-index claim]
  (let [path [:lineage/sources source-index :source/claims claim-index]
        status (:claim/status claim)
        carried-by (:claim/carried-by claim)]
    (vec
     (concat
      (when-not (keyword? (:claim/id claim))
        [(law-error :foresight/lineage-claim-id
                    (conj path :claim/id)
                    "Lineage claims require a keyword identity"
                    :keyword
                    (:claim/id claim))])
      (when-not (contains? allowed-claim-statuses status)
        [(law-error :foresight/lineage-claim-status
                    (conj path :claim/status)
                    "Lineage claim status must be a supported continuity status"
                    allowed-claim-statuses
                    status)])
      (when-not (contains? allowed-epistemic-statuses
                           (:claim/epistemic-status claim))
        [(law-error :foresight/lineage-claim-provisional
                    (conj path :claim/epistemic-status)
                    "Recovered lineage claims remain provisional until explicitly promoted"
                    allowed-epistemic-statuses
                    (:claim/epistemic-status claim))])
      (when-not (= :interpretation (:claim/recovery claim))
        [(law-error :foresight/lineage-claim-recovery
                    (conj path :claim/recovery)
                    "Recovered lineage claims must declare that they are interpretations"
                    :interpretation
                    (:claim/recovery claim))])
      (when-not (nonblank-string? (:claim/statement claim))
        [(law-error :foresight/lineage-claim-statement
                    (conj path :claim/statement)
                    "Lineage claims require a nonblank recovered statement"
                    :nonblank-string
                    (:claim/statement claim))])
      (when (contains? #{:live :recurring-pressure} status)
        (concat
         (when-not (and (vector? carried-by) (seq carried-by))
           [(law-error :foresight/lineage-claim-carriers
                       (conj path :claim/carried-by)
                       "Live claims and recurring pressures require explicit current carriers"
                       :nonempty-keyword-vector
                       carried-by)])
         (when (and (vector? carried-by)
                    (not (every? keyword? carried-by)))
           [(law-error :foresight/lineage-claim-carrier-id
                       (conj path :claim/carried-by)
                       "Current carrier identities must be keywords"
                       :keyword-vector
                       carried-by)])))
      (when (and (= :live-with-revision status)
                 (not (nonblank-string? (:claim/revision claim))))
        [(law-error :foresight/lineage-claim-revision
                    (conj path :claim/revision)
                    "A live-with-revision claim requires the surviving revised statement"
                    :nonblank-string
                    (:claim/revision claim))])
      (when (and (= :rejected-implementation status)
                 (not (nonblank-string?
                       (:claim/surviving-proposition claim))))
        [(law-error :foresight/lineage-claim-surviving-proposition
                    (conj path :claim/surviving-proposition)
                    "A rejected implementation must preserve the proposition that survived it"
                    :nonblank-string
                    (:claim/surviving-proposition claim))])))))

(defn- evidence-errors [source-index source]
  (let [evidence (:source/evidence source)
        source-revision (:source/revision source)
        path [:lineage/sources source-index :source/evidence]]
    (vec
     (concat
      (when-not (and (vector? evidence) (seq evidence))
        [(law-error :foresight/lineage-source-evidence
                    path
                    "Historical sources require at least one pinned evidence record"
                    :nonempty-vector
                    evidence)])
      (mapcat
       (fn [[evidence-index record]]
         (let [record-path (conj path evidence-index)]
           (concat
            (when-not (= :repository-document (:evidence/kind record))
              [(law-error :foresight/lineage-evidence-kind
                          (conj record-path :evidence/kind)
                          "Lineage evidence must name its artifact kind"
                          :repository-document
                          (:evidence/kind record))])
            (when-not (project-law/confined-relative-path?
                       (:evidence/path record))
              [(law-error :foresight/lineage-evidence-path-confined
                          (conj record-path :evidence/path)
                          "Lineage evidence requires a confined repository-relative artifact path"
                          :confined-relative-path
                          (:evidence/path record))])
            (when-not (= source-revision (:evidence/revision record))
              [(law-error :foresight/lineage-evidence-revision
                          (conj record-path :evidence/revision)
                          "Evidence must be pinned to the source revision"
                          source-revision
                          (:evidence/revision record))])
            (when-not (and (vector? (:evidence/supports record))
                           (seq (:evidence/supports record))
                           (every? keyword? (:evidence/supports record)))
              [(law-error :foresight/lineage-evidence-supports
                          (conj record-path :evidence/supports)
                          "Evidence must name the propositions it directly supports"
                          :nonempty-keyword-vector
                          (:evidence/supports record))]))))
       (map-indexed vector (if (vector? evidence) evidence [])))))))

(defn- source-errors [source-index source]
  (let [path [:lineage/sources source-index]
        claims (:source/claims source)]
    (vec
     (concat
      (when-not (keyword? (:source/id source))
        [(law-error :foresight/lineage-source-id
                    (conj path :source/id)
                    "Lineage sources require a keyword identity"
                    :keyword
                    (:source/id source))])
      (when-not (contains? allowed-source-types (:source/type source))
        [(law-error :foresight/lineage-source-type
                    (conj path :source/type)
                    "Lineage source type must preserve its non-workspace role"
                    allowed-source-types
                    (:source/type source))])
      (when-not (nonblank-string? (:source/repository source))
        [(law-error :foresight/lineage-source-repository
                    (conj path :source/repository)
                    "Lineage sources require a repository identity"
                    :nonblank-string
                    (:source/repository source))])
      (when-not (full-commit-sha? (:source/revision source))
        [(law-error :foresight/lineage-source-revision
                    (conj path :source/revision)
                    "Lineage sources must be pinned to a full lowercase commit SHA"
                    :full-lowercase-commit-sha
                    (:source/revision source))])
      (when-not (= :ancestral-prototype (:source/role source))
        [(law-error :foresight/lineage-source-role
                    (conj path :source/role)
                    "Historical prototypes must declare their ancestral role"
                    :ancestral-prototype
                    (:source/role source))])
      (when-not (false? (:source/actionable? source))
        [(law-error :foresight/lineage-source-non-actionable
                    (conj path :source/actionable?)
                    "Historical lineage sources cannot be actionable"
                    false
                    (:source/actionable? source))])
      (when (contains? source :source/path)
        [(law-error :foresight/lineage-source-no-workspace-path
                    (conj path :source/path)
                    "Historical lineage sources cannot claim a workspace path"
                    :absent
                    (:source/path source))])
      (when-not (= :none (:source/workspace-authority source))
        [(law-error :foresight/lineage-source-no-workspace-authority
                    (conj path :source/workspace-authority)
                    "Historical lineage sources have no workspace authority"
                    :none
                    (:source/workspace-authority source))])
      (when-not (= :none (:source/execution-authority source))
        [(law-error :foresight/lineage-source-no-execution-authority
                    (conj path :source/execution-authority)
                    "Historical lineage sources have no execution authority"
                    :none
                    (:source/execution-authority source))])
      (when-not (contains? allowed-source-semantic-statuses
                           (:source/semantic-status source))
        [(law-error :foresight/lineage-source-semantic-status
                    (conj path :source/semantic-status)
                    "Historical lineage sources remain proposed until promoted by explicit evidence"
                    allowed-source-semantic-statuses
                    (:source/semantic-status source))])
      (when-not (contains? allowed-epistemic-statuses
                           (:source/epistemic-status source))
        [(law-error :foresight/lineage-source-provisional
                    (conj path :source/epistemic-status)
                    "Historical lineage sources remain provisional until explicitly promoted"
                    allowed-epistemic-statuses
                    (:source/epistemic-status source))])
      (evidence-errors source-index source)
      (when-not (and (vector? claims) (seq claims))
        [(law-error :foresight/lineage-source-claims
                    (conj path :source/claims)
                    "Historical lineage sources require recovered claims"
                    :nonempty-vector
                    claims)])
      (mapcat
       (fn [[claim-index claim]]
         (claim-errors source-index claim-index claim))
       (map-indexed vector (if (vector? claims) claims [])))))))

(defn inventory-errors [sources]
  (let [claims (mapcat :source/claims sources)]
    (vec
     (concat
      (duplicate-errors :foresight/lineage-source-id-unique
                        [:lineage/sources :source/id]
                        (map :source/id sources))
      (duplicate-errors :foresight/lineage-claim-id-unique
                        [:lineage/sources :claim/id]
                        (map :claim/id claims))
      (mapcat
       (fn [[source-index source]]
         (source-errors source-index source))
       (map-indexed vector sources))))))

(defn validate-inventory [sources]
  (let [errors (inventory-errors sources)
        claims (mapcat :source/claims sources)]
    {:valid? (empty? errors)
     :source/count (count sources)
     :claim/count (count claims)
     :errors errors}))

(defn assert-inventory! [sources]
  (let [result (validate-inventory sources)]
    (if (:valid? result)
      sources
      (throw (ex-info "Foresight lineage law failed" result)))))
