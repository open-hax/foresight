;; SPDX-License-Identifier: GPL-3.0-or-later
(ns receipt-history-test
  (:require [cljs.test :refer [deftest is]]
            [clojure.string :as str]
            [evidence :as cli]
            [foresight.evidence :as evidence]
            [foresight.receipt-history :as history]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]))

(def prefix-bytes
  (.subarray (fs/readFileSync (path/join cli/root evidence/receipt-ledger-path))
             0 (:ledger/bytes history/historical-prefix)))

(def prefix-records (cli/read-receipt-records! (.toString prefix-bytes "utf8")))
(def registration (history/archive-registration "2026-09-12T12:00:00Z" "test"))
(def revision (:ledger/revision history/historical-prefix))

(defn extending [records]
  (js/Buffer.concat
   #js [prefix-bytes
        (js/Buffer.from (apply str (map #(str (pr-str %) "\n") records)) "utf8")]))

(defn immutable-ledger [bytes]
  {:ledger/identity {:ledger/path evidence/receipt-ledger-path
                     :ledger/revision revision
                     :ledger/sha256 (cli/sha256 bytes)}
   :ledger/bytes bytes
   :ledger/records (cli/read-receipt-records! (.toString bytes "utf8"))})

(defn with-history-file [run!]
  (let [directory (fs/mkdtempSync (path/join (os/tmpdir) "receipt-history-"))
        file (path/join directory "receipts.edn")]
    (fs/writeFileSync file prefix-bytes)
    (try
      (with-redefs [cli/receipt-file file
                    cli/current-committed-receipt-bytes! (constantly prefix-bytes)]
        (run! file))
      (finally (fs/rmSync directory #js {:recursive true :force true})))))

(deftest exact-history-is-archival-with-current-suffix-law
  (is (= (:ledger/sha256 history/historical-prefix) (cli/sha256 prefix-bytes)))
  (is (= 137 (count prefix-records)))
  (let [counts (cli/require-valid-receipt-bytes! prefix-bytes)]
    (is (= 137 (:receipt/total counts)))
    (is (= 11 (:receipt/unverified-archive counts)))
    (is (= 3 (:receipt/evidence counts))))
  (is (= 138 (:receipt/total (cli/require-valid-receipt-bytes!
                            (extending [registration])))))
  (doseq [line (keys history/unverified-rows)]
    (let [record (nth prefix-records (dec line))]
      (is (not (evidence/receipt-envelope? record)))
      (is (not (evidence/evidence-receipt? record))))))

(deftest base-selection-cannot-grandfather-new-malformed-records
  (let [bad (extending [(nth prefix-records 93)])]
    (is (thrown-with-msg? js/Error #"invalid receipt envelopes"
                         (cli/require-valid-receipt-bytes! bad)))
    (with-redefs [cli/read-immutable-receipt-ledger!
                  (constantly (immutable-ledger bad))]
      (is (thrown-with-msg? js/Error #"invalid receipt envelopes"
                           (cli/verify-receipts! {:base revision :at revision}))))))

(deftest byte-mutation-crlf-and-truncation-do-not-inherit-exceptions
  (let [changed (js/Buffer.from prefix-bytes)
        crlf (js/Buffer.from (str/replace (.toString prefix-bytes "utf8")
                                        "\n" "\r\n") "utf8")
        truncated (.subarray prefix-bytes 0 (dec (.-length prefix-bytes)))]
    (.writeUInt8 changed 32 1)
    (doseq [bytes [changed crlf truncated]]
      (is (not (cli/historical-prefix? bytes)))
      (is (thrown? js/Error (cli/require-valid-receipt-bytes! bytes)))
      (is (thrown-with-msg? js/Error #"does not preserve the base bytes"
                           (cli/appended-receipt-records! prefix-bytes bytes))))
    (is (thrown-with-msg? js/Error #"does not preserve the base bytes"
                         (cli/appended-receipt-records!
                          prefix-bytes (js/Buffer.alloc 0))))))

(deftest current-suffix-framing-cannot-be-grandfathered-by-base
  (let [full (extending [registration])
        unterminated (.subarray full 0 (dec (.-length full)))]
    (with-redefs [cli/read-immutable-receipt-ledger!
                  (constantly (immutable-ledger unterminated))]
      (is (thrown-with-msg? js/Error #"must end with a newline"
                           (cli/verify-receipts! {:base revision :at revision}))))))

(deftest archival-whitelist-refuses-evidence-even-with-a-matching-row-digest
  (let [line 94
        digest (get history/unverified-rows line)
        original (nth prefix-records (dec line))]
    (is (history/unverified-row? line digest original))
    (doseq [receipt [(assoc original :origin evidence/evidence-receipt-origin)
                     (assoc original :evidence/result {})
                     (assoc original :evidence/schema 2)]]
      (is (not (history/unverified-row? line digest receipt))))
    (is (not (history/unverified-row? 138 digest original)))))

(deftest archive-registration-is-exact-unique-and-unverified
  (is (evidence/receipt-envelope? registration))
  (is (history/valid-archive-registration? registration))
  (is (not (evidence/evidence-receipt? registration)))
  (doseq [forgery [(assoc registration :archive/classification :accepted)
                   (assoc-in registration [:archive/prefix :ledger/bytes] 1)
                   (assoc registration :archive/rows [])
                   (assoc registration :origin "another-writer")
                   (assoc registration :evidence/result {})]]
    (is (not (history/valid-archive-registration? forgery)))
    (is (thrown-with-msg? js/Error #"archive registration"
                         (cli/require-valid-receipt-bytes! (extending [forgery])))))
  (is (thrown-with-msg? js/Error #"duplicate.*archive registration"
                       (cli/require-valid-receipt-bytes!
                        (extending [registration registration]))))
  (is (thrown-with-msg? js/Error #"archive registration"
                       (cli/require-valid-receipt-bytes!
                        (js/Buffer.from (str (pr-str registration) "\n") "utf8")))))

(deftest registration-cli-appends-under-the-existing-locked-writer
  (with-history-file
    (fn [file]
      (is (zero? (cli/register-history!)))
      (let [written (fs/readFileSync file)
            suffix (cli/appended-receipt-records! prefix-bytes written)]
        (is (= 1 (count suffix)))
        (is (history/valid-archive-registration? (first suffix)))
        (is (= (:ledger/sha256 history/historical-prefix)
               (cli/sha256 (.subarray written 0 (.-length prefix-bytes)))))
        (is (thrown-with-msg? js/Error #"already registered" (cli/register-history!)))
        (is (.equals written (fs/readFileSync file)))
        (is (not (fs/existsSync (path/join (path/dirname file)
                                         ".receipts.edn.append.lock"))))))))

(deftest held-ledger-validates-new-suffix-before-any-write
  (with-history-file
    (fn [file]
      (let [bad (extending [(nth prefix-records 93)])]
        (fs/writeFileSync file bad)
        (is (thrown-with-msg? js/Error #"invalid receipt envelopes"
                             (cli/register-history!)))
        (is (.equals bad (fs/readFileSync file))))))
  (with-history-file
    (fn [file]
      (let [unterminated (.subarray (extending [registration])
                                   0 (dec (.-length (extending [registration]))))]
        (fs/writeFileSync file unterminated)
        (is (thrown-with-msg? js/Error #"must end with a newline"
                             (cli/register-history!)))
        (is (.equals unterminated (fs/readFileSync file)))))))

(deftest current-evidence-writer-can-extend-registered-history
  (let [existing (first (filter #(= 2 (:evidence/schema %)) prefix-records))
        result (:evidence/result existing)]
    (is (evidence/evidence-receipt? existing))
    (with-history-file
      (fn [file]
        (is (zero? (cli/register-history!)))
        (let [appended (cli/append-evidence-receipt! result)
              bytes (fs/readFileSync file)
              suffix (cli/appended-receipt-records! prefix-bytes bytes)]
          (is (evidence/evidence-receipt? appended))
          (is (= result (:evidence/result appended)))
          (is (= 2 (count suffix)))
          (is (= appended (second suffix)))
          (is (= 139 (:receipt/total (cli/require-valid-receipt-bytes! bytes)))))))))

(deftest malformed-current-evidence-still-fails
  (let [malformed (assoc registration
                         :kind :test-run
                         :origin evidence/evidence-receipt-origin
                         :evidence/result {:result/outcome :passed})]
    (is (thrown-with-msg? js/Error #"invalid evidence receipts"
                         (cli/require-valid-receipt-bytes! (extending [malformed]))))))

(deftest archival-input-does-not-become-promotion-authority
  (let [archival (mapv #(nth prefix-records (dec %)) (keys history/unverified-rows))
        catalog-id {:catalog/path "config/quality-gates.edn"
                    :catalog/sha256 (apply str (repeat 64 "a"))}
        gate {:gate/id :repo/unit :gate/kind :unit :gate/execution :local
              :gate/command ["test"] :gate/source "repo/package.json"}
        catalog {:catalog/version 1
                 :catalog/repositories {"repo" {:repository/path "repo"
                                                :repository/gates [gate]}}}
        result {:gate/id :repo/unit :result/outcome :passed :result/exit 0
                :result/revision revision :result/execution :local
                :result/command ["test"] :result/catalog catalog-id
                :result/source {:source/path "repo/package.json"
                                :source/repository "repo" :source/revision revision}}
        ledger (assoc (immutable-ledger (extending [registration]))
                      :ledger/records (conj archival registration))
        ready? (fn [value] (evidence/promotion-evidence-consistent?
                            catalog catalog-id revision #{:repo/unit} [result] value))]
    (is (evidence/valid-catalog? catalog))
    (is (evidence/valid-result? result))
    (doseq [record (conj archival registration)]
      (is (not (evidence/receipt-attests-result? record result))))
    (is (not (ready? ledger)))
    (let [current (cli/evidence-receipt result "2026-09-12T12:00:00Z" "test" "test")]
      (is (ready? (update ledger :ledger/records conj current))))))
