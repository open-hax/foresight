;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.evidence
  (:require [clojure.string :as str]))

(def gate-kinds
  #{:static :unit :integration :e2e :coverage :security :build :live-smoke
    :independent-review})

(def executions #{:local :workflow-only :external})

(def outcomes #{:passed :failed :blocked :unavailable :not-applicable})

(def receipt-ledger-path ".ημ/receipts.edn")

(def evidence-receipt-origin "revision-bound-evidence")

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

(defn git-commit-id? [value]
  (and (nonblank-string? value)
       (boolean (re-matches #"(?:[0-9a-f]{64}|[0-9a-f]{40})" value))))

(defn catalog-identity? [value]
  (and (map? value)
       (nonblank-string? (:catalog/path value))
       (sha256? (:catalog/sha256 value))))

(defn source-identity? [value]
  (and (map? value)
       (nonblank-string? (:source/path value))
       (nonblank-string? (:source/repository value))
       (or (nil? (:source/revision value))
           (git-commit-id? (:source/revision value)))))

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
       (keep (fn [gate]
               (let [gate-id (:gate/id gate)]
                 (when (keyword? gate-id) gate-id))))
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
  (let [actionable-submodule-paths (set actionable-submodule-paths)
        repositories (:catalog/repositories catalog)]
    (if-not (map? repositories)
      []
      (->> repositories
           keys
           (remove actionable-submodule-paths)
           (sort-by pr-str)
           (mapv (fn [repository-path]
                   {:error :catalog/repository-not-actionable-submodule
                    :repository repository-path}))))))

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
  (if-not (map? result)
    [{:error :result/type :result result}]
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
             (not (git-commit-id? revision)))
        (conj {:error :result/passed-revision :result result})

        (and (nonblank-string? revision)
             (not= revision (:source/revision source)))
        (conj {:error :result/source-revision :result result})))))

(defn valid-result? [result]
  (empty? (result-errors result)))

(defn receipt-ledger-identity? [value]
  (and (map? value)
       (= receipt-ledger-path (:ledger/path value))
       (git-commit-id? (:ledger/revision value))
       (sha256? (:ledger/sha256 value))))

(defn receipt-envelope? [receipt]
  (and (map? receipt)
       (keyword? (:kind receipt))
       (every? nonblank-string?
               ((juxt :ts :origin :owner :dod :pi :host) receipt))
       (vector? (:manifest receipt))
       (every? nonblank-string? (:manifest receipt))
       (vector? (:refs receipt))
       (every? nonblank-string? (:refs receipt))))

(def canonical-observation-contract
  "Foresight's supported external observation shape, not the producer registry.
  Producer provenance: open-hax/eta-mu d98ab446d56ddfd24542df49b102b851f2a767fa,
  packages/receipt-river/src/cljs/eta_mu/receipt_river/law/receipt.cljs,
  SHA256 a40dd843cf298926dfbe229d5544470dbd51682fd538e841a0fad62d29bed320.
  The actual producer law separately validated both retained regression events.
  Other producer kinds, representations and versions require explicit admission;
  none of this grants revision-bound gate-result or promotion evidence credit."
  {:event/type :receipt-river/receipt-recorded
   :event/schema {:id :eta-mu.receipt-river/receipt-recorded :version 1}
   :producer/package "@eta-mu/receipt-river"
   :producer/admitted-versions #{"0.1.0"}
   :payload/kind :observation
   :payload/string-fields [:repo :origin :owner :dod :pi :host :manifest :refs]
   :event/keys #{:event/id :event/type :event/recorded-at :event/schema
                 :event/producer :event/subject :event/payload
                 :event/caused-by :event/causes :event/refs}})

(defn utc-receipt-timestamp?
  "Validate the consumer's UTC string representation, including fractional
  seconds, without converting or normalizing the immutable receipt."
  [value]
  (boolean
   (when (string? value)
     (when-let [[_ year month day hour minute second]
                (re-matches
                 #"(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(?:\.\d+)?Z"
                 value)]
       (let [[year month day hour minute second]
             (mapv parse-long [year month day hour minute second])
             leap? (or (zero? (mod year 400))
                       (and (zero? (mod year 4))
                            (not (zero? (mod year 100)))))
             last-day (case month
                        2 (if leap? 29 28)
                        (4 6 9 11) 30
                        31)]
         (and (<= 1 month 12)
              (<= 1 day last-day)
              (<= 0 hour 23)
              (<= 0 minute 59)
              (<= 0 second 59)))))))

(defn canonical-observation-receipt? [receipt]
  (let [producer (:event/producer receipt)
        subject (:event/subject receipt)
        payload (:event/payload receipt)]
    (boolean
     (and (map? receipt)
          (every? (:event/keys canonical-observation-contract) (keys receipt))
          (nonblank-string? (:event/id receipt))
          (re-matches
           #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
           (:event/id receipt))
          (= (:event/type canonical-observation-contract) (:event/type receipt))
          (= (:event/schema canonical-observation-contract)
             (:event/schema receipt))
          (utc-receipt-timestamp? (:event/recorded-at receipt))
          (map? producer)
          (= (:producer/package canonical-observation-contract)
             (:package/name producer))
          (contains? (:producer/admitted-versions canonical-observation-contract)
                     (:package/version producer))
          (nonblank-string? (:actor producer))
          (nonblank-string? (:command producer))
          (nonblank-string? (:eta-mu/version producer))
          (git-commit-id? (:source/commit producer))
          (map? subject)
          (nonblank-string? (:repo subject))
          (map? payload)
          (every? #(and (keyword? %) (nil? (namespace %))) (keys payload))
          (= (:payload/kind canonical-observation-contract) (:kind payload))
          (utc-receipt-timestamp? (:ts payload))
          (every? #(nonblank-string? (get payload %))
                  (:payload/string-fields canonical-observation-contract))
          (= (:repo subject) (:repo payload))
          (not= evidence-receipt-origin (:origin payload))
          (every? (fn [key]
                    (or (not (contains? receipt key))
                        (and (vector? (get receipt key))
                             (every? nonblank-string? (get receipt key)))))
                  [:event/caused-by :event/causes :event/refs])))))

(defn receipt-record?
  "Admit supported records without flattening envelopes or changing their bytes.
  Any event key selects the external contract, so a partial or mixed envelope
  cannot fall back to the legacy receipt contract."
  [receipt]
  (boolean
   (and (map? receipt)
        (if (some #(and (keyword? %) (= "event" (namespace %))) (keys receipt))
          (canonical-observation-receipt? receipt)
          (receipt-envelope? receipt)))))

(defn duplicate-appended-event-ids
  "Report exact event identities repeated by newly admitted records.
  Trusted history seeds identity reservations without revalidating, rewriting
  or deduplicating historical records. Legacy records without IDs add nothing.
  Record-shape admission remains the caller's separate obligation."
  [trusted-records appended-records]
  (let [event-ids (fn [records]
                    (keep #(when (map? %) (:event/id %)) records))
        counts (frequencies (concat (set (event-ids trusted-records))
                                    (event-ids appended-records)))]
    (->> (event-ids appended-records)
         (filter #(> (get counts % 0) 1))
         distinct
         vec)))

(defn evidence-receipt? [receipt]
  (let [schema (:evidence/schema receipt)]
    (and (receipt-envelope? receipt)
         (= :test-run (:kind receipt))
         (= evidence-receipt-origin (:origin receipt))
         (or (nil? schema)
             (and (= 2 schema)
                  (nonblank-string? (:evidence/adapter receipt))))
         (valid-result? (:evidence/result receipt)))))

(defn immutable-receipt-ledger? [ledger]
  (and (map? ledger)
       (receipt-ledger-identity? (:ledger/identity ledger))
       (vector? (:ledger/records ledger))))

(defn receipt-attests-result? [receipt result]
  (let [source (:result/source result)
        expected-ref (str (:source/repository source)
                          "@"
                          (:result/revision result))]
    (boolean
     (and (evidence-receipt? receipt)
          (= 2 (:evidence/schema receipt))
          (nonblank-string? (:evidence/adapter receipt))
          (= result (:evidence/result receipt))
          (some #{(:catalog/path (:result/catalog result))}
                (:manifest receipt))
          (some #{(:source/path source)} (:manifest receipt))
          (some #{expected-ref} (:refs receipt))
          (some #{(str (:gate/id result))} (:refs receipt))))))

(defn satisfied? [result]
  (or (= :passed (:result/outcome result))
      (explicit-not-applicable? result)))

(defn promotion-satisfied? [result]
  (= :passed (:result/outcome result)))

(defn automatic-promotion-supported? [gate]
  ;; Coverage needs a retained report, threshold, and baseline attestation.
  ;; Until that versioned contract lands, a zero exit must fail closed here.
  (not= :coverage (:gate/kind gate)))

(defn gate-index [catalog]
  (let [repositories (:catalog/repositories catalog)]
    (if-not (map? repositories)
      {}
      (into {}
            (mapcat
             (fn [[repository-path repository]]
               (let [gates (:repository/gates repository)]
                 (map (fn [gate]
                        [(:gate/id gate)
                         {:repository/path repository-path
                          :gate gate}])
                      (if (vector? gates) gates [])))))
            repositories))))

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
  (if-not (coll? results)
    {:result/outcome :failed
     :result/counts {}
     :result/satisfied? false
     :result/errors [{:error :results/type :results results}]}
    (let [errors (vec (mapcat result-errors results))]
      (if (seq errors)
        {:result/outcome :failed
         :result/counts (->> results
                             (map :result/outcome)
                             (filter outcomes)
                             frequencies
                             (into (sorted-map)))
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
           :result/satisfied? (boolean
                               (and (seq results)
                                    (every? satisfied? results)))})))))

(defn promotion-evidence-consistent?
  "Pure consistency over records authenticated by an effectful boundary.
  This predicate is not promotion authority."
  [catalog catalog-identity target-revision required-gate-ids results
   immutable-receipt-ledger]
  (if-not (and (coll? required-gate-ids)
               (coll? results)
               (immutable-receipt-ledger? immutable-receipt-ledger))
    false
    (let [required-gate-ids (set required-gate-ids)
          required-results (filterv #(contains? required-gate-ids (:gate/id %))
                                    results)
          gate-id-counts (frequencies (map :gate/id required-results))
          by-id (into {} (map (juxt :gate/id identity)) required-results)
          trusted-gates (gate-index catalog)
          receipts (:ledger/records immutable-receipt-ledger)]
      (boolean
       (and (valid-catalog? catalog)
            (catalog-identity? catalog-identity)
            (git-commit-id? target-revision)
            (seq required-gate-ids)
            (every? #(contains? trusted-gates %) required-gate-ids)
            (every? #(= 1 (get gate-id-counts % 0)) required-gate-ids)
            (every? (fn [gate-id]
                      (when-let [result (get by-id gate-id)]
                        (and (valid-result? result)
                             (promotion-satisfied? result)
                             (automatic-promotion-supported?
                              (:gate (get trusted-gates gate-id)))
                             (= target-revision (:result/revision result))
                             (some #(receipt-attests-result? % result)
                                   receipts)
                             (result-matches-gate?
                              catalog-identity
                              target-revision
                              result
                              (get trusted-gates gate-id)))))
                    required-gate-ids))))))
