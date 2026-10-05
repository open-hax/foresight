#!/usr/bin/env bb
;; Offline regression for the PR #105 correction artifact. This validates
;; retained bytes, local causal references, and the scoped interpretation.
;; It is not the complete Clio/Malli gate or a live MongoDB integration test.
(require '[babashka.fs :as fs]
         '[clojure.edn :as edn]
         '[clojure.string :as str]
         '[clojure.test :refer [deftest is testing run-tests]])

(def root (or (first *command-line-args*) "."))
(def predecessor "4da05854-5161-4351-a1d3-daf00fb4c3f1")
(def successor "416ca87e-8071-4336-997d-9a4a391eb9e4")
(def roles [:run :findings :actions :evidence :relations])
(def original-blobs
  {"run" "62461d15952d53163b73258c2e15f08f5278f851"
   "findings" "b39fba757f885d7ba987c3432967725483367578"
   "actions" "14cf2f72f08e555db8583b0ef80955edc57eade3"
   "evidence" "d60f9a4a41519df736a0bfc6bcac4de0d2d1bec1"
   "relations" "d852b18e02841306430d334b63cbdf7b819bdfa2"})

(defn ledger-path [run role]
  (fs/path root ".ημ" "archaeology" "ledgers" run (str (name role) ".edn")))

(defn read-one [text]
  (with-open [reader (java.io.PushbackReader. (java.io.StringReader. text))]
    (let [eof (Object.)
          value (edn/read {:eof eof} reader)]
      (when (or (identical? eof value)
                (not (identical? eof (edn/read {:eof eof} reader))))
        (throw (ex-info "Expected exactly one EDN form" {})))
      value)))

(defn records [run role]
  (let [resource (-> (fs/path root ".ημ" "archaeology" "resources" (str run ".edn"))
                     str slurp read-one :resources first)
        path (get-in resource [:archaeology/ledgers role :ledger/path])]
    (->> (str/split-lines (slurp (str (fs/path root path))))
       (remove str/blank?)
       (mapv read-one))))

(defn git-blob [path]
  (let [bytes (java.nio.file.Files/readAllBytes (fs/path path))
        digest (java.security.MessageDigest/getInstance "SHA-1")]
    (.update digest (.getBytes (str "blob " (alength bytes) "\u0000") "UTF-8"))
    (.update digest bytes)
    (apply str (map #(format "%02x" (bit-and % 255)) (.digest digest)))))

(defn successor-present? []
  (every? #(fs/exists? (ledger-path successor %)) roles))

(deftest original-publication-is-byte-identical
  (doseq [[role sha] original-blobs]
    (is (= sha (git-blob (ledger-path predecessor role))) role))
  (is (= "bf56e96e733b4b3aee097321a3c0463d67255721"
         (git-blob (fs/path root ".ημ" "archaeology" "rejected-inputs"
                           predecessor "resource-original.edn")))))

(deftest successor-is-a-causally-bound-separate-publication
  (is (successor-present?) "The corrective successor must actually exist")
  (when (successor-present?)
    (let [before (mapcat #(records predecessor %) roles)
          after (mapcat #(records successor %) roles)
          ids (set (map :event/id (concat before after)))
          previous-schema (into {} (map (juxt :event/type :event/schema)) before)
          resource (-> (fs/path root ".ημ" "archaeology" "resources"
                               (str successor ".edn"))
                       str slurp read-one :resources first)]
      (is (= successor (:archaeology/run-id resource)))
      (is (= [predecessor] (:event/causes (first (records successor :run))))
          "Run parents name runs; finer evidence causes belong to their own events")
      (is (= 18 (count after)))
      (is (= (count after) (count (set (map :event/id after)))))
      (is (empty? (filter (set (map :event/id before)) (map :event/id after))))
      (doseq [event after]
        (is (= (str "archaeology:" successor) (:event/subject event)))
        (is (= (get previous-schema (:event/type event)) (:event/schema event))
            "Reuse the observed historical schema reference; invent no hash")
        (is (every? ids (:event/causes event)))
        (is (= (count (:event/causes event)) (count (set (:event/causes event)))))
        (is (not (some #{(:event/id event)} (:event/causes event)))))
      (doseq [role roles]
        (let [events (records successor role)]
          (is (= (vec (range 1 (inc (count events)))) (mapv :event/seq events)))
          (is (= #{(str "archaeology:" (name role) ":" successor)}
                 (set (map :event/stream events))))
          (is (= (str ".ημ/archaeology/ledgers/" successor "/" (name role) ".edn")
                 (get-in resource [:archaeology/ledgers role :ledger/path])))
          (doseq [[previous current] (partition 2 1 events)]
            (is (some #{(:event/id previous)} (:event/causes current)))))))))

(deftest correction-narrows-only-the-expiry-interpretation
  (is (successor-present?) "A note alone cannot replace the corrective ledger")
  (when (successor-present?)
    (let [before (mapv :event/data (records predecessor :findings))
          after (mapv :event/data (records successor :findings))
          corrected (last after)
          relation (-> (records successor :relations) first :event/data)
          action (-> (records successor :actions) first :event/data)]
      (is (= (subvec before 0 2) (subvec after 0 2)))
      (is (= "TtlCleanedAtomicCounter<Key>" (:finding/candidate-name corrected)))
      (is (= :knoxx/chat-rate-limit-is-ttl-cleaned-atomic-counter (:finding/id corrected)))
      (is (str/includes? (:finding/claim corrected) "60350c6d-6599-4013-8adf-eb275d5f3711"))
      (is (str/includes? (:finding/claim corrected) "before asynchronous TTL deletion"))
      (is (str/includes? (:finding/claim corrected) "expiresAt remains unchanged"))
      (is (not (str/includes? (:finding/claim corrected) "resets with expiry")))
      (is (= :supersedes (:relation/status relation)))
      (is (= :run/consumes (:relation/kind relation)))
      (is (= successor (:relation/from-id relation)))
      (is (= predecessor (:relation/to-id relation)))
      (is (= :seed/continue-knoxx-shape-inventory (:action/id action)))
      (is (= :open (:action/status action)))
      (is (= (mapv :event/data (records predecessor :evidence))
             (mapv :event/data (records successor :evidence)))))))

(deftest reader-refuses-multiple-records-in-one-line
  (is (thrown? Exception (read-one "{:event/id 1} {:event/id 2}"))))

(deftest malformed-predecessor-relations-are-preserved-not-admitted
  (is (thrown? Exception (edn/read-string
                         (first (str/split-lines
                                 (slurp (str (ledger-path predecessor :relations)))))))))

(defn counter-update-model
  "Bounded model of the pinned key-only $inc/$setOnInsert operation, not MongoDB.
   The explicit cleanup step belongs to the caller of this model."
  [rows key now window]
  (if (contains? rows key)
    (update-in rows [key :count] inc)
    (assoc rows key {:count 1 :expires-at (+ now window)})))

(deftest logical-expiry-does-not-imply-row-deletion
  (let [rows {"example" {:count 2 :expires-at 10}}
        before-cleanup (counter-update-model rows "example" 11 10)
        after-cleanup (counter-update-model (dissoc rows "example") "example" 11 10)]
    (testing "A still-present expired row matches the key-only update"
      (is (= 3 (get-in before-cleanup ["example" :count])))
      (is (= 10 (get-in before-cleanup ["example" :expires-at]))))
    (testing "Only absence admits the set-on-insert branch"
      (is (= 1 (get-in after-cleanup ["example" :count])))
      (is (= 21 (get-in after-cleanup ["example" :expires-at]))))))

(let [{:keys [fail error]} (run-tests)]
  (when (pos? (+ fail error)) (System/exit 1)))
