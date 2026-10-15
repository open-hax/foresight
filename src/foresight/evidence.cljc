;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.evidence
  (:require [clojure.string :as str]))

(def gate-kinds
  #{:static :unit :integration :e2e :coverage :security :build :live-smoke
    :independent-review})

(def executions #{:local :workflow-only :external})

(def outcomes #{:passed :failed :blocked :unavailable :not-applicable})

(def receipt-ledger-path ".ημ/receipts.edn")

;; New receipts are immutable segment files in this directory. The legacy
;; `receipt-ledger-path` file is the first, frozen segment.
(def receipt-segment-directory ".ημ/receipts")

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

(def ^:private receipt-ts-pattern
  ;; RFC 3339 §5.6 permits lowercase t/z as well as uppercase T/Z.
  #"(\d{4})-(\d{2})-(\d{2})[Tt](\d{2}):(\d{2}):(\d{2})(?:\.(\d+))?(?:([Zz])|([+-])(\d{2}):(\d{2}))")

(defn- parse-decimal [digits]
  #?(:clj (Long/parseLong digits)
     :cljs (js/parseInt digits 10)))

(defn leap-year? [year]
  (or (and (zero? (mod year 4)) (pos? (mod year 100)))
      (zero? (mod year 400))))

(defn days-in-month [year month]
  (case month
    2 (if (leap-year? year) 29 28)
    (4 6 9 11) 30
    31))

(defn- days-from-civil
  "Days since 1970-01-01 for a proleptic Gregorian date (Hinnant's algorithm)."
  [year month day]
  (let [year (if (<= month 2) (dec year) year)
        era (quot (if (neg? year) (- year 399) year) 400)
        year-of-era (- year (* era 400))
        shifted-month (mod (+ month 9) 12)
        day-of-year (+ (quot (+ (* 153 shifted-month) 2) 5) (dec day))
        day-of-era (+ (* 365 year-of-era)
                      (quot year-of-era 4)
                      (- (quot year-of-era 100))
                      day-of-year)]
    (+ (* era 146097) day-of-era -719468)))

(def ^:private leap-second-days
  "UTC dates whose last minute had an inserted leap second (23:59:60), as
  announced by IERS Bulletin C. No leap second has been inserted since
  2016-12-31; extend this table if IERS announces another."
  (into #{}
        (map (fn [[year month day]] (days-from-civil year month day)))
        [[1972 6 30] [1972 12 31] [1973 12 31] [1974 12 31] [1975 12 31]
         [1976 12 31] [1977 12 31] [1978 12 31] [1979 12 31] [1981 6 30]
         [1982 6 30] [1983 6 30] [1985 6 30] [1987 12 31] [1989 12 31]
         [1990 12 31] [1992 6 30] [1993 6 30] [1994 6 30] [1995 12 31]
         [1997 6 30] [1998 12 31] [2005 12 31] [2008 12 31] [2012 6 30]
         [2015 6 30] [2016 12 31]]))

(defn- leap-second-at? [utc-seconds]
  (and (= 86399 (mod utc-seconds 86400))
       (contains? leap-second-days (quot (- utc-seconds 86399) 86400))))

(defn receipt-ts-instant
  "Parses an RFC 3339 timestamp into `[utc-epoch-seconds leap fraction-digits]`,
  or nil unless it names a real calendar date, clock time, and known UTC
  offset. Rejects `-00:00` (RFC 3339 unknown-offset). Second 60 is accepted
  only at an actual inserted leap second (23:59:60 UTC, in any offset); it
  reads as the preceding second with `leap` 1, so it orders after 23:59:59
  and before the next day. Fraction digits drop trailing zeros, so equal
  instants compare equal and fractions of any precision order correctly as
  strings."
  [ts]
  (when (string? ts)
    (when-let [[_ year month day hour minute second fraction zulu sign
                offset-hour offset-minute]
               (re-matches receipt-ts-pattern ts)]
      (let [[year month day hour minute second]
            (map parse-decimal [year month day hour minute second])
            offset-hour (if zulu 0 (parse-decimal offset-hour))
            offset-minute (if zulu 0 (parse-decimal offset-minute))]
        (when (and (<= 1 month 12)
                   (<= 1 day (days-in-month year month))
                   (< hour 24) (< minute 60) (<= second 60)
                   (< offset-hour 24) (< offset-minute 60)
                   ;; RFC 3339: -00:00 means the local offset is unknown, so the
                   ;; timestamp does not name a known UTC instant.
                   (not (and (= "-" sign)
                             (zero? offset-hour)
                             (zero? offset-minute))))
          (let [offset-seconds (* (if (= "-" sign) -1 1)
                                  (+ (* 3600 offset-hour) (* 60 offset-minute)))
                leap (if (= 60 second) 1 0)
                utc-seconds (- (+ (* 86400 (days-from-civil year month day))
                                  (* 3600 hour) (* 60 minute) (- second leap))
                               offset-seconds)]
            (when (or (zero? leap) (leap-second-at? utc-seconds))
              [utc-seconds leap (str/replace (or fraction "") #"0+$" "")])))))))

(defn canonical-receipt-ts?
  "True when `ts` is an RFC 3339 string naming a real instant: a valid
  calendar date, clock time (second 60 only at an actual leap second), and
  UTC offset."
  [ts]
  (some? (receipt-ts-instant ts)))

(defn receipt-envelope? [receipt]
  (and (map? receipt)
       (keyword? (:kind receipt))
       (canonical-receipt-ts? (:ts receipt))
       (every? nonblank-string?
               ((juxt :origin :owner :dod :pi :host) receipt))
       (vector? (:manifest receipt))
       (every? nonblank-string? (:manifest receipt))
       (vector? (:refs receipt))
       (every? nonblank-string? (:refs receipt))))

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

;; ---------------------------------------------------------------------------
;; Segmented Receipt River ledger law (pure). Adapters supply bytes and a
;; content-hash function; ordering and duplicate rules live here.

(defn- persistent-queue?
  "True for a cljs/JVM persistent queue (#queue ...), which is sequential
  and equal to a list or vector of the same elements but is a distinct EDN
  tagged collection."
  [value]
  #?(:clj (instance? clojure.lang.PersistentQueue value)
     :cljs (instance? PersistentQueue value)))

(defn canonical-form
  "An order-independent representation of an EDN value: map entries and set
  members are sorted, and vectors, lists and queues stay distinct, so equal
  values always print identically and unequal values never collide."
  [value]
  (cond
    (map? value)
    [:map (vec (sort-by pr-str
                        (map (fn [[k v]] [(canonical-form k) (canonical-form v)])
                             value)))]
    (set? value) [:set (vec (sort-by pr-str (map canonical-form value)))]
    (vector? value) [:vector (mapv canonical-form value)]
    (persistent-queue? value) [:queue (mapv canonical-form value)]
    (sequential? value) [:list (mapv canonical-form value)]
    :else value))

(defn repeated-ledger-records
  "Groups of segment paths that hold the same record ID."
  [id-fn segments]
  (->> (for [{:segment/keys [path records]} segments
             record records]
         {:path path :id (id-fn record)})
       (group-by :id)
       vals
       (filter #(< 1 (count %)))
       (mapv #(mapv :path %))))

(defn receipt-ts-sort-key
  "Total sort key for a record timestamp. Real instants compare by UTC time;
  anything else (historical free-form values, or no :ts) sorts first by its
  canonical printed form. Every key has the same shape and element types."
  [ts]
  (if-let [[seconds leap fraction] (receipt-ts-instant ts)]
    [1 seconds leap fraction ""]
    [0 0 0 "" (pr-str (canonical-form ts))]))

(defn merge-ledger-records
  "Merges segment records into one ledger ordered by the instant named by
  `:ts`, then record ID. Historical records whose `:ts` predates the
  canonical-instant rule still merge; new records are held to that rule by
  `receipt-envelope?` once the extension check identifies them."
  [id-fn segments]
  (->> (for [{:segment/keys [records]} segments
             record records]
         {:record record :id (id-fn record)})
       (sort-by (fn [{:keys [record id]}]
                  (conj (receipt-ts-sort-key (when (map? record) (:ts record)))
                        id)))
       (mapv :record)))
