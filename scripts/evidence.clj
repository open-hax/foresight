;; SPDX-License-Identifier: GPL-3.0-or-later
(ns evidence
  (:require [cljs.core :refer [clj->js]]
            [cljs.reader :as reader]
            [clojure.string :as str]
            [foresight.evidence :as law]
            [foresight.project :as project-model]
            [nbb.core :as nbb]
            [workspace :as workspace]
            ["child_process" :as child-process]
            ["crypto" :as crypto]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]))

(def root
  (path/resolve (path/dirname nbb/*file*) ".."))

(def catalog-file
  (path/join root "config" "quality-gates.edn"))

(def catalog-relative-path "config/quality-gates.edn")

(def receipt-file
  (path/join root ".ημ" "receipts.edn"))

(defn read-single-edn! [contents label]
  (try
    (let [forms (reader/read-string (str "[" contents "\n]"))]
      (when-not (= 1 (count forms))
        (throw (js/Error. (str label " must contain exactly one EDN form"))))
      (first forms))
    (catch :default error
      (throw (js/Error. (str "Invalid " label ": " (.-message error)))))))

(def max-receipt-line-bytes
  "Ceiling for one complete segment blob: the EDN record plus its mandatory
  trailing newline."
  (* 1024 1024))

(def max-receipt-batch-bytes
  "Ceiling for one immutable `git cat-file --batch` chunk of segment blobs.
  Each segment is capped at `max-receipt-line-bytes`; chunking keeps any one
  spawnSync buffer well under the former 1 GiB limit without capping the
  lifetime size of the ledger."
  (* max-receipt-line-bytes 64))

(def max-receipt-listing-bytes
  "Ceiling for one `git ls-tree` / metadata capture of the segment directory.
  Header lines are small; 32 MiB covers hundreds of thousands of segments."
  (* 32 1024 1024))

(def ^:dynamic *secure-append-phase-hook*
  (fn [_phase _context] nil))

(def bigint-stat-options #js {:bigint true})

(defn sha256 [value]
  (-> (crypto/createHash "sha256")
      (.update value)
      (.digest "hex")))

(defn decode-utf8! [bytes label]
  (try
    (.decode (js/TextDecoder. "utf-8" #js {:fatal true}) bytes)
    (catch :default error
      (throw (js/Error.
              (str "Invalid UTF-8 in " label ": " (.-message error)))))))

(defn read-catalog-bundle []
  (let [bytes (fs/readFileSync catalog-file)
        contents (decode-utf8! bytes "quality gate catalog")]
    {:catalog (read-single-edn! contents "quality gate catalog EDN")
     :catalog-identity {:catalog/path catalog-relative-path
                        :catalog/sha256 (sha256 bytes)}}))

(defn read-catalog []
  (:catalog (read-catalog-bundle)))

(defn git-capture! [args]
  (let [result (child-process/spawnSync
                "git"
                (clj->js (into ["--no-replace-objects"] args))
                #js {:cwd root :encoding "utf8" :shell false
                     :maxBuffer max-receipt-listing-bytes})
        error (.-error result)
        status (.-status result)]
    (when error
      (throw (js/Error. (str "Git invocation failed: " (.-message error)))))
    (when (nil? status)
      (throw (js/Error. "Git invocation ended without an exit status")))
    (when-not (zero? status)
      (throw (js/Error.
              (str "Git invocation failed with exit " status ": "
                   (str/trim (or (.-stderr result) ""))))))
    (or (.-stdout result) "")))

(defn git-buffer! [args]
  (let [result (child-process/spawnSync
                "git"
                (clj->js (into ["--no-replace-objects"] args))
                #js {:cwd root :shell false
                     :maxBuffer max-receipt-listing-bytes})
        error (.-error result)
        status (.-status result)]
    (when error
      (throw (js/Error. (str "Git invocation failed: " (.-message error)))))
    (when (nil? status)
      (throw (js/Error. "Git invocation ended without an exit status")))
    (when-not (zero? status)
      (let [stderr (.-stderr result)]
        (throw (js/Error.
                (str "Git invocation failed with exit " status ": "
                     (str/trim (if stderr
                                 (.toString stderr "utf8")
                                 "")))))))
    (or (.-stdout result) (js/Buffer.alloc 0))))

(defn edn-token-delimiter? [ch]
  (boolean (re-matches #"[\s,()\[\]{}\";]" ch)))

(defn edn-lexemes
  "Lexemes of one EDN text as [kind text] pairs. Kind is :string (raw
  contents between the quotes, escapes kept), :token, :open or :close.
  Whitespace, commas and comments are skipped. Structural brackets stay so
  a later pass can skip whole forms (including `#_` discards). Text inside
  strings, character literals and comments never becomes a token."
  [text]
  (let [n (count text)]
    (loop [i 0
           out []]
      (if (>= i n)
        out
        (let [ch (.charAt text i)]
          (cond
            (= "\"" ch)
            (let [end (loop [j (inc i)]
                        (cond
                          (>= j n) n
                          (= "\\" (.charAt text j)) (recur (+ j 2))
                          (= "\"" (.charAt text j)) (inc j)
                          :else (recur (inc j))))]
              (recur end (conj out [:string (subs text (inc i)
                                                  (max (inc i) (dec end)))])))

            (= ";" ch)
            (recur (let [j (.indexOf text "\n" i)] (if (neg? j) n j)) out)

            (contains? #{"(" "[" "{"} ch)
            (recur (inc i) (conj out [:open ch]))

            (contains? #{")" "]" "}"} ch)
            (recur (inc i) (conj out [:close ch]))

            (edn-token-delimiter? ch) (recur (inc i) out)

            :else
            (let [end (loop [j (if (= "\\" ch) (+ i 2) (inc i))]
                        (if (and (< j n) (not (edn-token-delimiter? (.charAt text j))))
                          (recur (inc j))
                          (min j n)))]
              (recur end (conj out [:token (subs text i end)])))))))))

(defn edn-number-lexemes
  "Numeric literal tokens of one EDN text. Strings, character literals and
  comments are skipped, and digits inside symbols or keywords are not
  numbers."
  [text]
  (into []
        (comp (filter #(= :token (first %)))
              (map second)
              ;; `##Inf`, `##-Inf` and `##NaN` are symbolic numbers.
              (filter #(re-find #"^(?:[+-]?[0-9]|##)" %)))
        (edn-lexemes text)))

(defn- edn-tag-token?
  "True for an EDN tagged-literal dispatch token such as `#inst` or
  `#uuid`. Not `#_` (discard) and not symbolic `##Inf`."
  [value]
  (boolean (and (string? value) (re-matches #"#[A-Za-z][^\"\\s]*" value))))

(defn- edn-skip-form
  "Index after one complete EDN form starting at `idx` in `lexemes`.
  Collections use matched :open/:close depth. A tag consumes its value.
  A `#_` discard skips its operand and then continues as another form
  (tools.reader discard semantics)."
  [lexemes idx]
  (let [[kind value] (get lexemes idx)]
    (cond
      (nil? kind) idx

      (= :string kind) (inc idx)

      (= :open kind)
      (loop [i (inc idx) depth 1]
        (let [[k _] (get lexemes i)]
          (cond
            (nil? k) i
            (= :open k) (recur (inc i) (inc depth))
            (= :close k) (if (= 1 depth)
                           (inc i)
                           (recur (inc i) (dec depth)))
            :else (recur (inc i) depth))))

      (and (= :token kind) (= "#_" value))
      (edn-skip-form lexemes (edn-skip-form lexemes (inc idx)))

      (and (= :token kind) (edn-tag-token? value))
      (edn-skip-form lexemes (inc idx))

      (= :token kind) (inc idx)

      :else (inc idx))))

(defn edn-inst-strings
  "Raw string contents of every real `#inst` tag in one EDN text. A `#inst`
  inside a string or comment is not a tag. Reader-discard forms (`#_ ...`)
  between the tag and its string are skipped, matching tools.reader. A tag
  not followed by a string is left to cljs.reader, which rejects it."
  [text]
  (let [lexemes (edn-lexemes text)
        n (count lexemes)]
    (loop [i 0
           out []]
      (if (>= i n)
        out
        (let [[kind value] (nth lexemes i)]
          (if (and (= :token kind) (= "#inst" value))
            (let [after-discards
                  (loop [k (inc i)]
                    (let [[nk nv] (get lexemes k)]
                      (if (and (= :token nk) (= "#_" nv))
                        ;; Discard only the operand; further `#_` or the
                        ;; instant string is handled by this loop / below.
                        (recur (edn-skip-form lexemes (inc k)))
                        k)))
                  [nk nv] (get lexemes after-discards)]
              (if (= :string nk)
                (recur (inc after-discards) (conj out nv))
                (recur (inc i) out)))
            (recur (inc i) out)))))))

(defn decimal-value
  "Exact decimal value of a plain decimal literal as [negative? digits
  exponent] with no leading or trailing zero digits, or nil."
  [lexeme]
  (when-let [[_ sign whole fraction exponent]
             (re-matches #"([+-]?)([0-9]+)(?:\.([0-9]+))?(?:[eE]([+-]?[0-9]+))?"
                         lexeme)]
    (let [fraction (or fraction "")
          digits (str/replace (str whole fraction) #"^0+" "")
          trailing (count (re-find #"0*$" digits))
          exponent (+ (js/parseInt (or exponent "0") 10)
                      (- (count fraction))
                      trailing)
          digits (subs digits 0 (- (count digits) trailing))]
      (if (str/blank? digits)
        [false "" 0]
        [(= "-" sign) digits exponent]))))

(def max-safe-integer-digits (str js/Number.MAX_SAFE_INTEGER))

(defn exact-number-lexeme?
  "True when reading `lexeme` keeps its exact EDN value. JS doubles are the
  only numbers cljs.reader yields, so integers must be safe integers and a
  float must be the double's own shortest decimal, not integral (1.0 would
  read as the integer 1). N/M suffixes, ratios, radix, hex and leading-zero
  forms are rejected, as are the symbolic non-finite ##Inf, ##-Inf and
  ##NaN."
  [lexeme]
  (if-let [[_ digits] (re-matches #"[+-]?(0|[1-9][0-9]*)" lexeme)]
    (or (< (count digits) (count max-safe-integer-digits))
        (and (= (count digits) (count max-safe-integer-digits))
             (<= (compare digits max-safe-integer-digits) 0)))
    (and (re-matches #"[+-]?(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]+)?" lexeme)
         (let [value (js/Number lexeme)]
           (and (js/Number.isFinite value)
                (not (js/Number.isInteger value))
                (= (decimal-value lexeme)
                   (decimal-value (str value))))))))

(defn require-exact-receipt-numbers!
  "Record IDs hash the value cljs.reader produced, so a number that loses
  precision while reading would let two distinct records share one ID."
  [text]
  (doseq [lexeme (edn-number-lexemes text)]
    (when-not (exact-number-lexeme? lexeme)
      (throw (js/Error.
              (str "Receipt River numbers must round-trip exactly; "
                   lexeme " does not"))))))

(defn exact-inst-string?
  "True when `#inst` text keeps its value under cljs.reader, which stores at
  most millisecond precision."
  [instant]
  (boolean
   (re-matches
    #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{1,3})?(Z|[+-]\d{2}:\d{2})"
    instant)))

(defn require-exact-receipt-tags!
  "Record IDs hash the value cljs.reader produced, so a tagged instant with
  sub-millisecond fraction would collide with another truncated to the same
  millisecond."
  [text]
  ;; Lexed, not matched as raw text: EDN treats commas as whitespace, so
  ;; `#inst,"..."` is a valid spelling, while `#inst` text inside a string
  ;; value is not a tag at all.
  (doseq [instant (edn-inst-strings text)]
    (when-not (exact-inst-string? instant)
      (throw (js/Error.
              (str "Receipt River tagged instants must round-trip exactly; "
                   "#inst \"" instant "\" does not"))))))

(defn assert-receipt-batch-sizes!
  "Fails closed when any blob exceeds the writer's per-segment bound. The
  aggregate batch ceiling is applied per chunk in `git-blobs-batch!`, so the
  lifetime ledger size is not capped by one spawnSync call."
  [sizes]
  (doseq [[oid size] sizes]
    (when-not (and (js/Number.isSafeInteger size) (not (neg? size)))
      (throw (js/Error.
              (str "Receipt River segment blob size is not a safe integer: "
                   oid " " size))))
    (when (> size max-receipt-line-bytes)
      (throw (js/Error.
              (str "Receipt River segment blob exceeds "
                   max-receipt-line-bytes " bytes: " oid)))))
  sizes)

(defn git-blob-batch-sizes!
  "Returns object-id -> byte size from one `git cat-file --batch-check`.
  Output is only headers, so the buffer stays small before any blob body is
  read."
  [object-ids]
  (if (empty? object-ids)
    {}
    (let [result (child-process/spawnSync
                  "git"
                  #js ["--no-replace-objects" "cat-file" "--batch-check"]
                  #js {:cwd root
                       :shell false
                       :input (str (str/join "\n" object-ids) "\n")
                       :encoding "utf8"
                       ;; Header lines are ~55–80 bytes; scale with the request
                       ;; so the full ledger is not capped at 1 MiB of metadata.
                       :maxBuffer (+ 4096 (* 160 (count object-ids)))})
          error (.-error result)
          status (.-status result)]
      (when error
        (throw (js/Error. (str "Git invocation failed: " (.-message error)))))
      (when-not (= 0 status)
        (throw (js/Error.
                (str "Git invocation failed with exit " status ": "
                     (str/trim (or (.-stderr result) ""))))))
      (let [lines (vec (remove str/blank? (str/split (or (.-stdout result) "") #"\n")))]
        (when-not (= (count lines) (count object-ids))
          (throw (js/Error.
                  (str "git cat-file --batch-check returned "
                       (count lines) " lines for " (count object-ids)
                       " object ids"))))
        (assert-receipt-batch-sizes!
         (into {}
               (map (fn [oid line]
                      (let [[_ id kind size]
                            (re-matches #"([0-9a-f]+) ([a-z]+) ([0-9]+)" line)
                            size (when size (js/parseInt size 10))]
                        (when-not (and (= oid id) (= "blob" kind)
                                       (js/Number.isSafeInteger size))
                          (throw (js/Error.
                                  (str "Unexpected git cat-file --batch-check "
                                       "entry for " oid ": " line))))
                        [oid size]))
                    object-ids
                    lines)))))))

(defn receipt-blob-chunks
  "Splits object IDs into runs whose total size stays within the batch cap.
  The reduction carries the current chunk's byte total so construction stays
  linear in the number of segments."
  [object-ids sizes]
  (let [[chunks _total]
        (reduce (fn [[chunks total] oid]
                  (let [size (get sizes oid 0)]
                    (if (and (seq (peek chunks))
                             (> (+ total size) max-receipt-batch-bytes))
                      [(conj chunks [oid]) size]
                      [(conj (pop chunks) (conj (peek chunks) oid))
                       (+ total size)])))
                [[[]] 0]
                object-ids)]
    (vec (remove empty? chunks))))

(defn copy-blob [_oid bytes]
  (js/Buffer.from bytes))

(defn git-blobs-batch-chunk!
  "Reads one chunk of object IDs through `git cat-file --batch`. `sizes` must
  already have passed `assert-receipt-batch-sizes!`. Each blob's bytes (a view
  into this chunk's output) are passed to `transform` with its object ID; the
  default copies them."
  ([object-ids sizes]
   (git-blobs-batch-chunk! object-ids sizes copy-blob))
  ([object-ids sizes transform]
   (let [total (reduce + 0 (map #(get sizes %) object-ids))
         max-buffer (+ total (* 256 (inc (count object-ids))) 4096)
         result (child-process/spawnSync
                 "git"
                 #js ["--no-replace-objects" "cat-file" "--batch"]
                 #js {:cwd root
                      :shell false
                      :input (str (str/join "\n" object-ids) "\n")
                      :maxBuffer max-buffer})
         error (.-error result)
         status (.-status result)]
     (when error
       (throw (js/Error. (str "Git invocation failed: " (.-message error)))))
     (when-not (= 0 status)
       (throw (js/Error.
               (str "Git invocation failed with exit " status ": "
                    (str/trim (if-let [stderr (.-stderr result)]
                                (.toString stderr "utf8")
                                ""))))))
     (loop [out (.-stdout result)
            acc {}
            ids (seq object-ids)]
       (if-not ids
         (do (when (pos? (.-length out))
               (throw (js/Error. "git cat-file --batch returned extra output")))
             acc)
         (let [newline-at (.indexOf out 10)
               _ (when (neg? newline-at)
                   (throw (js/Error. "git cat-file --batch output is truncated")))
               header (.toString (.subarray out 0 newline-at) "utf8")
               [_ oid kind size] (re-matches #"([0-9a-f]+) ([a-z]+) ([0-9]+)" header)
               size (when size (js/parseInt size 10))
               expected (get sizes (first ids))
               start (inc newline-at)
               end (when size (+ start size))]
           (when-not (and (= oid (first ids)) (= "blob" kind)
                          (js/Number.isSafeInteger size)
                          (= size expected)
                          (< end (.-length out))
                          (= 10 (aget out end)))
             (throw (js/Error.
                     (str "Unexpected git cat-file --batch entry for "
                          (first ids) ": " header))))
           (recur (.subarray out (inc end))
                  (assoc acc oid (transform oid (.subarray out start end)))
                  (next ids))))))))

(defn git-blobs-batch!
  "Returns object-id -> Buffer for `object-ids`. Sizes are checked with
  `--batch-check` first against the writer's per-segment bound; blobs are
  then read in chunks whose aggregate stays within `max-receipt-batch-bytes`,
  so the lifetime ledger is not capped by one spawnSync buffer. With
  `transform`, each blob is reduced (for example parsed) while its chunk is
  current, so only the transformed values outlive the chunk's buffer."
  ([object-ids]
   (git-blobs-batch! object-ids copy-blob))
  ([object-ids transform]
   (if (empty? object-ids)
     {}
     (let [sizes (git-blob-batch-sizes! object-ids)]
       (reduce (fn [acc chunk]
                 (merge acc (git-blobs-batch-chunk! chunk sizes transform)))
               {}
               (receipt-blob-chunks object-ids sizes))))))


(defn read-receipt-records! [contents]
  (->> (str/split-lines contents)
       (remove str/blank?)
       (map-indexed
        (fn [index line]
          (try
            (require-exact-receipt-numbers! line)
            (require-exact-receipt-tags! line)
            (read-single-edn! line "Receipt River record")
            (catch :default error
              (throw (js/Error.
                      (str "Invalid Receipt River EDN at line " (inc index)
                           ": " (.-message error))))))))
       vec))

(defn git-regular-blob-bytes! [revision repository-path label]
  (let [output (git-capture!
                ["ls-tree" "-z" revision "--" repository-path])
        entries (vec (remove str/blank? (str/split output #"\u0000")))]
    (when-not (= 1 (count entries))
      (throw (js/Error.
              (str label " must be one regular Git blob"))))
    (let [[_ _ path]
          (re-matches
           #"100644 blob ([0-9a-f]{64}|[0-9a-f]{40})\t(.+)"
           (first entries))]
      (when-not (= repository-path path)
        (throw (js/Error.
                (str label " must be a non-executable regular Git blob"))))
      (git-buffer! ["show" (str revision ":" repository-path)]))))

(defn read-immutable-receipt-ledger! [revision]
  (when-not (law/git-commit-id? revision)
    (throw (js/Error. "--at requires a full lowercase Git commit ID")))
  (when-not (= "commit" (str/trim (git-capture! ["cat-file" "-t" revision])))
    (throw (js/Error. "--at must identify a Git commit object")))
  (let [bytes (git-regular-blob-bytes!
               revision law/receipt-ledger-path
               "Immutable Receipt River ledger")
        contents (decode-utf8! bytes "immutable Receipt River ledger")]
    {:ledger/identity {:ledger/path law/receipt-ledger-path
                       :ledger/revision revision
                       :ledger/sha256 (sha256 bytes)}
     :ledger/bytes bytes
     :ledger/records (read-receipt-records! contents)}))

;; ---------------------------------------------------------------------------
;; Segmented Receipt River ledger
;;
;; The ledger is the union of the frozen legacy file `.ημ/receipts.edn` and
;; every `.ημ/receipts/<name>.edn` segment. Segments are written once and never
;; edited, so parallel branches (a fork and its org) add disjoint files instead
;; of editing the same lines. Validity is a property of the merged in-memory
;; ledger, not of any single file's byte prefix.

(def receipt-segment-readme "README.md")

;; <UTC timestamp, basic format with optional fraction>-<lowercase id>.edn,
;; for example 2026-10-13T164500123Z-3f9a1c2b7d4e.edn.
(def receipt-segment-name-pattern
  #"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{6}[0-9]{0,9}Z-[0-9a-z][0-9a-z-]*[.]edn")

(def append-lock-name-pattern #"[.].+[.]append[.]lock")

;; One ledger-wide lock serializes same-checkout semantic writers for the whole
;; validate -> gate -> write section. Parallelism is across branches (a fork and
;; its org add disjoint segments that verify-receipts merges), not concurrent
;; processes in one working tree: a segment that appears while a gate runs can
;; then never be a cooperating writer's receipt, so it stays quarantined.
(def receipt-writer-lock-name ".receipt-river.writer.lock")

;; The segment directory itself can be renamed away and replaced while a gate
;; runs. Every lock in it is then reached only through the held descriptor of
;; the renamed directory, so that quarantine is recorded here instead, in the
;; stable `.ημ` directory that holds the legacy ledger and the segment
;; directory. Its presence closes the working ledger until adjudicated.
(def receipt-quarantine-marker-name ".receipt-river.quarantine")

(defn receipt-segment-timestamp-valid?
  "The name's timestamp must be a real UTC calendar date and clock time, not
  only the right number of digits. The receipt timestamp law decides, so any
  four-digit year (including 0000-0099) is read literally."
  [value]
  (when-let [[_ year month day hour minute second]
             (re-matches #"([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2})([0-9]{2})([0-9]{2}).*"
                         value)]
    (law/canonical-receipt-ts?
     (str year "-" month "-" day "T" hour ":" minute ":" second "Z"))))

(defn receipt-segment-name? [value]
  (boolean (and (string? value)
                (re-matches receipt-segment-name-pattern value)
                (receipt-segment-timestamp-valid? value))))

(defn record-id
  "Stable content identity used to order and deduplicate merged records."
  [record]
  (sha256 (pr-str (law/canonical-form record))))

(defn read-segment-records! [segment-path bytes]
  (when (zero? (.-length bytes))
    (throw (js/Error.
            (str "Receipt River segment must not be empty: " segment-path))))
  (when (> (.-length bytes) max-receipt-line-bytes)
    (throw (js/Error.
            (str "Receipt River segment blob exceeds "
                 max-receipt-line-bytes " bytes: " segment-path))))
  (when-not (= 10 (.at bytes -1))
    (throw (js/Error.
            (str "Receipt River segment must end with a newline: "
                 segment-path))))
  (let [contents (decode-utf8! bytes (str "Receipt River segment "
                                          segment-path))]
    (let [records (try
                    (read-receipt-records! contents)
                    (catch :default error
                      (throw (js/Error. (str (.-message error) " in "
                                             segment-path)))))]
      (when-not (= 1 (count records))
        (throw (js/Error.
                (str "Receipt River segment must contain exactly one record: "
                     segment-path))))
      ;; The segment contract is one physical line: the record, then its
      ;; only newline. A single trailing CR is allowed so a CRLF legacy row
      ;; can migrate byte-identical; embedded CRs and blank lines are not
      ;; dropped silently (encode-edn-line! still rejects CR on new writes).
      (let [body (subs contents 0 (dec (count contents)))
            body* (if (str/ends-with? body "\r")
                    (subs body 0 (dec (count body)))
                    body)]
        (when (or (str/includes? body* "\n") (str/includes? body* "\r"))
          (throw (js/Error.
                  (str "Receipt River segment must hold exactly one physical line: "
                       segment-path)))))
      records)))

(defn segment-entry
  "A parsed segment. Only its content hash is kept, not its bytes, so a large
  ledger is held as records plus hashes; equality of segment files is
  equality of their SHA-256."
  [segment-path bytes]
  {:segment/path segment-path
   :segment/sha256 (sha256 bytes)
   :segment/records (read-segment-records! segment-path bytes)})

(defn segment-sha256
  "Content hash of a segment entry; the legacy entry keeps its bytes."
  [segment]
  (or (:segment/sha256 segment) (sha256 (:segment/bytes segment))))

(defn legacy-segment-entry [bytes records]
  {:segment/path law/receipt-ledger-path
   :segment/legacy? true
   :segment/bytes bytes
   :segment/records records})

(def receipt-operational-file-names
  "Working-tree operational state beside the ledger. Each one closes the
  working ledger to writers, so a reviewed revision must never carry it."
  [receipt-quarantine-marker-name receipt-writer-lock-name])

(defn require-no-committed-operational-files! [revision]
  (let [ledger-directory (.dirname (.-posix path) law/receipt-ledger-path)]
    (doseq [name receipt-operational-file-names]
      (let [repository-path (.join (.-posix path) ledger-directory name)
            entries (remove str/blank?
                            (str/split (git-capture! ["ls-tree" "-z" revision
                                                      "--" repository-path])
                                       #"\u0000"))]
        (when (seq entries)
          (throw (js/Error.
                  (str "Receipt River operational file must not be committed: "
                       repository-path " at " revision))))))))

(defn immutable-receipt-segment-object-ids!
  "Returns repository-path -> blob ID for every segment at `revision`, with
  `:segment/directory?` metadata. The reviewed tree must not carry
  working-tree operational files."
  [revision]
  (require-no-committed-operational-files! revision)
  (let [directory (str law/receipt-segment-directory "/")
        own (vec (remove str/blank?
                         (str/split (git-capture! ["ls-tree" "-z" revision "--"
                                                   law/receipt-segment-directory])
                                    #"\u0000")))
        _ (when (and (seq own)
                     (not (and (= 1 (count own))
                               (str/starts-with? (first own) "040000 tree "))))
            (throw (js/Error.
                    (str law/receipt-segment-directory
                         " must be a Git tree (directory) at " revision))))
        output (git-capture! ["ls-tree" "-z" revision "--" directory])
        entries (remove str/blank? (str/split output #"\u0000"))
        object-ids
        (reduce
         (fn [segments entry]
           (let [[_ mode kind object-id repository-path]
                 (re-matches
                  #"([0-7]{6}) ([a-z]+) ([0-9a-f]{64}|[0-9a-f]{40})\t(.+)"
                  entry)
                 parent (when repository-path (.dirname (.-posix path) repository-path))
                 basename (when repository-path
                            (.basename (.-posix path) repository-path))]
             (when-not (= law/receipt-segment-directory parent)
               (throw (js/Error.
                       (str "Unexpected Receipt River segment entry: " entry))))
             (when-not (and (= "100644" mode) (= "blob" kind))
               (throw (js/Error.
                       (str "Receipt River segment must be a non-executable regular Git blob: "
                            repository-path))))
             (cond
               (= receipt-segment-readme basename) segments
               (receipt-segment-name? basename)
               (assoc segments repository-path object-id)
               :else
               (throw (js/Error.
                       (str "Receipt River segment name must match "
                            "<UTC timestamp>-<id>.edn: " repository-path))))))
         (sorted-map)
         entries)]
    (with-meta object-ids {:segment/directory? (boolean (seq own))})))

(defn read-immutable-receipt-segments!
  "Returns repository-path -> bytes for every segment blob at `revision`.
  Blob IDs come from ls-tree and segment bytes are read by `git cat-file
  --batch` in bounded chunks."
  [revision]
  (let [object-ids (immutable-receipt-segment-object-ids! revision)
        blobs (git-blobs-batch! (vec (distinct (vals object-ids))))]
    (with-meta
      (into (sorted-map)
            (map (fn [[repository-path object-id]]
                   [repository-path (get blobs object-id)]))
            object-ids)
      (meta object-ids))))

(defn read-immutable-receipt-segment-entries!
  "Parsed segment entries (path, hash, records) at `revision`. Each blob is
  parsed while its batch chunk is current and its bytes are then dropped, so
  verifier memory holds records and hashes rather than every segment's
  bytes."
  [revision]
  (let [object-ids (immutable-receipt-segment-object-ids! revision)
        path-of (reduce (fn [acc [repository-path object-id]]
                          (if (contains? acc object-id)
                            acc
                            (assoc acc object-id repository-path)))
                        {}
                        object-ids)
        parsed (git-blobs-batch!
                (vec (distinct (vals object-ids)))
                (fn [object-id bytes]
                  (dissoc (segment-entry (get path-of object-id) bytes)
                          :segment/path)))]
    (with-meta
      (mapv (fn [[repository-path object-id]]
              (assoc (get parsed object-id) :segment/path repository-path))
            object-ids)
      (meta object-ids))))

(defn merged-receipt-records!
  "Merges every segment into one ledger ordered by timestamp, then record ID.
  A record value may appear in only one place."
  [segments]
  (when-let [repeated (seq (law/repeated-ledger-records record-id segments))]
    (throw (js/Error.
            (str "Receipt River ledger repeats a record: "
                 (pr-str (vec repeated))))))
  (law/merge-ledger-records record-id segments))

(defn merged-ledger-sha256
  "Identity of the whole segmented ledger: every segment path and content hash."
  [segments]
  (sha256 (apply str (map (fn [segment]
                            (str (:segment/path segment) "\t"
                                 (segment-sha256 segment) "\n"))
                          segments))))

(defn segmented-ledger [legacy-ledger segments]
  {:ledger/identity (assoc (:ledger/identity legacy-ledger)
                           :ledger/sha256 (merged-ledger-sha256 segments)
                           :ledger/legacy-sha256
                           (get-in legacy-ledger [:ledger/identity :ledger/sha256])
                           :ledger/segments (count segments))
   :ledger/segments segments
   :ledger/records (merged-receipt-records! segments)})

(defn read-immutable-merged-ledger! [revision]
  (let [legacy (read-immutable-receipt-ledger! revision)
        stored (read-immutable-receipt-segment-entries! revision)
        segments (into [(legacy-segment-entry (:ledger/bytes legacy)
                                              (:ledger/records legacy))]
                       stored)]
    (assoc (segmented-ledger legacy segments)
           :ledger/segment-directory?
           (boolean (:segment/directory? (meta stored))))))

(defn require-segment-directory-kept! [base-ledger ledger]
  (when (and (:ledger/segment-directory? base-ledger)
             (not (:ledger/segment-directory? ledger)))
    (throw (js/Error.
            (str law/receipt-segment-directory
                 " was removed; the segment directory must remain")))))

(defn line-slices
  "Split `bytes` into complete line slices. Every newline-terminated line keeps
  its trailing `\\n` byte; only a final unterminated remnant (if any) has none.
  Comparing these slices rejects deleting a terminator from a retained line."
  [bytes]
  (if (zero? (.-length bytes))
    []
    (let [text (.toString bytes "utf8")
          raw (str/split text #"\n" -1)
          terminated? (str/ends-with? text "\n")
          ;; A trailing empty field exists only because of a final newline; drop
          ;; it so that terminator lives on the preceding slice instead.
          parts (if terminated? (pop (vec raw)) (vec raw))]
      (mapv (fn [i part]
              (if (or terminated? (< i (dec (count parts))))
                (str part "\n")
                part))
            (range (count parts))
            parts))))

(defn line-subsequence?
  "True when every complete line slice of `head` appears, byte-identical and in
  order, in `base`. Slices include their terminators, so stripping the newline
  that ends a retained line is not a whole-line loss."
  [head base]
  (if (.equals head base)
    true
    (loop [wanted (seq (line-slices head))
           available (seq (line-slices base))]
      (cond
        (empty? wanted) true
        (empty? available) false
        (= (first wanted) (first available)) (recur (rest wanted) (rest available))
        :else (recur wanted (rest available))))))

(defn removed-line-slices
  "Base line slices that a terminator-preserving subsequence match against
  `head` does not consume. Each removed slice must reappear as an entire
  non-legacy segment body so emptying or rewriting legacy cannot invent
  alternate serializations. A final unterminated slice reappears with one
  added newline, the only form a segment can hold."
  [head base]
  (loop [wanted (seq (line-slices head))
         available (seq (line-slices base))
         removed []]
    (cond
      (empty? available) removed
      (and (seq wanted) (= (first wanted) (first available)))
      (recur (rest wanted) (rest available) removed)
      :else (recur wanted (rest available) (conj removed (first available))))))

(defn ledger-extension-records!
  "Checks that `head-segments` extend `base-segments` as an event ledger and
  returns the new records in merged order. Rules:
  - every base segment file still exists byte-for-byte (no edits, no deletes);
  - every base record is still present somewhere in the head ledger;
  - the frozen legacy file is unchanged except that whole lines may be
    removed (its lines stay an ordered, byte-identical subsequence of the
    base file), and it gains no record that the base ledger lacked;
  - no record appears twice in the merged head ledger.
  Records may move out of the legacy file into segments, which is how a
  diverged fork and org reconcile their histories."
  [base-segments head-segments]
  (let [head-by-path (into {} (map (juxt :segment/path identity)) head-segments)
        merged-head (merged-receipt-records! head-segments)]
    (merged-receipt-records! base-segments)
    (doseq [{:segment/keys [path legacy?] :as base} base-segments
            :when (not legacy?)]
      (let [head (get head-by-path path)]
        (when-not head
          (throw (js/Error.
                  (str "Receipt River segment was removed: " path))))
        (when-not (= (segment-sha256 base) (segment-sha256 head))
          (throw (js/Error.
                  (str "Receipt River segment was edited: " path))))))
    (let [base-ids (into #{} (comp (mapcat :segment/records) (map record-id))
                         base-segments)
          head-ids (into #{} (map record-id) merged-head)
          missing (remove head-ids base-ids)
          legacy-additions (->> head-segments
                                (filter :segment/legacy?)
                                (mapcat :segment/records)
                                (remove #(contains? base-ids (record-id %))))]
      (when (seq missing)
        (throw (js/Error.
                (str "Receipt River head drops " (count missing)
                     " base record(s)"))))
      (when (seq legacy-additions)
        (throw (js/Error.
                (str "The legacy Receipt River ledger " law/receipt-ledger-path
                     " is frozen; write new receipts as segments under "
                     law/receipt-segment-directory "/"))))
      (doseq [{:segment/keys [bytes legacy?]} base-segments
              :when legacy?
              :let [head (or (first (filter :segment/legacy? head-segments))
                             {:segment/bytes (js/Buffer.alloc 0)})
                    head-bytes (:segment/bytes head)]]
        (when-not (line-subsequence? head-bytes bytes)
          (throw (js/Error.
                  (str "The legacy Receipt River ledger " law/receipt-ledger-path
                       " may only lose whole lines; it was rewritten or reordered"))))
        (let [removed (removed-line-slices head-bytes bytes)
              ;; Segments keep their content hash, not their bytes.
              segment-hashes (into #{}
                                   (comp (remove :segment/legacy?)
                                         (map segment-sha256))
                                   head-segments)]
          (doseq [line removed]
            ;; Only the final row of an unterminated legacy file lacks a
            ;; newline. Its sole preservation form is that row plus the one
            ;; terminator every segment needs.
            (let [line-bytes (js/Buffer.from (if (str/ends-with? line "\n")
                                               line
                                               (str line "\n"))
                                             "utf8")]
              (when-not (contains? segment-hashes (sha256 line-bytes))
                (throw (js/Error.
                        (str "The legacy Receipt River ledger "
                             law/receipt-ledger-path
                             " may only lose a line when that exact line is preserved as a segment"))))))))
      (filterv #(not (contains? base-ids (record-id %))) merged-head))))

(defn read-immutable-catalog-bundle! [revision]
  (when-not (law/git-commit-id? revision)
    (throw (js/Error. "Reviewed root revision must be a full Git commit ID")))
  (when-not (= "commit" (str/trim (git-capture! ["cat-file" "-t" revision])))
    (throw (js/Error. "Reviewed root revision must identify a Git commit object")))
  (let [bytes (git-regular-blob-bytes!
               revision catalog-relative-path
               "Immutable quality gate catalog")
        contents (decode-utf8! bytes "immutable quality gate catalog")]
    {:catalog (read-single-edn! contents "immutable quality gate catalog EDN")
     :catalog-identity {:catalog/path catalog-relative-path
                        :catalog/sha256 (sha256 bytes)}}))

(defn current-root-state! []
  {:root/revision (str/trim (git-capture! ["rev-parse" "HEAD"]))
   :root/status (git-capture!
                 ["status" "--porcelain=v1" "--untracked-files=no"
                  "--ignore-submodules=none"])})

(defn require-current-clean-root! [expected-revision state]
  (when-not (= expected-revision (:root/revision state))
    (throw (js/Error. "Reviewed root revision is not the current HEAD")))
  (when-not (str/blank? (:root/status state))
    (throw (js/Error. "Reviewed root checkout has tracked or submodule changes")))
  state)

(defn gitlink-target! [root-revision repository-path]
  (let [output (git-capture!
                ["ls-tree" "-z" root-revision "--" repository-path])
        entries (vec (remove str/blank? (str/split output #"\u0000")))]
    (when-not (= 1 (count entries))
      (throw (js/Error.
              (str "Reviewed root must contain one gitlink for "
                   repository-path))))
    (let [[_ target path]
          (re-matches
           #"160000 commit ([0-9a-f]{64}|[0-9a-f]{40})\t(.+)"
           (first entries))]
      (when-not (and target (= repository-path path))
        (throw (js/Error.
                (str "Reviewed root entry is not the expected gitlink for "
                     repository-path))))
      target)))

(defn require-result-gitlinks!
  [catalog required-gate-ids results reviewed-root-revision]
  (let [trusted-gates (law/gate-index catalog)
        by-id (into {} (map (juxt :gate/id identity)) results)]
    (doseq [gate-id required-gate-ids]
      (let [repository-path (get-in trusted-gates [gate-id :repository/path])
            target-revision (:result/revision (get by-id gate-id))
            gitlink-revision (gitlink-target!
                              reviewed-root-revision repository-path)]
        (when-not (= target-revision gitlink-revision)
          (throw (js/Error.
                  (str "Gate result revision does not match reviewed gitlink: "
                       gate-id))))))
    true))

(defn actionable-submodule-paths []
  (into #{}
        (comp (filter :source/actionable?)
              (map :source/path))
        (project-model/submodule-sources)))

(defn validate-catalog!
  ([catalog]
   (validate-catalog! catalog (actionable-submodule-paths)))
  ([catalog actionable-paths]
   (when-let [errors (seq (into (law/catalog-errors catalog)
                                (law/catalog-inventory-errors
                                 catalog
                                 actionable-paths)))]
    (throw (js/Error. (str "Invalid quality gate catalog: " (pr-str errors)))))
   catalog))

(defn require-valid-receipt-records! [records]
  (let [invalid-envelopes (remove law/receipt-envelope? records)
        evidence-records (filter #(= law/evidence-receipt-origin (:origin %))
                                 records)
        invalid-evidence (remove law/evidence-receipt? evidence-records)]
    (when (seq invalid-envelopes)
      (throw (js/Error.
              (str "Immutable ledger contains invalid receipt envelopes: "
                   (pr-str (vec invalid-envelopes))))))
    (when (seq invalid-evidence)
      (throw (js/Error.
              (str "Immutable ledger contains invalid evidence receipts: "
                   (pr-str (mapv #(law/result-errors (:evidence/result %))
                                 invalid-evidence))))))
    {:receipt/total (count records)
     :receipt/evidence (count evidence-records)
     :receipt/legacy-evidence
     (count (filter #(nil? (:evidence/schema %)) evidence-records))}))

(declare appended-receipt-records!)

(defn promotion-ready-at!
  [target-revision required-gate-ids results trusted-base-revision
   reviewed-root-revision]
  (when-not (law/git-commit-id? trusted-base-revision)
    (throw (js/Error. "Trusted base revision must be a full Git commit ID")))
  (when-not (law/git-commit-id? reviewed-root-revision)
    (throw (js/Error. "Reviewed root revision must be a full Git commit ID")))
  (let [before (require-current-clean-root!
                reviewed-root-revision
                (current-root-state!))
        {:keys [catalog catalog-identity]}
        (read-immutable-catalog-bundle! reviewed-root-revision)
        catalog (validate-catalog! catalog)
        base-ledger (read-immutable-merged-ledger! trusted-base-revision)
        ledger (read-immutable-merged-ledger! reviewed-root-revision)]
    (require-segment-directory-kept! base-ledger ledger)
    (require-valid-receipt-records!
     (ledger-extension-records! (:ledger/segments base-ledger)
                                (:ledger/segments ledger)))
    (if-not (law/promotion-evidence-consistent?
             catalog catalog-identity target-revision
             required-gate-ids results ledger)
      false
      (do
        (require-result-gitlinks!
         catalog required-gate-ids results reviewed-root-revision)
        (let [after (require-current-clean-root!
                     reviewed-root-revision
                     (current-root-state!))]
          (when-not (= before after)
            (throw (js/Error.
                    "Reviewed root state changed during promotion review")))
          true)))))

(defn parse-csv [value flag]
  (let [values (into #{} (remove str/blank?)
                     (map str/trim (str/split (or value "") #",")))]
    (when (empty? values)
      (throw (js/Error. (str flag " requires a comma-separated value"))))
    values))

(defn parse-args [args]
  (loop [remaining (vec args)
         options {:only nil :kinds law/gate-kinds :at nil :base nil}]
    (if-let [arg (first remaining)]
      (case arg
        "--only"
        (if-let [value (second remaining)]
          (recur (subvec remaining 2)
                 (assoc options :only (parse-csv value "--only")))
          (throw (js/Error. "--only requires a comma-separated value")))

        "--kind"
        (if-let [value (second remaining)]
          (let [kinds (into #{} (map keyword) (parse-csv value "--kind"))
                unknown (seq (remove law/gate-kinds kinds))]
            (when unknown
              (throw (js/Error. (str "Unknown gate kinds: "
                                     (str/join ", " (map name unknown))))))
            (recur (subvec remaining 2) (assoc options :kinds kinds)))
          (throw (js/Error. "--kind requires a comma-separated value")))

        "--at"
        (if-let [value (second remaining)]
          (recur (subvec remaining 2) (assoc options :at value))
          (throw (js/Error. "--at requires a full Git commit ID")))

        "--base"
        (if-let [value (second remaining)]
          (recur (subvec remaining 2) (assoc options :base value))
          (throw (js/Error. "--base requires a full Git commit ID")))

        (throw (js/Error. (str "Unknown argument: " arg))))
      options)))

(defn require-mapped-repositories! [catalog repository-paths]
  (let [known (set (keys (:catalog/repositories catalog)))
        unknown (seq (remove known repository-paths))]
    (when unknown
      (throw (js/Error. (str "Repositories have no mapped gates: "
                             (str/join ", " unknown)))))
    repository-paths))

(defn require-repositories! [catalog only]
  (when-not (seq only)
    (throw (js/Error. "Choose repositories with --only <paths>")))
  (require-mapped-repositories! catalog only)
  (let [inventory (workspace/inventory)
        selected (workspace/select-repos inventory {:only only :all? false})
        available (filterv #(and (:exists %)
                                 (:initialized %)
                                 (false? (:dirty %))
                                 (empty? (:git-errors %)))
                           selected)
        execution-paths (into {}
                              (map (fn [{:keys [repo absolute]}]
                                     [(:path repo) absolute]))
                              (workspace/execution-paths! available))]
    (into {}
          (map (fn [{:keys [path exists initialized dirty git-errors head]
                     :as repository}]
                 [path (if-let [absolute (get execution-paths path)]
                         {:path path
                          :absolute absolute
                          :revision head
                          :inventory repository}
                         {:path path
                          :unavailable-reason
                          (cond
                            (not exists) "Repository checkout is missing"
                            (not initialized) "Repository checkout is not initialized"
                            (seq git-errors) "Repository Git state could not be verified"
                            (true? dirty) "Repository checkout is dirty"
                            (nil? dirty) "Repository checkout cleanliness is unavailable"
                            :else "Repository checkout is unavailable")
                          :revision head})]))
          selected)))

(defn process-group-alive? [pid]
  (try
    (js/process.kill (- pid) 0)
    true
    (catch :default error
      (not= "ESRCH" (.-code error)))))

(defn contain-gate-descendants!
  "Kills the gate's whole process group and waits until it is empty. A gate
  may leave detached descendants behind; killing them before the receipt is
  written and the locks are released keeps them from mutating the working
  ledger after the final revalidation. Returns true once the group is gone."
  [pid]
  ;; Only a real child PID: signalling group 0 would hit this process's own.
  (or (not (and (number? pid) (pos? pid)))
      (do (try
            (js/process.kill (- pid) "SIGKILL")
            (catch :default _error nil))
          (let [deadline (+ (js/Date.now) 5000)]
            (loop []
              (cond
                (not (process-group-alive? pid)) true
                (> (js/Date.now) deadline) false
                :else (do (js/Atomics.wait
                           (js/Int32Array. (js/SharedArrayBuffer. 4)) 0 0 50)
                          (recur))))))))

(defn spawn-result [cwd command]
  ;; `detached` makes the gate the leader of its own process group (and
  ;; session), so every descendant that stays in it can be killed once the
  ;; gate exits.
  (let [result (child-process/spawnSync
                (first command)
                (clj->js (rest command))
                #js {:cwd cwd :encoding "utf8" :stdio "inherit" :shell false
                     :detached true})]
    (if (contain-gate-descendants! (.-pid result))
      result
      ;; Still-running descendants could mutate the ledger after release, so
      ;; the reservation keeps its lock as a quarantine marker.
      (throw (ex-info
              (str "Gate descendants of " (first command)
                   " survived SIGKILL; the gate result cannot be retained")
              {:receipt-river/quarantine true})))))

(defn local-result [cwd gate]
  (let [result (spawn-result cwd (:gate/command gate))
        error (.-error result)
        status (.-status result)]
    (cond
      error {:gate/id (:gate/id gate)
             :result/outcome :unavailable
             :result/reason (.-message error)}
      (nil? status) {:gate/id (:gate/id gate)
                     :result/outcome :failed
                     :result/reason (str "Process ended without an exit status"
                                         (when-let [signal (.-signal result)]
                                           (str " (" signal ")")))}
      (zero? status) {:gate/id (:gate/id gate)
                      :result/outcome :passed
                      :result/exit status}
      :else {:gate/id (:gate/id gate)
             :result/outcome :failed
             :result/exit status})))

(defn execution-path-unavailable-reason
  [{:keys [absolute inventory]}]
  (if-not (map? inventory)
    "Repository inventory identity is unavailable"
    (try
      (let [{current-absolute :absolute}
            (first (workspace/execution-paths! [inventory]))]
        (when-not (= absolute current-absolute)
          "Repository execution path changed after inventory"))
      (catch :default error
        (str "Repository execution path could not be reverified: "
             (.-message error))))))

(defn local-unavailable-reason
  ([{:keys [absolute] :as repository}]
   (or (execution-path-unavailable-reason repository)
       (local-unavailable-reason repository (workspace/git-state absolute))))
  ([{:keys [revision]} {:keys [initialized head dirty git-errors]}]
    (cond
      (not initialized) "Repository checkout became uninitialized"
      (seq git-errors) "Repository Git state could not be reverified"
      (true? dirty) "Repository checkout became dirty"
      (nil? dirty) "Repository checkout cleanliness could not be reverified"
      (not= revision head) "Repository revision changed after inventory"
      :else nil)))

(defn result-provenance
  [{:keys [path revision]} gate catalog-identity]
  (cond-> {:result/execution (:gate/execution gate)
           :result/catalog catalog-identity
           :result/source {:source/path (:gate/source gate)
                           :source/repository path
                           :source/revision revision}}
    (seq (:gate/command gate))
    (assoc :result/command (vec (:gate/command gate)))))

(defn run-local-gate!
  [{:keys [absolute unavailable-reason revision] :as repository} gate]
  (if-not absolute
    {:gate/id (:gate/id gate)
     :result/outcome :unavailable
     :result/reason unavailable-reason}
    (if-let [reason (local-unavailable-reason repository)]
      {:gate/id (:gate/id gate)
       :result/outcome :unavailable
       :result/reason reason}
      (let [attempt (local-result absolute gate)
            path-reason (execution-path-unavailable-reason repository)
            post-state (when-not path-reason (workspace/git-state absolute))
            reason (or path-reason
                       (local-unavailable-reason repository post-state))]
        (if reason
          (cond-> {:gate/id (:gate/id gate)
                   :result/outcome :unavailable
                   :result/reason (str "Evidence rejected after gate execution: " reason)
                   :result/attempt-outcome (:result/outcome attempt)}
            (contains? attempt :result/exit)
            (assoc :result/attempt-exit (:result/exit attempt))

            (law/nonblank-string? (:head post-state))
            (assoc :result/observed-revision (:head post-state)))
          (cond-> attempt
            (law/nonblank-string? revision)
            (assoc :result/revision revision)))))))

(def coverage-artifact-unavailable-reason
  "Coverage report attestation is not implemented; see open-hax/foresight#59")

(defn require-attestable-result [gate result]
  (if (and (= :coverage (:gate/kind gate))
           (= :passed (:result/outcome result)))
    (cond-> (-> result
                (assoc :result/outcome :unavailable
                       :result/reason coverage-artifact-unavailable-reason
                       :result/attempt-outcome :passed)
                (dissoc :result/exit))
      (contains? result :result/exit)
      (assoc :result/attempt-exit (:result/exit result)))
    result))

(defn run-gate! [{:keys [revision] :as repository} gate catalog-identity]
  (let [provenance (result-provenance repository gate catalog-identity)]
    (println "START"
             (pr-str (merge {:gate/id (:gate/id gate)
                             :gate/kind (:gate/kind gate)}
                            provenance)))
    (let [outcome-result
          (case (:gate/execution gate)
            :local (run-local-gate! repository gate)
            :workflow-only (cond-> {:gate/id (:gate/id gate)
                                    :result/outcome :unavailable
                                    :result/reason (:gate/reason gate)}
                             (law/nonblank-string? revision)
                             (assoc :result/revision revision))
            :external (cond-> {:gate/id (:gate/id gate)
                               :result/outcome :blocked
                               :result/reason (:gate/reason gate)}
                        (law/nonblank-string? revision)
                        (assoc :result/revision revision)))
          result (require-attestable-result
                  gate
                  (merge outcome-result provenance))]
      (println (str/upper-case (name (:result/outcome result)))
               (str (:gate/id gate))
               (or (:result/reason result) ""))
      (println "RESULT" (pr-str result))
      result)))

(defn runtime-adapter []
  (str "nbb/node@" (.-version js/process)))

(defn evidence-receipt [result timestamp hostname adapter]
  (let [source (:result/source result)]
    {:ts timestamp
     :kind :test-run
     :repo "."
     :origin law/evidence-receipt-origin
     :evidence/schema 2
     :evidence/adapter adapter
     :owner "foresight-evidence-runner"
     :dod "Retain one exact gate result for immutable promotion review"
     :pi "eta-mu"
     :host hostname
     :manifest (->> [(:catalog/path (:result/catalog result))
                     (:source/path source)]
                    (filter law/nonblank-string?)
                    distinct
                    vec)
     :refs [(str (:source/repository source)
                 "@"
                 (or (:result/revision result) "unbound"))
            (str (:gate/id result))]
     :evidence/result result}))

(defn append-error! [message]
  (throw (js/Error. (str "Secure Receipt River append rejected: " message))))

(defn fs-constant! [name]
  (let [value (aget (.-constants fs) name)]
    (when-not (number? value)
      (append-error! (str "this Node runtime does not expose fs.constants." name)))
    value))

(defn require-secure-append-support! []
  (when-not (= "linux" (.-platform js/process))
    (append-error! "descriptor-bound append currently requires Linux"))
  (doseq [constant ["O_RDONLY" "O_WRONLY" "O_RDWR" "O_APPEND" "O_CREAT"
                    "O_EXCL" "O_NOFOLLOW" "O_DIRECTORY" "O_NONBLOCK"]]
    (fs-constant! constant))
  (let [proc-fd "/proc/self/fd"]
    (when-not (and (fs/existsSync proc-fd)
                   (.isDirectory (fs/statSync proc-fd)))
      (append-error! "/proc/self/fd is unavailable")))
  true)

(defn file-identity [stats]
  [(str (.-dev stats)) (str (.-ino stats))])

(defn same-file? [left right]
  (= (file-identity left) (file-identity right)))

(defn lstat-if-present! [file description]
  (try
    (fs/lstatSync file bigint-stat-options)
    (catch :default error
      (if (= "ENOENT" (.-code error))
        nil
        (append-error!
         (str description " could not be inspected: " (.-message error)))))))

(defn require-directory-stats! [stats description]
  (when (.isSymbolicLink stats)
    (append-error! (str description " must not be a symbolic link")))
  (when-not (.isDirectory stats)
    (append-error! (str description " must be a directory")))
  stats)

(defn require-regular-stats! [stats description]
  (when (.isSymbolicLink stats)
    (append-error! (str description " must not be a symbolic link")))
  (when-not (.isFile stats)
    (append-error! (str description " must be a regular file")))
  (when-not (= "1" (str (.-nlink stats)))
    (append-error! (str description " must have exactly one hard link")))
  stats)

(defn require-non-executable-stats!
  "Git records any executable bit as a 100755 blob, which the immutable
  Receipt River reader rejects, so a working ledger file must have none."
  [stats description]
  (when-not (zero? (bit-and (js/Number (.-mode stats)) 73))
    (append-error! (str description " must not be executable")))
  stats)

(defn require-path-identity! [file expected-stats description stats-check!]
  (let [path-stats (lstat-if-present! file description)]
    (when-not path-stats
      (append-error! (str description " disappeared")))
    (stats-check! path-stats description)
    (when-not (same-file? path-stats expected-stats)
      (append-error! (str description " identity changed")))
    path-stats))

(defn close-after-open-error! [fd error]
  (try
    (fs/closeSync fd)
    (catch :default _close-error nil))
  (throw error))

(defn open-parent-directory!
  ([directory]
   (open-parent-directory! directory "Receipt River parent directory"))
  ([directory description]
   (let [initial-stats (lstat-if-present! directory description)]
     (when-not initial-stats
       (append-error! (str description " does not exist")))
     (require-directory-stats! initial-stats description)
     (let [fd (fs/openSync
               directory
               (bit-or (fs-constant! "O_RDONLY")
                       (fs-constant! "O_DIRECTORY")
                       (fs-constant! "O_NOFOLLOW")))]
       (try
         (let [fd-stats (fs/fstatSync fd bigint-stat-options)
               proc-path (str "/proc/self/fd/" fd)]
           (require-directory-stats! fd-stats (str description " descriptor"))
           (require-path-identity! directory fd-stats description
                                   require-directory-stats!)
           (let [proc-stats (fs/statSync proc-path bigint-stat-options)]
             (when-not (and (.isDirectory proc-stats)
                            (same-file? proc-stats fd-stats))
               (append-error! "/proc/self/fd did not resolve to the held parent descriptor")))
           {:directory directory
            :description description
            :fd fd
            :stats fd-stats
            :proc-path proc-path})
         (catch :default error
           (close-after-open-error! fd error)))))))

(defn require-parent-identity! [{:keys [directory stats description]}]
  (require-path-identity! directory stats
                          (or description "Receipt River parent directory")
                          require-directory-stats!))

(defn parent-identity-intact? [parent]
  (try
    (require-parent-identity! parent)
    true
    (catch :default _error false)))

(defn append-lock-path [{:keys [proc-path]} basename]
  (path/join proc-path (str "." basename ".append.lock")))

(defn write-all! [fd bytes]
  (loop [offset 0]
    (when (< offset (.-byteLength bytes))
      (let [written (fs/writeSync fd bytes offset
                                  (- (.-byteLength bytes) offset)
                                  nil)]
        (when-not (and (integer? written) (pos? written))
          (append-error! "descriptor write made no progress"))
        (recur (+ offset written))))))

(defn remove-created-lock!
  "Removes a lock this process just created with O_EXCL when acquisition
  aborts before any gate ran. Only a path that still names the descriptor's
  inode is unlinked, and the directory entry removal is synced. Anything
  uncertain leaves the path in place for adjudication."
  [parent lock-path fd]
  (try
    (let [fd-stats (fs/fstatSync fd bigint-stat-options)
          path-stats (lstat-if-present! lock-path "Receipt River append lock")]
      (when (and path-stats (same-file? fd-stats path-stats))
        (fs/unlinkSync lock-path)
        (fs/fsyncSync (:fd parent))))
    (catch :default _cleanup-error nil)))

(defn acquire-lock-file! [parent lock-path busy-message]
  (require-parent-identity! parent)
  (let [flags (bit-or (fs-constant! "O_WRONLY")
                      (fs-constant! "O_CREAT")
                      (fs-constant! "O_EXCL")
                      (fs-constant! "O_NOFOLLOW"))
        fd (try
             (fs/openSync lock-path flags 384)
             (catch :default error
               (if (= "EEXIST" (.-code error))
                 (append-error! busy-message)
                 (append-error! (str "append lock could not be created: "
                                     (.-message error))))))]
    (try
      (let [stats (fs/fstatSync fd bigint-stat-options)
            marker (.encode (js/TextEncoder.)
                            (str "pid=" (.-pid js/process) "\n"))]
        (require-regular-stats! stats "Receipt River append lock")
        (require-path-identity! lock-path stats "Receipt River append lock"
                                require-regular-stats!)
        (write-all! fd marker)
        (fs/fsyncSync fd)
        (fs/fsyncSync (:fd parent))
        (require-parent-identity! parent)
        {:fd fd :path lock-path :stats stats})
      (catch :default error
        ;; No gate or receipt write has started: a lock left here would block
        ;; every later writer although nobody holds it.
        (remove-created-lock! parent lock-path fd)
        (close-after-open-error! fd error)))))

(defn acquire-append-lock! [parent basename]
  (acquire-lock-file!
   parent (append-lock-path parent basename)
   "another writer holds the append lock, or a stale/unsafe lock exists"))

(defn acquire-receipt-writer-lock! [parent]
  (acquire-lock-file!
   parent (path/join (:proc-path parent) receipt-writer-lock-name)
   (str "another writer holds an active Receipt River reservation; retry after "
        "it finishes (a writer lock left by a crashed process must be "
        "adjudicated and removed by hand)")))

(defn require-append-lock-identity! [lock]
  (let [fd-stats (fs/fstatSync (:fd lock) bigint-stat-options)]
    (require-regular-stats! fd-stats "Receipt River append lock descriptor")
    (when-not (same-file? fd-stats (:stats lock))
      (append-error! "Receipt River append lock descriptor identity changed"))
    (require-path-identity! (:path lock) fd-stats
                            "Receipt River append lock"
                            require-regular-stats!)
    fd-stats))

(defn release-append-lock! [parent lock]
  (try
    (require-append-lock-identity! lock)
    (fs/unlinkSync (:path lock))
    (fs/fsyncSync (:fd parent))
    (finally
      (fs/closeSync (:fd lock)))))

(defn create-quarantine-marker!
  "Creates a quarantine marker exclusively at `marker-path`. Any entry already
  occupying the path still refuses later writers, so it is left in place for
  adjudication. Throws only when no marker could be established."
  [parent marker-path]
  (let [flags (bit-or (fs-constant! "O_WRONLY")
                      (fs-constant! "O_CREAT")
                      (fs-constant! "O_EXCL")
                      (fs-constant! "O_NOFOLLOW"))
        fd (try
             (fs/openSync marker-path flags 384)
             (catch :default error
               (when-not (= "EEXIST" (.-code error))
                 (append-error!
                  (str "a Receipt River quarantine marker could not be "
                       "established at " (path/basename marker-path)
                       "; the ledger must be adjudicated by hand: "
                       (.-message error))))))]
    (when fd
      (try
        (write-all! fd (.encode (js/TextEncoder.)
                                (str "quarantine pid=" (.-pid js/process)
                                     "\n")))
        (fs/fsyncSync fd)
        (finally
          (fs/closeSync fd))))
    (when parent
      (fs/fsyncSync (:fd parent)))
    marker-path))

(defn retain-quarantine-lock!
  "Keeps `lock` as a durable quarantine marker. Closing a held descriptor
  preserves nothing once its path was unlinked or replaced, so the lock's
  path identity is verified first; failing that, a fresh marker is created
  exclusively at the same path. Any entry already occupying that path still
  refuses later writers, so it is left for adjudication. Throws only when no
  marker could be established."
  [parent lock]
  (let [verified? (try
                    (require-append-lock-identity! lock)
                    true
                    (catch :default _error false))]
    (try
      (fs/closeSync (:fd lock))
      (catch :default _close-error nil))
    (when-not verified?
      (create-quarantine-marker! parent (:path lock)))
    lock))

(defn establish-ledger-quarantine!
  "Records a quarantine in the stable `.ημ` directory `ancestor` after the
  segment directory this reservation holds was renamed or replaced. When the
  held `.ημ` descriptor no longer names the canonical path either, a marker
  is also attempted at the canonical path and the failure is reported, since
  the held one may be unreachable."
  [ancestor]
  (let [intact? (parent-identity-intact? ancestor)]
    (create-quarantine-marker!
     ancestor (path/join (:proc-path ancestor) receipt-quarantine-marker-name))
    (when-not intact?
      ;; The held descriptor no longer names the canonical `.ημ`, so its
      ;; directory fsync does not make the canonical marker's entry durable.
      ;; Open the canonical replacement and fsync it after creating the
      ;; marker; a crash then cannot keep a synced forged ledger but lose
      ;; the quarantine.
      (try
        (if-let [canonical (try
                             (open-parent-directory!
                              (:directory ancestor)
                              "Receipt River canonical ledger directory")
                             (catch :default _open-error nil))]
          (try
            (create-quarantine-marker!
             canonical (path/join (:proc-path canonical)
                                  receipt-quarantine-marker-name))
            (finally
              (try
                (fs/closeSync (:fd canonical))
                (catch :default _close-error nil))))
          ;; The canonical path is not a plain directory that can be held;
          ;; still try to leave a marker there for adjudication.
          (create-quarantine-marker!
           nil (path/join (:directory ancestor)
                          receipt-quarantine-marker-name)))
        (catch :default _error nil))
      (append-error!
       (str "the Receipt River ledger directory was replaced while the gate "
            "ran; the ledger must be adjudicated by hand")))
    ancestor))

(defn require-safe-existing-target! [target-path]
  (when-let [stats (lstat-if-present! target-path "Receipt River file")]
    (require-regular-stats! stats "Receipt River file")))

(defn open-append-target!
  ([parent target-path]
   (open-append-target! parent target-path true))
  ([parent target-path create?]
   (require-parent-identity! parent)
   (require-safe-existing-target! target-path)
   (let [base-flags (bit-or (fs-constant! "O_RDWR")
                            (fs-constant! "O_APPEND")
                            (fs-constant! "O_NOFOLLOW")
                            (fs-constant! "O_NONBLOCK"))
         flags (case create?
                 :exclusive (bit-or base-flags (fs-constant! "O_CREAT")
                                    (fs-constant! "O_EXCL"))
                 true (bit-or base-flags (fs-constant! "O_CREAT"))
                 base-flags)
         fd (try
              (fs/openSync target-path flags 384)
              (catch :default error
                (append-error! (str "Receipt River file could not be securely opened: "
                                    (.-message error)))))]
     (try
       (let [stats (fs/fstatSync fd bigint-stat-options)]
         (require-regular-stats! stats "Receipt River file descriptor")
         (require-path-identity! target-path stats "Receipt River file"
                                 require-regular-stats!)
         (fs/fsyncSync (:fd parent))
         {:fd fd :path target-path :stats stats})
       (catch :default error
         (when (= :exclusive create?)
           ;; We created this file; do not leave an empty segment behind.
           (try
             (let [stats (fs/fstatSync fd bigint-stat-options)
                   path-stats (lstat-if-present! target-path "Receipt River file")]
               (when (and path-stats (same-file? stats path-stats)
                          (zero? (js/Number (.-size stats))))
                 (fs/unlinkSync target-path)))
             (catch :default _cleanup-error nil)))
         (close-after-open-error! fd error))))))

(defn require-target-identity! [target]
  (let [fd-stats (fs/fstatSync (:fd target) bigint-stat-options)]
    (require-regular-stats! fd-stats "Receipt River file descriptor")
    (when-not (same-file? fd-stats (:stats target))
      (append-error! "Receipt River file descriptor identity changed"))
    (require-path-identity! (:path target) fd-stats
                            "Receipt River file"
                            require-regular-stats!)
    fd-stats))

(defn safe-file-size! [stats]
  (let [size (js/Number (.-size stats))]
    (when-not (js/Number.isSafeInteger size)
      (append-error! "Receipt River file size is not safely addressable"))
    size))

(defn terminal-newline? [fd size]
  (if (zero? size)
    true
    (let [last-byte (js/Uint8Array. 1)
          read-count (fs/readSync fd last-byte 0 1 (dec size))]
      (when-not (= 1 read-count)
        (append-error! "Receipt River terminal byte could not be read"))
      (= 10 (aget last-byte 0)))))

(defn encode-edn-line! [record]
  (let [line (pr-str record)]
    (when (or (str/includes? line "\n") (str/includes? line "\r"))
      (append-error! "receipt serialization must occupy exactly one physical line"))
    (require-exact-receipt-numbers! line)
    (require-exact-receipt-tags! line)
    (let [parsed (try
                   (reader/read-string line)
                   (catch :default error
                     (append-error! (str "receipt is not readable EDN: "
                                         (.-message error)))))]
      (when-not (= record parsed)
        (append-error! "receipt does not round-trip through EDN")))
    (let [bytes (.encode (js/TextEncoder.) line)]
      ;; Immutable verification measures the complete segment (record +
      ;; newline); keep the same bound here so a written receipt always
      ;; verifies.
      (when (> (inc (.-byteLength bytes)) max-receipt-line-bytes)
        (append-error! (str "receipt exceeds " max-receipt-line-bytes
                            " UTF-8 bytes including its newline")))
      line)))

(defn read-held-target-bytes! [target]
  (let [before (require-target-identity! target)
        size (safe-file-size! before)
        bytes (js/Buffer.alloc size)]
    (loop [offset 0]
      (when (< offset size)
        (let [read-count (fs/readSync (:fd target) bytes offset
                                      (- size offset) offset)]
          (when-not (and (integer? read-count) (pos? read-count))
            (append-error! "Receipt River held content read made no progress"))
          (recur (+ offset read-count)))))
    (let [after-size (safe-file-size! (require-target-identity! target))]
      (when-not (= size after-size (.-byteLength bytes))
        (append-error! "Receipt River file changed during held content read")))
    bytes))

(defn stable-held-target-bytes! [target]
  (let [first-read (read-held-target-bytes! target)
        second-read (read-held-target-bytes! target)]
    (when-not (.equals first-read second-read)
      (append-error! "Receipt River file changed during held content validation"))
    second-read))

(defn buffer-prefix? [prefix value]
  (and (<= (.-length prefix) (.-length value))
       (.equals prefix (.subarray value 0 (.-length prefix)))))

(defn receipt-segment-directory []
  (path/join (path/dirname receipt-file) "receipts"))

(defn receipt-quarantine-marker-path []
  (path/join (path/dirname receipt-file) receipt-quarantine-marker-name))

(defn receipt-segment-file-name [iso-timestamp suffix]
  (str (str/replace iso-timestamp #"[:.]" "") "-" suffix ".edn"))

(defn new-receipt-segment-file []
  (path/join (receipt-segment-directory)
             (receipt-segment-file-name
              (.toISOString (js/Date.))
              (.toString (crypto/randomBytes 6) "hex"))))

(defn read-held-bytes!
  "Reads exactly `size` bytes from `fd`, allocating `size` plus one byte so
  growth after the fstat is seen without reading the rest of the file."
  [fd size description]
  (let [buffer (js/Buffer.alloc (inc size))
        total (loop [offset 0]
                (if (>= offset (.-length buffer))
                  offset
                  (let [n (fs/readSync fd buffer offset
                                       (- (.-length buffer) offset) offset)]
                    (if (zero? n) offset (recur (+ offset n))))))]
    (when-not (= total size)
      (append-error! (str description " changed during read")))
    (.subarray buffer 0 size)))

(defn read-working-regular-file!
  "Reads a working ledger file through a validated no-follow descriptor.
  With `max-bytes`, the held descriptor's size is checked before any
  allocation, so an oversized or sparse file fails closed with the size
  error instead of being buffered whole."
  ([file description]
   (read-working-regular-file! file description nil))
  ([file description max-bytes]
   (let [stats (lstat-if-present! file description)]
     (when-not stats
       (append-error! (str description " could not be securely opened: "
                           "it does not exist")))
     (require-regular-stats! stats description)
     (require-non-executable-stats! stats description)
     ;; Validate and read the same inode: a pathname swapped after the lstat
     ;; (for a symlink or another file) must not be followed.
     (let [fd (try
                (fs/openSync file (bit-or (fs-constant! "O_RDONLY")
                                          (fs-constant! "O_NOFOLLOW")
                                          (fs-constant! "O_NONBLOCK")))
                (catch :default error
                  (append-error! (str description " could not be securely opened: "
                                      (.-message error)))))]
       (try
         (let [fd-stats (fs/fstatSync fd bigint-stat-options)]
           (require-regular-stats! fd-stats (str description " descriptor"))
           (require-non-executable-stats! fd-stats description)
           (when-not (same-file? fd-stats stats)
             (append-error! (str description " identity changed")))
           (let [size (safe-file-size! fd-stats)
                 _ (when (and max-bytes (> size max-bytes))
                     (append-error!
                      (str description " exceeds " max-bytes " bytes: " file)))
                 bytes (read-held-bytes! fd size description)]
             (when-not (= (.-length bytes)
                          (safe-file-size! (fs/fstatSync fd bigint-stat-options)))
               (append-error! (str description " changed during read")))
             (require-path-identity! file fd-stats description
                                     (fn [path-stats path-description]
                                       (require-regular-stats!
                                        path-stats path-description)
                                       (require-non-executable-stats!
                                        path-stats path-description)))
             bytes))
         (finally
           (fs/closeSync fd)))))))

(defn read-working-receipt-segments!
  "Reads the working ledger. `own-basename` names the caller's reserved (still
  empty) segment, which is skipped along with its own lock and the writer lock
  the caller holds. Any other retained append lock quarantines the whole
  ledger. `legacy-max-bytes` bounds the frozen legacy file before it is
  allocated; it may only lose lines, so callers pass its committed size plus
  one segment of slack."
  [own-basename & [legacy-max-bytes]]
  (when (lstat-if-present! (receipt-quarantine-marker-path)
                           "Receipt River quarantine marker")
    (append-error!
     (str "a Receipt River quarantine marker quarantines the ledger until it "
          "is adjudicated: " receipt-quarantine-marker-name)))
  (let [legacy-bytes (read-working-regular-file!
                      receipt-file "Receipt River file" legacy-max-bytes)
        directory (receipt-segment-directory)
        directory-stats (lstat-if-present!
                         directory "Receipt River segment directory")]
    (when-not directory-stats
      (append-error! "Receipt River segment directory does not exist"))
    (require-directory-stats! directory-stats "Receipt River segment directory")
    (into [(legacy-segment-entry
            legacy-bytes
            (read-receipt-records!
             (decode-utf8! legacy-bytes "Receipt River ledger")))]
          (keep (fn [basename]
                  (cond
                    (= receipt-segment-readme basename)
                    (when-let [stats (lstat-if-present!
                                      (path/join directory basename)
                                      "Receipt River segment README")]
                      ;; Immutable readers require a 100644 blob; a directory or
                      ;; special file with mode 0644 would pass an executable-bit
                      ;; check and then fail verification after commit.
                      (require-regular-stats!
                       stats "Receipt River segment README")
                      (require-non-executable-stats!
                       stats "Receipt River segment README")
                      nil)
                    (= own-basename basename) nil
                    (= (str "." own-basename ".append.lock") basename) nil
                    ;; Only a reserving writer reads the working ledger, and it
                    ;; holds this lock; no other writer can be active.
                    (and own-basename (= receipt-writer-lock-name basename)) nil
                    (re-matches append-lock-name-pattern basename)
                    (append-error!
                     (str "a retained append lock quarantines the Receipt River "
                          "ledger until it is adjudicated: " basename))
                    (receipt-segment-name? basename)
                    (segment-entry
                     (str law/receipt-segment-directory "/" basename)
                     (read-working-regular-file!
                      (path/join directory basename)
                      "Receipt River segment"
                      max-receipt-line-bytes))
                    :else
                    (append-error!
                     (str "unexpected file in the Receipt River segment directory: "
                          basename)))))
          (sort (fs/readdirSync directory)))))

(defn current-committed-receipt-segments! []
  (let [revision (str/trim (git-capture! ["rev-parse" "HEAD"]))]
    (:ledger/segments (read-immutable-merged-ledger! revision))))

(defn validate-working-receipt-ledger!
  "The working ledger must extend the committed ledger, and every
  uncommitted record must satisfy the current receipt law."
  [own-basename]
  (let [committed (current-committed-receipt-segments!)
        committed-legacy (first (filter :segment/legacy? committed))
        working (read-working-receipt-segments!
                 own-basename
                 ;; The legacy file may only lose lines. One segment's worth of
                 ;; slack still lets a small append reach the clearer "frozen"
                 ;; error, while a huge replacement fails before allocation.
                 (when committed-legacy
                   (+ (.-length (:segment/bytes committed-legacy))
                      max-receipt-line-bytes)))]
    (try
      (require-valid-receipt-records!
       (ledger-extension-records! committed working))
      working
      (catch :default error
        (append-error!
         (str "Receipt River does not preserve the committed ledger: "
              (.-message error)))))))

(defn same-working-ledger?
  "True when two working-ledger reads name the same files with the same bytes."
  [before after]
  (and (= (mapv :segment/path before) (mapv :segment/path after))
       (every? true? (map #(= (segment-sha256 %1) (segment-sha256 %2))
                          before after))))

(defn working-ledger-unchanged-after-write?
  "Re-reads the whole working ledger (skipping only the caller's reserved
  segment and its locks) and compares it with the pre-gate snapshot. Any
  read or validation failure counts as a change."
  [{:keys [semantic-basename validated-working]}]
  (try
    (same-working-ledger?
     validated-working
     (validate-working-receipt-ledger! semantic-basename))
    (catch :default _error false)))

(defn reserved-segment-intact?
  "True only when the reserved segment path still names the written inode
  and holds exactly the verified bytes. Used after the reserved descriptor is
  closed, through final lock cleanup; any read failure counts as a change."
  [{:keys [target target-path written-segment-bytes]}]
  (try
    (let [expected (and written-segment-bytes @written-segment-bytes)
          bytes (when expected
                  (read-working-regular-file! target-path "Receipt River segment"))
          stats (when expected
                  (lstat-if-present! target-path "Receipt River segment"))]
      (boolean (and expected
                    stats
                    (same-file? stats (:stats target))
                    (.equals expected bytes))))
    (catch :default _error false)))

(defn require-held-ledger-unchanged! [target validated-bytes]
  (let [current-bytes (stable-held-target-bytes! target)]
    (when-not (.equals validated-bytes current-bytes)
      (append-error! "Receipt River file changed after held ledger validation"))
    current-bytes))

(declare remove-unwritten-segment!)

(defn release-writer-lock!
  "Releases the ledger-wide writer lock. A quarantine is normally carried by
  the retained per-segment lock, so the writer lock does not outlive its
  writer. When segment-lock cleanup itself fails, `close-append-reservation!`
  retains the writer lock instead. If release fails because the path was
  unlinked or replaced, recreate a quarantine marker at the writer-lock path
  (same as when both locks vanish) so a forged segment cannot become an
  ordinary extension. Other release failures are still reported."
  [parent writer-lock]
  (try
    (release-append-lock! parent writer-lock)
    (catch :default error
      ;; Gate code may unlink only the writer lock after a successful-looking
      ;; append; retain-quarantine-lock! recreates the marker when the held
      ;; descriptor no longer names the path.
      (retain-quarantine-lock! parent writer-lock)
      (append-error!
       (str "the Receipt River writer lock could not be released and must be "
            "adjudicated by hand: " (.-message error))))))

(defn release-writer-lock-after-error!
  "Releases the writer lock on a failed reservation, then rethrows `error`,
  naming a release failure too rather than hiding either one."
  [parent writer-lock error]
  (when writer-lock
    (try
      (release-writer-lock! parent writer-lock)
      (catch :default release-error
        (throw (js/Error. (str (.-message error) "; "
                               (.-message release-error)))))))
  (throw error))

(defn open-append-reservation!
  ([file]
   (open-append-reservation! file false))
  ([file validate-ledger?]
   (require-secure-append-support!)
   (let [absolute-file (path/resolve file)
         directory (path/dirname absolute-file)
         basename (path/basename absolute-file)
         parent (open-parent-directory! directory)
         ancestor (when validate-ledger?
                    (try
                      (open-parent-directory!
                       (path/dirname receipt-file)
                       "Receipt River ledger directory")
                      (catch :default error
                        (fs/closeSync (:fd parent))
                        (throw error))))]
     (try
       (let [writer-lock (when validate-ledger?
                           (acquire-receipt-writer-lock! parent))
             lock (try
                    (acquire-append-lock! parent basename)
                    (catch :default error
                      (release-writer-lock-after-error!
                       parent writer-lock error)))
             retain-lock? (atom false)]
         (try
           (require-append-lock-identity! lock)
           (let [target-path (path/join (:proc-path parent) basename)
                 ;; Semantic evidence must extend an initialized ledger. Generic
                 ;; append callers retain the explicit initialization path.
                 ;; Semantic evidence goes into a brand-new immutable segment,
                 ;; after the whole working ledger is checked against HEAD.
                 validated-working (when validate-ledger?
                                     (validate-working-receipt-ledger! basename))
                 target (open-append-target! parent target-path
                                             (if validate-ledger?
                                               :exclusive
                                               true))]
             (try
               {:requested-file absolute-file
                :directory directory
                :parent parent
                :ancestor ancestor
                :lock lock
                :writer-lock writer-lock
                :target target
                :target-path target-path
                :semantic-basename (when validate-ledger? basename)
                ;; The working ledger as validated before the gate ran; at
                ;; write time it must be unchanged apart from the reserved
                ;; target, so gate output cannot add its own segments.
                :validated-working validated-working
                :held-ledger-bytes
                (let [held (stable-held-target-bytes! target)]
                  (when (and validate-ledger? (pos? (.-length held)))
                    (append-error! "new Receipt River segment is not empty"))
                  held)
                :target-open? (atom true)
                :quarantined? (atom false)
                :parent-replaced? (atom false)
                :write-started? (atom false)
                :write-verified? (atom false)
                ;; Bytes of the verified write, kept so the reserved segment
                ;; can be revalidated after its descriptor is closed.
                :written-segment-bytes (atom nil)}
               (catch :default error
                 (when (and validate-ledger?
                            (not (remove-unwritten-segment!
                                  {:parent parent :target target
                                   :target-path target-path})))
                   ;; The reserved segment holds content this writer did not
                   ;; write: keep the lock as a quarantine marker.
                   (reset! retain-lock? true))
                 (close-after-open-error! (:fd target) error))))
           (catch :default error
             (release-writer-lock-after-error!
              parent writer-lock
              (try
                (if @retain-lock?
                  (retain-quarantine-lock! parent lock)
                  (release-append-lock! parent lock))
                error
                (catch :default cleanup-error cleanup-error))))))
       (catch :default error
         (try
           (fs/closeSync (:fd parent))
           (finally
             (when ancestor
               (fs/closeSync (:fd ancestor)))))
         (throw error))))))

(defn held-inode-unlinked?
  "True only when the still-open reserved descriptor proves its inode has no
  remaining directory entry. A closed descriptor proves nothing."
  [{:keys [target target-open?]}]
  (try
    (and (or (nil? target-open?) @target-open?)
         (let [stats (fs/fstatSync (:fd target) bigint-stat-options)]
           (and (same-file? stats (:stats target))
                (= "0" (str (.-nlink stats))))))
    (catch :default _error false)))

(defn remove-unwritten-segment!
  "Removes a reserved segment that was never written, while the lock is still
  held, so an aborted gate cannot leave an empty file in the ledger."
  [{:keys [parent target target-path] :as reservation}]
  ;; Returns true only when no reserved segment is left behind: the held
  ;; descriptor proves the inode has no remaining link, either after removing
  ;; this writer's still-empty file or because its path was already gone. Anything else (content injected
  ;; into it, a renamed or replaced file, a failed unlink) is false, and the
  ;; caller retains the lock as a quarantine marker. Path absence alone is not
  ;; proof: the inode may live on under another well-named segment.
  (try
    (let [stats (lstat-if-present! target-path "Receipt River file")]
      (cond
        (nil? stats) (held-inode-unlinked? reservation)
        (and (same-file? stats (:stats target))
             (zero? (safe-file-size! stats)))
        (do (fs/unlinkSync target-path)
            (fs/fsyncSync (:fd parent))
            ;; A hard link made while the gate ran would keep the inode (and
            ;; anything later written through it) alive under another name.
            (held-inode-unlinked? reservation))
        :else false))
    (catch :default _error false)))

(defn reserved-basename-present?
  "True when the reservation's basename names any entry in its held segment
  directory. After an unwritten reservation was removed, that is content this
  writer did not write. A failed lstat counts as present (fail closed)."
  [{:keys [parent semantic-basename]}]
  (try
    (boolean (lstat-if-present! (path/join (:proc-path parent) semantic-basename)
                                "Receipt River segment"))
    (catch :default _error true)))

(defn close-append-reservation!
  [{:keys [parent ancestor lock writer-lock target target-open? write-started?
           write-verified? quarantined? parent-replaced? semantic-basename]
    :as reservation}]
  (let [unwritten-removed? (atom false)
        retain-writer-lock? (atom false)
        late-replacement-error (atom nil)
        ;; The canonical segment directory is no longer the one this
        ;; reservation holds. Locks and cleanup below reach only the renamed
        ;; directory, so the quarantine goes into the stable `.ημ` ancestor.
        ;; Kept live until writer-lock release: a rename after the per-segment
        ;; unlock would otherwise miss the one-time snapshot below.
        directory-replaced? (atom
                             (boolean
                              (and ancestor
                                   (or (and parent-replaced? @parent-replaced?)
                                       (not (parent-identity-intact? parent))))))]
    (try
      (try
        (when @directory-replaced?
          (establish-ledger-quarantine! ancestor))
        (finally
          ;; A gate that threw never reached append-reserved-edn-line!, so
          ;; compare the ledger with its pre-gate snapshot here as well; a
          ;; segment it added keeps the lock as the quarantine marker.
          (when (and semantic-basename
                     quarantined?
                     (not @write-started?)
                     (not @quarantined?)
                     (not (working-ledger-unchanged-after-write? reservation)))
            (reset! quarantined? true))
          ;; Adjudicate the unwritten reservation while its descriptor is still
          ;; held, so an unlinked inode can be told apart from a renamed one.
          (reset! unwritten-removed?
                  (if (and semantic-basename (not @write-started?))
                    (remove-unwritten-segment! reservation)
                    true))
          (when @target-open?
            (fs/closeSync (:fd target))
            (reset! target-open? false))))
      (finally
        (try
          (if (or (and @write-started? (not @write-verified?))
                  (not @unwritten-removed?)
                  (and quarantined? @quarantined?)
                  @directory-replaced?
                  ;; The verified segment must still be exactly what was
                  ;; written when its lock is about to be released.
                  (and semantic-basename
                       @write-verified?
                       (not (reserved-segment-intact? reservation))
                       (do (reset! late-replacement-error
                                   (js/Error.
                                    (str "the Receipt River segment changed "
                                         "after it was written; the ledger "
                                         "must be adjudicated by hand")))
                           true)))
            ;; A short write, fsync/close failure, post-write identity change,
            ;; content injected into the reserved segment, a reservation moved
            ;; out from under its path, a segment added while the gate ran, or
            ;; a replaced segment directory.
            ;; Retain the lock as a durable quarantine marker so a later
            ;; writer cannot normalize the damage. A vanished lock path is
            ;; recreated, since closing its descriptor would preserve nothing.
            (retain-quarantine-lock! parent lock)
            (release-append-lock! parent lock))
          (catch :default error
            ;; Segment-lock identity or cleanup failed (for example the lock
            ;; was unlinked while a candidate added its own segment). Keep the
            ;; writer lock as the quarantine marker so the next run cannot
            ;; treat that segment as an ordinary extension; if the writer lock
            ;; path is gone too, a fresh marker is created in its place.
            (reset! retain-writer-lock? true)
            (throw error))
          (finally
            ;; Recheck after segment-lock cleanup. Gate-spawned work that
            ;; watches the unlock can rename/replace the directory before the
            ;; writer lock is released; following the held descriptor then
            ;; would clear that lock in the renamed directory and leave the
            ;; canonical ledger unquarantined.
            (when (and ancestor
                       (not @directory-replaced?)
                       (not (parent-identity-intact? parent)))
              (reset! directory-replaced? true)
              (establish-ledger-quarantine! ancestor)
              (reset! retain-writer-lock? true)
              (reset! late-replacement-error
                      (js/Error.
                       (str "the Receipt River segment directory was replaced "
                            "during lock cleanup; the ledger must be "
                            "adjudicated by hand"))))
            ;; An unwritten reservation was removed before unlock. Gate-spawned
            ;; work that watches the unlock can recreate that basename; the
            ;; whole-ledger comparison below still skips it, so check here.
            (when (and writer-lock
                       semantic-basename
                       @unwritten-removed?
                       (not @write-started?)
                       (not @retain-writer-lock?)
                       (not @directory-replaced?)
                       (not (and quarantined? @quarantined?))
                       (lstat-if-present!
                        (path/join (:proc-path parent) semantic-basename)
                        "Receipt River segment"))
              (reset! retain-writer-lock? true)
              (when-not @late-replacement-error
                (reset! late-replacement-error
                        (js/Error.
                         (str "the Receipt River ledger changed during lock "
                              "cleanup: the reserved basename reappeared "
                              "after the unwritten reservation was removed; "
                              "the ledger must be adjudicated by hand")))))
            ;; A verified write, or an unwritten (failed) reservation, released
            ;; its segment lock above; the writer lock still serializes this
            ;; checkout. Compare the whole ledger once more so a segment added
            ;; during lock cleanup keeps the writer lock as the quarantine
            ;; marker. Only a verified write has a reserved segment to check.
            (when (and writer-lock
                       semantic-basename
                       (or @write-verified? (not @write-started?))
                       (not @retain-writer-lock?)
                       (not @directory-replaced?)
                       (not (and quarantined? @quarantined?))
                       (not (and (working-ledger-unchanged-after-write?
                                  reservation)
                                 (or (not @write-verified?)
                                     (reserved-segment-intact? reservation)))))
              (reset! retain-writer-lock? true)
              (when-not @late-replacement-error
                (reset! late-replacement-error
                        (js/Error.
                         (str "the Receipt River ledger changed during lock "
                              "cleanup: only the reserved segment may be "
                              "added; the ledger must be adjudicated by "
                              "hand")))))
            (try
              (when writer-lock
                (if (or @retain-writer-lock? @directory-replaced?)
                  (retain-quarantine-lock! parent writer-lock)
                  (release-writer-lock! parent writer-lock)))
              ;; Writer-lock unlink can be the signal gate-spawned work waits
              ;; on before renaming the segment directory. Recheck while the
              ;; ancestor descriptor is still held so a post-release replace
              ;; still quarantines the canonical ledger.
              (when (and ancestor
                         (not @directory-replaced?)
                         (not (parent-identity-intact? parent)))
                (reset! directory-replaced? true)
                (establish-ledger-quarantine! ancestor)
                (reset! late-replacement-error
                        (js/Error.
                         (str "the Receipt River segment directory was replaced "
                              "after writer-lock release; the ledger must be "
                              "adjudicated by hand"))))
              ;; Content can change without replacing the directory: work that
              ;; waited for the writer-lock unlink may add or rewrite a
              ;; segment. Revalidate the whole ledger and reserved bytes; on
              ;; mismatch recreate the writer-lock path as a quarantine marker.
              ;; The comparison skips the reserved basename, so an unwritten
              ;; (removed) reservation must also still be absent: delayed work
              ;; can recreate it under that name after this release.
              (when (and writer-lock
                         semantic-basename
                         (or @write-verified? (not @write-started?))
                         (not @directory-replaced?)
                         (not (and quarantined? @quarantined?))
                         (not (and (working-ledger-unchanged-after-write?
                                    reservation)
                                   (if @write-verified?
                                     (reserved-segment-intact? reservation)
                                     (not (and @unwritten-removed?
                                               (reserved-basename-present?
                                                reservation)))))))
                (create-quarantine-marker! parent (:path writer-lock))
                (when-not @late-replacement-error
                  (reset! late-replacement-error
                          (js/Error.
                           (str "the Receipt River ledger changed after "
                                "writer-lock release: only the reserved "
                                "segment may be added; the ledger must be "
                                "adjudicated by hand")))))
              (finally
                (try
                  (fs/closeSync (:fd parent))
                  (finally
                    (try
                      (when ancestor
                        (fs/closeSync (:fd ancestor)))
                      (finally
                        (when-let [error @late-replacement-error]
                          (throw error))))))))))))))

(defn with-append-reservation!
  ([file run!]
   (with-append-reservation! file false run!))
  ([file validate-ledger? run!]
   (let [reservation (open-append-reservation! file validate-ledger?)]
     (try
       (try
         (run! reservation)
         (catch :default error
           (when (and (:receipt-river/quarantine (ex-data error))
                      (:quarantined? reservation))
             (reset! (:quarantined? reservation) true))
           (throw error)))
       (finally
         (close-append-reservation! reservation))))))

(defn require-reserved-parent-identity!
  "Checks the held segment directory still names its path, recording a
  failure so cleanup quarantines the ledger through the `.ημ` ancestor."
  [{:keys [parent parent-replaced?]}]
  (try
    (require-parent-identity! parent)
    (catch :default error
      (when parent-replaced?
        (reset! parent-replaced? true))
      (throw error))))

(defn append-reserved-edn-line!
  [{:keys [requested-file directory lock target target-path
           held-ledger-bytes target-open? write-started? write-verified?
           quarantined? semantic-basename validated-working
           written-segment-bytes]
    :as reservation}
   record
   line]
  (*secure-append-phase-hook*
   :before-write
   {:requested-file requested-file
    :directory directory
    :target-path target-path})
  (require-reserved-parent-identity! reservation)
  (require-append-lock-identity! lock)
  ;; Gate code may unlink only the ledger-wide writer lock; never write a
  ;; receipt without it.
  (when-let [writer-lock (:writer-lock reservation)]
    (require-append-lock-identity! writer-lock))
  (require-held-ledger-unchanged! target held-ledger-bytes)
  ;; A gate can run for a long time; the rest of the ledger must still extend
  ;; HEAD at the moment this receipt is written.
  (when semantic-basename
    ;; The ledger validated before the gate ran. Any failure now — or a
    ;; successful revalidation that differs — means the working ledger changed
    ;; while the gate ran, so quarantine before throwing so cleanup retains
    ;; the lock and forged segments cannot become ordinary extensions.
    (let [working (try
                    (validate-working-receipt-ledger! semantic-basename)
                    (catch :default error
                      (reset! quarantined? true)
                      (throw error)))]
      (when-not (same-working-ledger? validated-working working)
        (reset! quarantined? true)
        (append-error!
         "Receipt River ledger changed while the gate ran: only the reserved segment may be added"))
      (when (contains? (into #{} (comp (mapcat :segment/records) (map record-id))
                             working)
                       (record-id record))
        (append-error! "receipt repeats a record already in the ledger"))))
  (when semantic-basename
    (require-non-executable-stats! (require-target-identity! target)
                                   "Receipt River segment"))
  (let [before (require-target-identity! target)
        before-size (safe-file-size! before)
        separator (if (terminal-newline? (:fd target) before-size)
                    ""
                    "\n")
        payload (.encode (js/TextEncoder.)
                         (str separator line "\n"))
        expected-bytes (js/Buffer.concat
                        #js [held-ledger-bytes payload])]
    (when-not (= before-size
                 (safe-file-size!
                  (require-target-identity! target)))
      (append-error!
       "Receipt River file changed while the append lock was held"))
    (reset! write-started? true)
    (write-all! (:fd target) payload)
    (fs/fsyncSync (:fd target))
    (require-append-lock-identity! lock)
    (let [after (require-target-identity! target)
          expected-size (+ before-size (.-byteLength payload))]
      (when-not (= expected-size (safe-file-size! after))
        (append-error! "Receipt River append size verification failed")))
    (require-reserved-parent-identity! reservation)
    (*secure-append-phase-hook*
     :after-write
     {:requested-file requested-file
      :directory directory
      :target-path target-path})
    (require-reserved-parent-identity! reservation)
    (require-append-lock-identity! lock)
    (when-let [writer-lock (:writer-lock reservation)]
      (require-append-lock-identity! writer-lock))
    (let [final-stats (require-target-identity! target)]
      (when semantic-basename
        (require-non-executable-stats! final-stats "Receipt River segment")))
    (when-not (.equals expected-bytes (stable-held-target-bytes! target))
      (append-error! "Receipt River bytes changed after append"))
    ;; The pre-write comparison is not the last word: gate-spawned work can
    ;; wait for the reserved segment to become nonempty and then add a forged
    ;; segment. Compare the whole ledger again before the locks are released.
    (when (and semantic-basename
               (not (working-ledger-unchanged-after-write? reservation)))
      (reset! quarantined? true)
      (append-error!
       (str "Receipt River ledger changed after the receipt was written: "
            "only the reserved segment may be added")))
    ;; Closing is part of the verified write boundary. If it fails, the outer
    ;; reservation cleanup retains the lock as a quarantine marker.
    (fs/closeSync (:fd target))
    (reset! target-open? false)
    (when written-segment-bytes
      (reset! written-segment-bytes expected-bytes))
    (*secure-append-phase-hook*
     :after-close
     {:requested-file requested-file
      :directory directory
      :target-path target-path})
    ;; The close is observable (CLOSE_WRITE), so gate-spawned work can rewrite
    ;; or replace the segment right after it; the last byte comparison above
    ;; ran before that. Revalidate the path and content now.
    (when (and semantic-basename
               (not (reserved-segment-intact? reservation)))
      (reset! quarantined? true)
      (append-error!
       "Receipt River segment changed after it was closed"))
    (reset! write-verified? true)
    record))

(defn append-edn-line! [file record]
  ;; Reject malformed or oversized records before opening or creating anything.
  (let [line (encode-edn-line! record)]
    (with-append-reservation!
      file
      #(append-reserved-edn-line! % record line))))

(defn validated-evidence-receipt! [result]
  (let [receipt (evidence-receipt result
                                  (.toISOString (js/Date.))
                                  (os/hostname)
                                  (runtime-adapter))]
    (when-not (law/evidence-receipt? receipt)
      (throw (js/Error.
              (str "Gate result cannot be recorded as an evidence receipt: "
                   (pr-str (law/result-errors result))))))
    receipt))

(defn append-evidence-receipt!
  ([result]
   (let [receipt (validated-evidence-receipt! result)
         line (encode-edn-line! receipt)]
     (with-append-reservation!
       (new-receipt-segment-file) true
       #(append-reserved-edn-line! % receipt line))))
  ([reservation result]
   (let [receipt (validated-evidence-receipt! result)]
     (append-reserved-edn-line! reservation receipt
                                (encode-edn-line! receipt)))))

(defn result-exit [{:result/keys [outcome]}]
  (case outcome
    :passed 0
    :failed 1
    :unavailable 3
    :blocked 4
    :not-applicable 0
    2))

(defn list-gates! [catalog {:keys [only kinds]}]
  (let [paths (if only
                (require-mapped-repositories! catalog only)
                (set (keys (:catalog/repositories catalog))))
        gates (law/select-gates catalog (sort paths) kinds)]
    (when (empty? gates)
      (throw (js/Error. "No mapped gates match the requested repositories and kinds")))
    (doseq [gate gates]
      (println (str (:gate/id gate))
               (name (:gate/kind gate))
               (name (:gate/execution gate))
               (str/join " " (:gate/command gate))
               (:gate/source gate)))
    0))

(defn gate-repository-path [catalog gate]
  (some (fn [[repository-path repository]]
          (when (some #(= (:gate/id gate) (:gate/id %))
                      (:repository/gates repository))
            repository-path))
        (:catalog/repositories catalog)))

(defn quarantine-if-ledger-changed!
  "When a gate throws without writing, mark the reservation quarantined if the
  working ledger no longer matches the pre-gate snapshot (or no longer validates)."
  [{:keys [quarantined? semantic-basename validated-working]}]
  (when (and semantic-basename quarantined? (not @quarantined?))
    (try
      (let [working (validate-working-receipt-ledger! semantic-basename)]
        (when-not (same-working-ledger? validated-working working)
          (reset! quarantined? true)))
      (catch :default _
        (reset! quarantined? true)))))

(defn run-selected-gates! [catalog catalog-identity {:keys [only kinds]}]
  ;; Fail before a gate executes when this host cannot durably retain its result.
  (require-secure-append-support!)
  (let [paths (require-repositories! catalog only)
        gates (law/select-gates catalog (sort only) kinds)]
    (when (empty? gates)
      (throw (js/Error. "No mapped gates match the requested repositories and kinds")))
    (let [results (mapv (fn [gate]
                          (let [repository-path (gate-repository-path catalog gate)]
                            (when-not repository-path
                              (throw (js/Error.
                                      (str "Gate repository is not in the catalog: "
                                           (:gate/id gate)))))
                            ;; Hold the exact parent, writer lock, and target
                            ;; descriptor before the gate starts. The same
                            ;; reservation retains the resulting receipt.
                            (with-append-reservation!
                              (new-receipt-segment-file) true
                              (fn [reservation]
                                (let [result
                                      (try
                                        (run-gate!
                                         (get paths repository-path)
                                         gate
                                         catalog-identity)
                                        (catch :default error
                                          ;; Gate threw before append: revalidate
                                          ;; so a forged segment still quarantines.
                                          (quarantine-if-ledger-changed! reservation)
                                          (throw error)))]
                                  ;; Quarantine only when receipt construction
                                  ;; fails before append-reserved (which has its
                                  ;; own revalidation / lock-identity paths).
                                  (let [receipt
                                        (try
                                          (validated-evidence-receipt! result)
                                          (catch :default error
                                            (quarantine-if-ledger-changed! reservation)
                                            (throw error)))
                                        line
                                        (try
                                          (encode-edn-line! receipt)
                                          (catch :default error
                                            (quarantine-if-ledger-changed! reservation)
                                            (throw error)))]
                                    (append-reserved-edn-line! reservation receipt line))
                                  result)))))
                        gates)
          summary (law/summarize-results results)]
      (println "SUMMARY" (pr-str summary))
      (reduce max 0 (map result-exit results)))))

(defn appended-receipt-records! [base-bytes head-bytes]
  ;; Only an exact immutable prefix receives historical compatibility. Keep
  ;; current envelope/semantic validation at callers on the returned suffix.
  (when-not (buffer-prefix? base-bytes head-bytes)
    (throw (js/Error.
            "Receipt River head does not preserve the base bytes as a prefix")))
  (let [base-length (.-length base-bytes)
        head-length (.-length head-bytes)
        appended (.subarray head-bytes base-length head-length)]
    (if (zero? (.-length appended))
      []
      (do
        (when (and (pos? base-length)
                   (not= 10 (.at base-bytes -1))
                   (not= 10 (.at appended 0)))
          (throw (js/Error.
                  "Receipt River append must begin on a new line")))
        (when-not (= 10 (.at appended -1))
          (throw (js/Error.
                  "Receipt River appended records must end with a newline")))
        (read-receipt-records!
         (decode-utf8! appended "appended Receipt River records"))))))

(defn verify-receipts! [{:keys [at base]}]
  (when-not (law/git-commit-id? base)
    (throw (js/Error. "verify-receipts requires --base with a full Git commit ID")))
  (let [base-ledger (read-immutable-merged-ledger! base)
        ledger (read-immutable-merged-ledger! at)
        _ (require-segment-directory-kept! base-ledger ledger)
        records (:ledger/records ledger)
        appended-records (ledger-extension-records!
                          (:ledger/segments base-ledger)
                          (:ledger/segments ledger))
        candidates (filter #(= law/evidence-receipt-origin (:origin %)) records)
        evidence-count (count candidates)
        counts (require-valid-receipt-records! appended-records)]
    (println "PASS"
             (pr-str (assoc (:ledger/identity ledger)
                            :ledger/base-revision base
                            :ledger/total-receipts (count records)
                            :ledger/appended-receipts (count appended-records)
                            :ledger/appended-evidence-receipts
                            (:receipt/evidence counts)
                            :ledger/legacy-evidence-receipts
                            (count (filter #(nil? (:evidence/schema %)) candidates))
                            :ledger/evidence-receipts evidence-count)))
    0))

(defn -main [& args]
  (try
    (let [[command & option-args] args
          {:keys [catalog catalog-identity]} (read-catalog-bundle)
          catalog (validate-catalog! catalog)
          options (parse-args option-args)]
      (case command
        "validate" (do (println "PASS quality gate catalog") 0)
        "list" (list-gates! catalog options)
        "run" (run-selected-gates! catalog catalog-identity options)
        "verify-receipts" (verify-receipts! options)
        (throw (js/Error. (str "Unknown command: " (or command "<missing>"))))))
    (catch :default error
      (binding [*out* *err*] (println (.-message error)))
      2)))

(when (= nbb/*file* (nbb/invoked-file))
  (set! (.-exitCode js/process) (apply -main *command-line-args*)))
