;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns evidence-test
  (:require [cljs.test :as test :refer [deftest is]]
            [foresight.evidence :as evidence]))

(def valid-catalog
  {:catalog/version 1
   :catalog/repositories
   {"repo"
    {:repository/path "repo"
     :repository/gates
     [{:gate/id :repo/unit
       :gate/kind :unit
       :gate/execution :local
       :gate/command ["tool" "test"]
       :gate/source "repo/README.md"}
      {:gate/id :repo/integration
       :gate/kind :integration
       :gate/execution :local
       :gate/command ["tool" "integration"]
       :gate/source "repo/README.md"}
      {:gate/id :repo/live
       :gate/kind :live-smoke
       :gate/execution :external
       :gate/source "repo/.github/workflows/deploy.yml"
       :gate/reason "Requires the target host"}]}}})

(def test-catalog-identity
  {:catalog/path "config/quality-gates.edn"
   :catalog/sha256 (apply str (repeat 64 "a"))})

(def revision-a (apply str (repeat 40 "1")))

(def revision-b (apply str (repeat 40 "2")))

(defn recorded-result [gate-id outcome revision]
  (let [gate (or (some #(when (= gate-id (:gate/id %)) %)
                       (get-in valid-catalog
                               [:catalog/repositories "repo"
                                :repository/gates]))
                 {:gate/execution :local
                  :gate/command ["tool" "test"]
                  :gate/source "repo/README.md"})]
    (cond-> {:gate/id gate-id
             :result/outcome outcome
             :result/execution (:gate/execution gate)
             :result/catalog test-catalog-identity
             :result/source {:source/path (:gate/source gate)
                             :source/repository "repo"
                             :source/revision revision}
             :result/revision revision}
      (:gate/command gate)
      (assoc :result/command (:gate/command gate))

      (and (= :local (:gate/execution gate))
           (= :passed outcome))
      (assoc :result/exit 0))))

(defn receipt-for [result]
  (let [source (:result/source result)]
    {:ts "2026-08-29T17:22:40Z"
     :kind :test-run
     :repo "."
     :origin evidence/evidence-receipt-origin
     :evidence/schema 2
     :evidence/adapter "nbb/node@test"
     :owner "foresight-evidence-runner"
     :dod "Retain one exact gate result for immutable promotion review"
     :pi "eta-mu"
     :host "test"
     :manifest [(:catalog/path (:result/catalog result))
                (:source/path source)]
     :refs [(str (:source/repository source) "@" (:result/revision result))
            (str (:gate/id result))]
     :evidence/result result}))

(defn immutable-ledger [results]
  {:ledger/identity
   {:ledger/path evidence/receipt-ledger-path
    :ledger/revision (apply str (repeat 40 "c"))
    :ledger/sha256 (apply str (repeat 64 "d"))}
   :ledger/records (mapv receipt-for results)})

(deftest canonical-receipt-envelope-matches-the-v2-contract
  (let [receipt (dissoc (receipt-for
                         (recorded-result :repo/unit :passed revision-a))
                        :repo)]
    (is (evidence/receipt-envelope? receipt))
    (is (false? (evidence/receipt-envelope? (dissoc receipt :origin))))
    (is (false? (evidence/receipt-envelope? 42)))))

(deftest receipt-timestamps-must-identify-a-real-instant
  (doseq [ts ["2026-08-29T17:22:40Z"
              "2026-08-13T12:17:48.462035867Z"
              "2026-01-01T00:00:00+02:00"
              "2024-02-29T23:59:59.5-23:59"
              "2000-02-29T00:00:00Z"
              "0050-01-01T00:00:00Z"
              ;; RFC 3339 permits lowercase t/z time markers.
              "2026-10-13t16:45:00z"
              "2026-10-13t16:45:00.5z"
              "2026-01-01t00:00:00+02:00"]]
    (is (evidence/canonical-receipt-ts? ts) ts))
  (let [receipt (receipt-for (recorded-result :repo/unit :passed revision-a))]
    (is (evidence/receipt-envelope?
         (assoc receipt :ts "2026-10-13t16:45:00z"))
        "lowercase RFC 3339 markers remain a valid envelope timestamp"))
  (doseq [ts ["2026-02-30T12:00:00Z" "2025-02-29T00:00:00Z"
              "1900-02-29T00:00:00Z" "2026-04-31T00:00:00Z"
              "2026-13-01T00:00:00Z" "2026-00-01T00:00:00Z"
              "2026-01-00T00:00:00Z" "2026-01-01T24:00:00Z"
              "2026-01-01T00:60:00Z" "2026-01-01T00:00:60Z"
              "2026-01-01T00:00:00+99:99" "2026-01-01T00:00:00+24:00"
              "2026-01-01T00:00:00+01:60" "2026-01-01T00:00:00-00:00"
              "2026-01-01T00:00:00"
              "2026-01-01 00:00:00Z" "yesterday" "" nil 42]]
    (is (not (evidence/canonical-receipt-ts? ts)) (pr-str ts)))
  (let [receipt (receipt-for (recorded-result :repo/unit :passed revision-a))]
    (is (false? (evidence/receipt-envelope?
                 (assoc receipt :ts "2026-02-30T12:00:00Z"))))
    (is (false? (evidence/receipt-envelope?
                 (assoc receipt :ts "2026-01-01T00:00:00+99:99"))))))

(defn merged-order [records]
  (evidence/merge-ledger-records
   (comp str :n)
   (map (fn [record] {:segment/path (str (:n record))
                      :segment/records [record]})
        records)))

(deftest merged-ledger-sorts-by-instant-not-timestamp-text
  (let [whole {:n 1 :ts "2026-01-01T00:00:00Z"}
        tenth {:n 2 :ts "2026-01-01T00:00:00.1Z"}
        hundredth {:n 3 :ts "2026-01-01T00:00:00.05Z"}
        offset {:n 4 :ts "2026-01-01T00:00:00+02:00"}
        utc {:n 5 :ts "2025-12-31T23:00:00Z"}
        same-instant {:n 6 :ts "2025-12-31T23:00:00.000+00:00"}
        expected [offset utc same-instant whole hundredth tenth]]
    (is (= expected (merged-order [whole tenth hundredth offset utc same-instant])))
    (is (= expected (merged-order (reverse expected))))))

(deftest receipt-timestamps-accept-actual-leap-seconds
  ;; RFC 3339 permits second 60 only at an inserted leap second: 23:59:60 UTC
  ;; at the end of a day IERS announced, in any UTC offset.
  (doseq [ts ["2016-12-31T23:59:60Z" "1972-06-30T23:59:60.5Z"
              "2016-12-31T17:59:60-06:00" "2017-01-01T00:59:60+01:00"
              "2012-06-30T23:59:60.000+00:00"]]
    (is (evidence/canonical-receipt-ts? ts) ts))
  (doseq [ts ["2015-12-31T23:59:60Z" "2016-06-30T23:59:60Z"
              "2016-12-31T23:58:60Z" "2016-12-31T23:59:60+01:00"
              "1971-12-31T23:59:60Z" "2016-12-31T23:59:61Z"]]
    (is (not (evidence/canonical-receipt-ts? ts)) ts))
  (let [before {:n 1 :ts "2016-12-31T23:59:59.9Z"}
        leap {:n 2 :ts "2016-12-31T23:59:60Z"}
        leap-local {:n 3 :ts "2016-12-31T17:59:60.000-06:00"}
        late-leap {:n 4 :ts "2016-12-31T23:59:60.5Z"}
        after {:n 5 :ts "2017-01-01T00:00:00Z"}
        expected [before leap leap-local late-leap after]]
    (is (= (evidence/receipt-ts-sort-key (:ts leap))
           (evidence/receipt-ts-sort-key (:ts leap-local)))
        "the same leap-second instant in another offset sorts equal")
    (is (= expected (merged-order (reverse expected))))
    (is (= expected (merged-order [after late-leap leap-local before leap])))))

(deftest merged-ledger-keeps-historical-timestamps-total
  ;; Historical records predate the canonical-instant rule; merging must not
  ;; reject them, and their order must not depend on segment order.
  (let [legacy {:n 1 :ts "yesterday"}
        untimed {:n 2}
        current {:n 3 :ts "2026-01-01T00:00:00Z"}
        merged (merged-order [current legacy untimed])]
    (is (= #{legacy untimed current} (set merged)))
    (is (= current (last merged)))
    (is (= merged (merged-order [untimed current legacy])))))

(deftest validates-gate-catalogs
  (is (evidence/valid-catalog? valid-catalog))
  (is (= :gate/command
         (:error
          (first
           (evidence/catalog-errors
            (assoc-in valid-catalog
                      [:catalog/repositories "repo" :repository/gates 0 :gate/command]
                      ["tool" ""]))))))
  (is (= :gate/reason
         (:error
          (first
           (evidence/catalog-errors
            (update-in valid-catalog
                       [:catalog/repositories "repo" :repository/gates 2]
                       dissoc :gate/reason)))))))

(deftest rejects-duplicate-gate-identities
  (let [duplicate (assoc-in valid-catalog
                            [:catalog/repositories "other"]
                            {:repository/path "other"
                             :repository/gates
                             [(get-in valid-catalog
                                      [:catalog/repositories "repo"
                                       :repository/gates 0])]})]
    (is (= {:error :gate/id-duplicates :gate/ids [:repo/unit]}
           (last (evidence/catalog-errors duplicate))))))

(deftest malformed-gate-collections-return-structured-errors
  (let [malformed (assoc-in valid-catalog
                            [:catalog/repositories "repo" :repository/gates]
                            42)]
    (is (= :repository/gates
           (:error (first (evidence/catalog-errors malformed)))))
    (is (false? (evidence/valid-catalog? malformed)))
    (is (false? (evidence/promotion-evidence-consistent?
                 malformed test-catalog-identity revision-a
                 #{:repo/unit}
                 [(recorded-result :repo/unit :passed revision-a)]
                 (immutable-ledger
                  [(recorded-result :repo/unit :passed revision-a)]))))))

(deftest malformed-mixed-identities-remain-total
  (let [unit (get-in valid-catalog
                     [:catalog/repositories "repo" :repository/gates 0])
        malformed (assoc-in valid-catalog
                            [:catalog/repositories "repo" :repository/gates]
                            [unit unit
                             (assoc unit :gate/id 42)
                             (assoc unit :gate/id 42)])]
    (is (= {:error :gate/id-duplicates :gate/ids [:repo/unit]}
           (last (evidence/catalog-errors malformed))))
    (is (false? (evidence/valid-catalog? malformed))))
  (let [errors (evidence/catalog-inventory-errors
                {:catalog/repositories {"repo" {} 42 {}}}
                #{})]
    (is (= 2 (count errors)))
    (is (= #{"repo" 42} (set (map :repository errors))))))

(deftest catalog-repositories-must-be-actionable-direct-submodules
  (is (empty? (evidence/catalog-inventory-errors valid-catalog #{"repo"})))
  (is (= [{:error :catalog/repository-not-actionable-submodule
           :repository "repo"}]
         (evidence/catalog-inventory-errors valid-catalog #{"other"}))))

(deftest selects-repositories-and-kinds
  (is (= [:repo/unit]
         (mapv :gate/id
               (evidence/select-gates valid-catalog ["repo"] #{:unit}))))
  (is (= [] (evidence/select-gates valid-catalog ["missing"] #{:unit}))))

(deftest unavailable-and-blocked-never-pass
  (doseq [outcome [:failed :blocked :unavailable]]
    (is (false? (evidence/satisfied? {:result/outcome outcome}))))
  (is (false? (evidence/satisfied? {:result/outcome :not-applicable
                                    :result/reason "No browser surface"})))
  (is (evidence/satisfied? {:result/outcome :not-applicable
                            :result/reason "No browser surface"
                            :result/approved-by "review/42"}))
  (is (false? (evidence/promotion-satisfied?
               {:result/outcome :not-applicable
                :result/reason "No browser surface"
                :result/approved-by "review/42"})))
  (is (evidence/promotion-satisfied? {:result/outcome :passed})))

(deftest validates-result-outcomes-and-not-applicable-approval
  (let [passed (recorded-result :repo/unit :passed revision-a)]
    (is (evidence/valid-result? passed))
    (is (= :result/command
           (:error (first (evidence/result-errors
                           (dissoc passed :result/command))))))
    (is (= :result/catalog
           (:error (first (evidence/result-errors
                           (assoc-in passed
                                     [:result/catalog :catalog/sha256]
                                     "not-a-digest"))))))
    (is (= :result/source-revision
           (:error (first (evidence/result-errors
                           (assoc-in passed
                                     [:result/source :source/revision]
                                     revision-b))))))
    (is (= :result/local-passed-exit
           (:error (first (filter #(= :result/local-passed-exit (:error %))
                                  (evidence/result-errors
                                   (assoc passed :result/exit 7)))))))
    (is (= :result/local-nonpass-exit
           (:error (first (filter #(= :result/local-nonpass-exit (:error %))
                                  (evidence/result-errors
                                   (assoc passed
                                          :result/outcome :failed))))))))
  (is (= :result/outcome
         (:error (first (evidence/result-errors
                         (recorded-result :repo/unit :greenish
                                          revision-a))))))
  (is (= :result/not-applicable-approval
         (:error (first (evidence/result-errors
                         (assoc (recorded-result :repo/e2e
                                                :not-applicable
                                                revision-a)
                                :result/reason "No user surface"))))))
  (doseq [symbolic-revision ["main" "v1.0.0" "abc123"
                             (apply str (repeat 40 "A"))]]
    (let [symbolic (recorded-result :repo/unit :passed symbolic-revision)
          errors (set (map :error (evidence/result-errors symbolic)))]
      (is (false? (evidence/valid-result? symbolic)))
      (is (contains? errors :result/source))
      (is (contains? errors :result/passed-revision))))
  (is (evidence/valid-result?
       (recorded-result :repo/unit :passed (apply str (repeat 64 "3"))))))

(deftest summary-keeps-the-strongest-non-pass-visible
  (is (= {:result/outcome :blocked
          :result/counts {:blocked 1 :passed 1 :unavailable 1}
          :result/satisfied? false}
         (evidence/summarize-results
          [(recorded-result :repo/unit :passed revision-a)
           (recorded-result :repo/e2e :unavailable revision-a)
           (recorded-result :repo/live :blocked revision-a)])))
  (is (= {:result/outcome :unavailable
          :result/counts {}
          :result/satisfied? false}
         (evidence/summarize-results [])))
  (is (= {:result/outcome :failed
          :result/counts {}
          :result/satisfied? false
          :result/errors [{:error :results/type :results 42}]}
         (evidence/summarize-results 42))))

(deftest invalid-result-data-fails-the-summary
  (let [summary (evidence/summarize-results
                 [(recorded-result :repo/unit :unknown revision-a)])]
    (is (= :failed (:result/outcome summary)))
    (is (false? (:result/satisfied? summary)))
    (is (= :result/outcome (get-in summary [:result/errors 0 :error])))))

(deftest malformed-result-values-return-structured-errors
  (is (= [{:error :result/type :result 42}]
         (evidence/result-errors 42)))
  (is (false? (evidence/valid-result? 42)))
  (let [summary (evidence/summarize-results [42])]
    (is (= :failed (:result/outcome summary)))
    (is (= {} (:result/counts summary)))
    (is (= :result/type (get-in summary [:result/errors 0 :error])))))

(deftest promotion-is-closed-over-required-gates
  (let [revision revision-a
        passed [(recorded-result :repo/unit :passed revision)
                (recorded-result :repo/integration :passed revision)]]
    (is (evidence/promotion-evidence-consistent?
         valid-catalog test-catalog-identity revision
         #{:repo/unit :repo/integration} passed
         (immutable-ledger passed)))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity revision
                 #{:repo/unit :repo/e2e} passed
                 (immutable-ledger passed))))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity revision
                 #{:repo/unit}
                 [(recorded-result :repo/unit :unavailable revision)]
                 (immutable-ledger
                  [(recorded-result :repo/unit :unavailable revision)]))))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity revision #{} []
                 (immutable-ledger []))))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity revision 42 []
                 (immutable-ledger []))))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity revision #{} 42
                 (immutable-ledger []))))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity revision
                 #{:repo/unit} [(first passed)] nil)))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity "not-a-git-commit"
                 #{:repo/unit} [(first passed)]
                 (immutable-ledger [(first passed)]))))))

(deftest promotion-requires-one-unambiguous-target-revision
  (let [unit (recorded-result :repo/unit :passed revision-a)
        integration (recorded-result :repo/integration :passed revision-b)]
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity revision-a
                 #{:repo/unit :repo/integration}
                 [unit integration]
                 (immutable-ledger [unit integration]))))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity
                 "" #{:repo/unit} [unit]
                 (immutable-ledger [unit]))))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity
                 revision-a #{:repo/unit} [unit unit]
                 (immutable-ledger [unit unit]))))))

(deftest coverage-cannot-promote-without-immutable-report-evidence
  (let [coverage-gate (assoc (get-in valid-catalog
                                     [:catalog/repositories "repo"
                                      :repository/gates 0])
                             :gate/id :repo/coverage
                             :gate/kind :coverage)
        catalog (assoc-in valid-catalog
                          [:catalog/repositories "repo" :repository/gates]
                          [coverage-gate])
        result (recorded-result :repo/coverage :passed revision-a)]
    (is (evidence/valid-result? result))
    (is (false? (evidence/automatic-promotion-supported? coverage-gate)))
    (is (false? (evidence/promotion-evidence-consistent?
                 catalog test-catalog-identity revision-a
                 #{:repo/coverage} [result]
                 (immutable-ledger [result]))))))

(deftest promotion-results-must-match-the-trusted-catalog-snapshot
  (let [revision revision-a
        result (recorded-result :repo/unit :passed revision)
        consistent? #(evidence/promotion-evidence-consistent?
                      valid-catalog test-catalog-identity revision
                      #{:repo/unit} [%]
                      (immutable-ledger [result]))]
    (is (consistent? result))
    (is (false? (consistent? (assoc result :result/command ["true"]))))
    (is (false? (consistent? (assoc result :result/exit 7))))
    (is (false? (consistent? (assoc result :result/execution :external))))
    (is (false? (consistent? (assoc result
                                    :result/catalog
                                    (assoc test-catalog-identity
                                           :catalog/sha256
                                           (apply str (repeat 64 "b")))))))
    (is (false? (consistent? (assoc-in result
                                       [:result/source :source/path]
                                       "repo/forged.edn"))))
    (is (false? (consistent? (assoc-in result
                                       [:result/source :source/repository]
                                       "other"))))
    (let [failed (assoc (recorded-result :repo/unit :failed revision)
                        :result/exit 7)
          edited-pass (assoc failed :result/outcome :passed :result/exit 0)]
      (is (evidence/valid-result? edited-pass))
      (is (false? (evidence/promotion-evidence-consistent?
                   valid-catalog test-catalog-identity revision
                   #{:repo/unit} [edited-pass]
                   (immutable-ledger [failed])))))))

(deftest promotion-requires-an-exact-immutable-receipt
  (let [result (recorded-result :repo/unit :passed revision-a)
        ledger (immutable-ledger [result])
        receipt (first (:ledger/records ledger))
        legacy-receipt (dissoc receipt :evidence/schema :evidence/adapter)
        not-applicable (-> result
                           (assoc :result/outcome :not-applicable
                                  :result/reason "No user surface"
                                  :result/approved-by "review/42")
                           (dissoc :result/exit))]
    (is (evidence/evidence-receipt? (first (:ledger/records ledger))))
    (is (evidence/evidence-receipt? legacy-receipt))
    (is (false? (evidence/receipt-attests-result? legacy-receipt result)))
    (is (false? (evidence/evidence-receipt?
                 (assoc receipt :evidence/adapter ""))))
    (is (evidence/immutable-receipt-ledger? ledger))
    (is (evidence/promotion-evidence-consistent?
         valid-catalog test-catalog-identity revision-a
         #{:repo/unit} [result]
         (update ledger :ledger/records
                 #(conj % (first %)))))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity revision-a
                 #{:repo/unit} [result]
                 (assoc-in ledger [:ledger/identity :ledger/sha256] "forged"))))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity revision-a
                 #{:repo/unit} [result]
                 (update ledger :ledger/records empty))))
    (is (false? (evidence/promotion-evidence-consistent?
                 valid-catalog test-catalog-identity revision-a
                 #{:repo/unit} [not-applicable]
                 (immutable-ledger [not-applicable]))))))

(deftest receipt-text-keeps-unicode-scalar-values
  (doseq [text ["" "plain" "é" "é" "😀" "�" "\u0000\n\r\t"
                (str "left" (js/String.fromCharCode 0xD800 0xDC00) "right")
                (js/String.fromCharCode 0xDBFF 0xDFFF)]]
    (is (evidence/unicode-scalar-text? text)))
  (doseq [units [[0xD800] [0xDBFF] [0xDC00] [0xDFFF]
                [0xD800 0x41] [0x41 0xDC00]
                [0xD800 0xD800 0xDC00] [0xD800 0xDC00 0xDC00]]]
    (is (not (evidence/unicode-scalar-text?
              (apply str (map #(js/String.fromCharCode %) units))))))
  (is (false? (evidence/unicode-scalar-text? nil))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (if (test/successful? summary) 0 1)))

;; BEGIN receipt-correction-view RED contract
;; Preserved PR28 receipt-correction-view [items verified-targets] regression contract.
;; The RED commit used a test-only absent-API resolver. GREEN calls the now
;; implemented API directly, retaining every assertion. Git, ancestry, UTF-8 and
;; including-LF digest verification belong to the adapter. Digests below are
;; opaque fixture identities, not hashes independently verified by this suite.
;;
;; Error maps use :error with the precise keywords asserted below; additional
;; ordinary-envelope errors may coexist with a correction refusal. Refused
;; entries must not apply even a subset of their fields. Duplicate targets are
;; refused globally, including across correction records/source identities.
;; Successful provenance is the original documentary entry plus
;; :correction/line (the absolute line of the correction record).
(defn correction-view-under-test [items verified-targets]
  (if-let [view (resolve 'foresight.evidence/receipt-correction-view)]
    (view items verified-targets)
    {:receipt/originals (mapv :receipt/record items)
     :receipt/views (mapv :receipt/record items)
     :receipt/corrections [] :receipt/errors [{:error :api-missing}]}))

(def correction-source-revision
  "91a6fc6b27264da4b8c9b8cd7919f153adea0ff2")
(def correction-source-digest (apply str (repeat 64 "a")))

(def correction-original
  (assoc (receipt-for (recorded-result :repo/unit :passed revision-a))
         :manifest ".ημ/receipts.edn,automation:follow-up"
         :refs "root1069,request6044932186"
         :dod [" first objective " "second; objective"]
         :pi :cephalon/character-memory))

(def correction-fields
  {:manifest [".ημ/receipts.edn,automation:follow-up"]
   :refs ["root1069,request6044932186"]
   :dod " first objective ; second; objective"
   :pi "cephalon/character-memory"})

(defn documentary-correction [entries]
  {:ts "2026-10-07T22:09:45.368Z"
   :kind :correction
   :repo "open-hax/foresight"
   :origin "documentary-envelope-correction"
   :owner "test-coordinator"
   :dod "Retain bound envelope interpretation without rewriting history"
   :pi "cephalon/character-memory"
   :host "test"
   :manifest [".ημ/receipts.edn"]
   :refs ["append correction or leave originals; containing repo supplies attribution"]
   :correction/entries entries})

(defn correction-fixture
  ([] (correction-fixture correction-original correction-fields))
  ([original fields]
   (let [entry {:receipt/line 267
                :receipt/origin (:origin original)
                :receipt/source-revision correction-source-revision
                :receipt/source-sha256 correction-source-digest
                :envelope/corrected-fields fields}
         binding (assoc (dissoc entry :envelope/corrected-fields)
                        :binding/verified? true :receipt/record original)]
     {:items [{:receipt/line 267 :receipt/record original}
              {:receipt/line 278 :receipt/record (documentary-correction [entry])}]
      :targets {[correction-source-revision 267 correction-source-digest] binding}
      :entry entry})))

(def correction-binding-key
  [correction-source-revision 267 correction-source-digest])

(defn fixture-entry [fixture entry]
  (assoc-in fixture [:items 1 :receipt/record :correction/entries] [entry]))

(defn expect-correction-success! [scenario {:keys [items targets]} views provenance]
  (is (= {:receipt/originals (mapv :receipt/record items)
          :receipt/views views
          :receipt/corrections provenance
          :receipt/errors []}
         (correction-view-under-test items targets))
      scenario))

(defn expect-correction-refusal! [scenario expected-code {:keys [items targets]}]
  (let [result (correction-view-under-test items targets)]
    (is (contains? (set (map :error (:receipt/errors result))) expected-code)
        (str scenario " must report " expected-code))
    (is (= {:receipt/views (mapv :receipt/record items)
            :receipt/corrections []}
           (select-keys result [:receipt/views :receipt/corrections]))
        (str scenario " must retain every original view and admit no correction"))))

(deftest receipt-correction-valid-ordinary-records-remain-exact
  (let [ordinary (dissoc (receipt-for (recorded-result :repo/unit :passed revision-a))
                         :repo)
        ;; Kind alone does not activate documentary grammar.
        kind-only (assoc ordinary :kind :correction)
        items [{:receipt/line 17 :receipt/record ordinary}
               {:receipt/line 259 :receipt/record kind-only}]]
    (expect-correction-success! "valid ordinary maps need no correction or repo field"
                                {:items items :targets {}}
                                [ordinary kind-only] [])))

(deftest receipt-correction-empty-input-has-empty-result
  (expect-correction-success! "empty ledger selection"
                              {:items [] :targets {}} [] []))

(deftest receipt-correction-documentary-syntax-keeps-originals-and-evidence
  (let [{:keys [items entry] :as fixture} (correction-fixture)
        corrected (assoc correction-original
                         :manifest [".ημ/receipts.edn,automation:follow-up"]
                         :refs ["root1069,request6044932186"]
                         :dod " first objective ; second; objective"
                         :pi "cephalon/character-memory")]
    (expect-correction-success!
     "four exact interpretations preserve ts/owner/origin/kind and evidence/result"
     fixture [corrected (:receipt/record (second items))]
     [(assoc entry :correction/line 278)])))

(deftest receipt-correction-converts-each-permitted-invalid-field-independently
  (let [ordinary (receipt-for (recorded-result :repo/unit :passed revision-a))]
    (doseq [[field original-value replacement]
            [[:manifest "path-a,path-b" ["path-a,path-b"]]
             [:refs "ref-a,ref-b" ["ref-a,ref-b"]]
             [:dod ["alpha " " beta"] "alpha ;  beta"]
             [:pi :cephalon/character-memory "cephalon/character-memory"]
             [:pi :eta-mu "eta-mu"]]]
      (let [original (assoc ordinary field original-value)
            {:keys [items entry] :as fixture}
            (correction-fixture original {field replacement})]
        (expect-correction-success!
         (str "exact conversion of " field " from " (pr-str original-value))
         fixture [(assoc original field replacement)
                  (:receipt/record (second items))]
         [(assoc entry :correction/line 278)])))))

(deftest receipt-correction-supports-full-sha256-source-identity
  (let [{:keys [entry targets] :as fixture} (correction-fixture)
        revision (apply str (repeat 64 "b"))
        entry (assoc entry :receipt/source-revision revision)
        binding (assoc (get targets correction-binding-key)
                       :receipt/source-revision revision)
        fixture (assoc (fixture-entry fixture entry)
                       :targets {[revision 267 correction-source-digest] binding})]
    (expect-correction-success!
     "full SHA256 commit identity is matched, not verified by the pure law"
     fixture [(merge correction-original correction-fields)
              (:receipt/record (second (:items fixture)))]
     [(assoc entry :correction/line 278)])))

(deftest receipt-correction-semantic-evidence-remains-for-existing-outer-validator
  (let [original (assoc-in correction-original [:evidence/result :result/exit] 7)
        {:keys [items entry] :as fixture} (correction-fixture original correction-fields)]
    (expect-correction-success!
     "envelope interpretation retains a semantically invalid result unchanged"
     fixture [(merge original correction-fields) (:receipt/record (second items))]
     [(assoc entry :correction/line 278)])))

(deftest receipt-correction-key-presence-requires-correction-kind
  (doseq [entries [nil []]]
    (expect-correction-refusal!
     (str "entries key present on non-correction kind, value " (pr-str entries))
     :receipt-correction/kind
     {:items [{:receipt/line 278
               :receipt/record (assoc (documentary-correction entries)
                                      :kind :observation)}]
      :targets {}})))

(deftest receipt-correction-rejects-empty-or-nonvector-entries
  (let [fixture (correction-fixture)]
    (doseq [entries [nil [] {} (list (:entry fixture)) "entries"]]
      (expect-correction-refusal!
       (str "entries must be a nonempty vector: " (pr-str entries))
       :receipt-correction/entries
       (assoc-in fixture [:items 1 :receipt/record :correction/entries] entries)))))

(deftest receipt-correction-rejects-nonmap-entry
  (expect-correction-refusal! "entry is not a map" :receipt-correction/entry-type
                              (fixture-entry (correction-fixture) 42)))

(deftest receipt-correction-requires-exact-documentary-entry-fields
  (let [{:keys [entry] :as fixture} (correction-fixture)]
    (doseq [field (keys entry)]
      (expect-correction-refusal! (str "missing entry key " field)
                                  :receipt-correction/entry-fields
                                  (fixture-entry fixture (dissoc entry field))))
    (expect-correction-refusal! "extra entry key is not documentary grammar"
                                :receipt-correction/entry-fields
                                (fixture-entry fixture (assoc entry :extra true)))))

(deftest receipt-correction-requires-positive-absolute-target-line
  (let [{:keys [entry] :as fixture} (correction-fixture)]
    (doseq [line [0 -1 267.5 "267" nil]]
      (expect-correction-refusal! (str "invalid target ordinal " (pr-str line))
                                  :receipt-correction/target-line
                                  (fixture-entry fixture (assoc entry :receipt/line line))))))

(deftest receipt-correction-requires-nonblank-entry-origin
  (let [{:keys [entry] :as fixture} (correction-fixture)]
    (doseq [origin [nil "" "  " :origin]]
      (expect-correction-refusal! (str "invalid entry origin " (pr-str origin))
                                  :receipt-correction/entry-origin
                                  (fixture-entry fixture (assoc entry :receipt/origin origin))))))

(deftest receipt-correction-requires-full-lowercase-source-identities
  (let [{:keys [entry] :as fixture} (correction-fixture)]
    (doseq [revision ["main" "91a6fc6" (apply str (repeat 40 "A")) nil]]
      (expect-correction-refusal! (str "invalid source revision " (pr-str revision))
                                  :receipt-correction/source-revision
                                  (fixture-entry fixture
                                                 (assoc entry :receipt/source-revision revision))))
    (doseq [digest ["a" (apply str (repeat 64 "A")) nil]]
      (expect-correction-refusal! (str "invalid source SHA256 " (pr-str digest))
                                  :receipt-correction/source-sha256
                                  (fixture-entry fixture
                                                 (assoc entry :receipt/source-sha256 digest))))))

(deftest receipt-correction-rejects-missing-binding-and-wrong-lookup-identity
  (let [{:keys [entry] :as fixture} (correction-fixture)]
    (expect-correction-refusal! "no verified fact for the exact lookup triple"
                                :receipt-correction/binding-missing
                                (assoc fixture :targets {}))
    (doseq [[field value] [[:receipt/source-revision revision-b]
                          [:receipt/source-sha256 (apply str (repeat 64 "b"))]]]
      (expect-correction-refusal! (str "lookup uses a different " field)
                                  :receipt-correction/binding-missing
                                  (fixture-entry fixture (assoc entry field value))))))

(deftest receipt-correction-rejects-malformed-binding-container-or-value
  (let [fixture (correction-fixture)]
    (doseq [targets [nil [] "facts"]]
      (expect-correction-refusal! (str "facts table is not a map: " (pr-str targets))
                                  :receipt-correction/bindings-type
                                  (assoc fixture :targets targets)))
    (expect-correction-refusal! "bound fact is not a map"
                                :receipt-correction/binding-type
                                (assoc fixture :targets {correction-binding-key 42}))))

(deftest receipt-correction-requires-complete-binding-facts
  (let [{:keys [targets] :as fixture} (correction-fixture)]
    (doseq [field [:receipt/line :receipt/origin :receipt/source-revision
                  :receipt/source-sha256 :receipt/record]]
      (expect-correction-refusal! (str "bound fact missing " field)
                                  :receipt-correction/binding-fields
                                  (assoc-in fixture [:targets correction-binding-key]
                                            (dissoc (get targets correction-binding-key) field))))))

(deftest receipt-correction-requires-explicit-true-verification-fact
  (let [fixture (correction-fixture)]
    (doseq [verified [false nil 1 "true"]]
      (expect-correction-refusal! (str "binding is not explicitly verified: " (pr-str verified))
                                  :receipt-correction/binding-unverified
                                  (assoc-in fixture
                                            [:targets correction-binding-key :binding/verified?]
                                            verified)))))

(deftest receipt-correction-matches-binding-line-origin-revision-and-hash
  (let [fixture (correction-fixture)]
    (doseq [[field value code]
            [[:receipt/line 268 :receipt-correction/line-mismatch]
             [:receipt/origin "other-origin" :receipt-correction/origin-mismatch]
             [:receipt/source-revision revision-b :receipt-correction/revision-mismatch]
             [:receipt/source-sha256 (apply str (repeat 64 "b"))
              :receipt-correction/hash-mismatch]]]
      (expect-correction-refusal! (str "bound fact contradicts entry " field) code
                                  (assoc-in fixture [:targets correction-binding-key field] value)))))

(deftest receipt-correction-matches-entry-origin-to-prior-original
  (let [{:keys [entry] :as fixture} (correction-fixture)]
    (expect-correction-refusal! "entry origin differs from prior map and bound fact"
                                :receipt-correction/origin-mismatch
                                (fixture-entry fixture
                                               (assoc entry :receipt/origin "other-origin")))))

(deftest receipt-correction-matches-bound-record-to-prior-original-map
  (let [fixture (correction-fixture)]
    (expect-correction-refusal!
     "bound original differs even in a field not being corrected"
     :receipt-correction/record-mismatch
     (assoc-in fixture [:targets correction-binding-key :receipt/record :owner] "other-owner"))
    (expect-correction-refusal!
     "selected prior original differs from the verified original"
     :receipt-correction/record-mismatch
     (assoc-in fixture [:items 0 :receipt/record :evidence/result :result/exit] 7))))

(deftest receipt-correction-rejects-target-not-present-at-absolute-line
  (expect-correction-refusal! "prior item was supplied under another absolute line"
                              :receipt-correction/target-missing
                              (assoc-in (correction-fixture) [:items 0 :receipt/line] 266)))

(deftest receipt-correction-rejects-self-and-forward-targets
  (let [{:keys [entry items targets]} (correction-fixture)]
    (doseq [line [278 279]]
      (let [entry (assoc entry :receipt/line line)
            correction (documentary-correction [entry])
            binding (assoc (get targets correction-binding-key) :receipt/line line)
            selected (if (= line 278)
                       [{:receipt/line 278 :receipt/record correction}]
                       [(second (assoc items 1 {:receipt/line 278 :receipt/record correction}))
                        {:receipt/line 279 :receipt/record correction-original}])]
        (expect-correction-refusal!
         (str "target ordinal " line " is not before correction line278")
         :receipt-correction/target-not-prior
         {:items selected
          :targets {[correction-source-revision line correction-source-digest] binding}})))))

(deftest receipt-correction-rejects-duplicate-and-conflicting-targets-in-one-record
  (let [{:keys [entry] :as fixture} (correction-fixture)]
    (doseq [[scenario second-entry]
            [["identical duplicate target" entry]
             ["conflicting replacement for the same target"
              (assoc-in entry [:envelope/corrected-fields :pi] "changed")]]]
      (expect-correction-refusal!
       scenario :receipt-correction/duplicate-target
       (assoc-in fixture [:items 1 :receipt/record :correction/entries]
                 [entry second-entry])))))

(deftest receipt-correction-rejects-duplicate-targets-globally
  (let [{:keys [entry items targets]} (correction-fixture)]
    (doseq [[scenario second-entry second-targets]
            [["same target in a later correction record" entry targets]
             ["same absolute target under a different verified source identity"
              (assoc entry :receipt/source-revision revision-b)
              (assoc targets [revision-b 267 correction-source-digest]
                     (assoc (get targets correction-binding-key)
                            :receipt/source-revision revision-b))]]]
      (expect-correction-refusal!
       scenario :receipt-correction/duplicate-target
       {:items (conj items {:receipt/line 279
                            :receipt/record (documentary-correction [second-entry])})
        :targets second-targets}))))

(deftest receipt-correction-requires-nonempty-corrected-field-map
  (let [{:keys [entry] :as fixture} (correction-fixture)]
    (doseq [fields [nil {} [] "fields"]]
      (expect-correction-refusal! (str "invalid corrected-fields " (pr-str fields))
                                  :receipt-correction/corrected-fields
                                  (fixture-entry fixture
                                                 (assoc entry :envelope/corrected-fields fields))))))

(deftest receipt-correction-rejects-all-unsupported-metadata-and-semantic-fields
  (let [{:keys [entry] :as fixture} (correction-fixture)]
    (doseq [[field value]
            [[:ts "later"] [:owner "new-owner"] [:origin "new-origin"]
             [:kind :decision] [:host "other-host"] [:repo "other-repo"]
             [:evidence/schema 2] [:evidence/adapter "other-adapter"]
             [:evidence/result {:result/outcome :passed}]
             [:result/command ["true"]] [:result/outcome :passed]
             [:result/revision revision-b] [:unknown "value"]]]
      (expect-correction-refusal!
       (str "unsupported corrected field " field)
       :receipt-correction/unsupported-fields
       (fixture-entry fixture
                      (assoc-in entry [:envelope/corrected-fields field] value))))))

(deftest receipt-correction-rejects-no-op-and-replacement-of-valid-metadata
  (let [ordinary (receipt-for (recorded-result :repo/unit :passed revision-a))]
    (doseq [field [:manifest :refs :dod :pi]]
      (expect-correction-refusal!
       (str "already valid metadata cannot be corrected: " field)
       :receipt-correction/field-already-valid
       (correction-fixture ordinary {field (get ordinary field)})))
    (expect-correction-refusal!
     "a different replacement cannot rewrite an already valid DoD"
     :receipt-correction/field-already-valid
     (correction-fixture ordinary {:dod "new objective"}))))

(deftest receipt-correction-rejects-uninterpretable-original-field-types
  (let [ordinary (receipt-for (recorded-result :repo/unit :passed revision-a))]
    (doseq [[field value replacement]
            [[:manifest " " [" "]] [:refs "" [""]]
             [:manifest nil ["invented"]] [:refs :reference ["reference"]]
             [:dod [] ""] [:dod ["ok" " "] "ok;  "]
             [:dod ["ok" 42] "ok; 42"] [:dod (list "a" "b") "a; b"]
             [:pi nil "invented"] [:pi 42 "42"]]]
      (expect-correction-refusal!
       (str "unsupported original " field " value " (pr-str value))
       :receipt-correction/original-field
       (correction-fixture (assoc ordinary field value) {field replacement})))))

(deftest receipt-correction-rejects-inexact-field-interpretations
  (doseq [[field replacement]
          [[:manifest [".ημ/receipts.edn" "automation:follow-up"]]
           [:refs ["root1069" "request6044932186"]]
           [:manifest ".ημ/receipts.edn,automation:follow-up"]
           [:refs ["root1069,request6044932186" 42]]
           [:dod " first objective , second; objective"]
           [:dod "first objective; second; objective"]
           [:dod [" first objective " "second; objective"]]
           [:pi ":cephalon/character-memory"]
           [:pi "character-memory"] [:pi :cephalon/character-memory]]]
    (expect-correction-refusal!
     (str "inexact replacement for " field ": " (pr-str replacement))
     :receipt-correction/field-interpretation
     (correction-fixture correction-original {field replacement}))))

(deftest receipt-correction-invalid-ordinary-views-remain-errors
  (doseq [[scenario original]
          [["uncorrected malformed ordinary map" correction-original]
           ["ordinary map missing required origin" (dissoc correction-original :origin)]
           ["ordinary scalar" 42]]]
    (expect-correction-refusal!
     scenario :receipt/invalid-envelope
     {:items [{:receipt/line 267 :receipt/record original}] :targets {}})))

(deftest receipt-correction-partial-interpretation-does-not-hide-remaining-invalid-fields
  (let [{:keys [items entry targets]}
        (correction-fixture correction-original
                            {:manifest [".ημ/receipts.edn,automation:follow-up"]})
        result (correction-view-under-test items targets)]
    (is (contains? (set (map :error (:receipt/errors result))) :receipt/invalid-envelope)
        "refs/DoD/Pi remain invalid after a permitted manifest interpretation")
    (is (= {:receipt/originals (mapv :receipt/record items)
            :receipt/views [(assoc correction-original
                                   :manifest [".ημ/receipts.edn,automation:follow-up"])
                            (:receipt/record (second items))]
            :receipt/corrections [(assoc entry :correction/line 278)]}
           (select-keys result [:receipt/originals :receipt/views :receipt/corrections]))
        "a derived partial view is retained with its provenance and explicit envelope error")))

(deftest receipt-correction-record-itself-must-have-a-strict-envelope
  (expect-correction-refusal!
   "correction with invalid own envelope cannot admit target interpretations"
   :receipt/invalid-envelope
   (assoc-in (correction-fixture) [:items 1 :receipt/record :owner] "")))
;; END receipt-correction-view RED contract

(def segmented-original-id (apply str (repeat 64 "c")))

(defn segmented-fixture
  ([original fields] (segmented-fixture original fields true))
  ([original fields qualified?]
   (let [entry (cond-> {:receipt/line 267
                       :receipt/origin (:origin original)
                       :receipt/source-revision correction-source-revision
                       :receipt/source-sha256 correction-source-digest
                       :envelope/corrected-fields fields}
                 qualified? (assoc :receipt/source-path evidence/receipt-ledger-path
                                   :receipt/identity segmented-original-id))
         binding (assoc (dissoc entry :envelope/corrected-fields)
                        :receipt/source-path evidence/receipt-ledger-path
                        :receipt/identity segmented-original-id
                        :receipt/record original :binding/verified? true)]
     {:items [{:receipt/identity segmented-original-id :receipt/record original}
              {:receipt/identity (apply str (repeat 64 "d"))
               :receipt/record (documentary-correction [entry])}]
      :targets {[correction-source-revision evidence/receipt-ledger-path
                 267 correction-source-digest] binding}
      :entry entry})))

(defn segmented-view [{:keys [items targets]}]
  (if-let [view (resolve 'foresight.evidence/segmented-receipt-correction-view)]
    (view items targets)
    {:receipt/originals (mapv :receipt/record items)
     :receipt/views (mapv :receipt/record items)
     :receipt/corrections [] :receipt/errors [{:error :api-missing}]}))

(defn ordinary-kind-original []
  (-> (receipt-for (recorded-result :repo/unit :passed revision-a))
      (dissoc :evidence/schema :evidence/adapter :evidence/result)
      (assoc :origin "ordinary-documentary-record" :kind "test-run")))

(deftest segmented-corrections-use-original-identity-without-display-order
  (doseq [qualified? [false true]]
    (let [fixture (segmented-fixture correction-original correction-fields qualified?)
          forward-display (update fixture :items #(vec (reverse %)))
          result (segmented-view forward-display)]
      (is (= [] (:receipt/errors result)))
      (is (= (mapv :receipt/record (:items forward-display))
             (:receipt/originals result)))
      (is (= (merge correction-original correction-fields)
             (second (:receipt/views result))))
      (is (= segmented-original-id
             (:receipt/identity (first (:receipt/corrections result))))))))

(deftest segmented-kind-extension-is-bound-and-exact
  (let [original (ordinary-kind-original)
        fixture (segmented-fixture original {:kind :test-run})
        result (segmented-view fixture)]
    (is (false? (evidence/receipt-envelope? original)))
    (is (= [] (:receipt/errors result)))
    (is (= (assoc original :kind :test-run) (first (:receipt/views result))))
    (is (= original (first (:receipt/originals result))))
    (doseq [bad [(assoc fixture :targets {})
                 (segmented-fixture original {:kind :test-run} false)
                 (segmented-fixture original {:kind :observation})
                 (segmented-fixture (assoc original :kind "observation")
                                    {:kind :observation})
                 (segmented-fixture (assoc original :kind :test-run)
                                    {:kind :test-run})
                 (segmented-fixture original {:kind :test-run :owner "rewrite"})]]
      (let [refused (segmented-view bad)]
        (is (seq (:receipt/errors refused)))
        (is (= (mapv :receipt/record (:items bad)) (:receipt/views refused)))
        (is (= [] (:receipt/corrections refused)))))))

(deftest kind-correction-excludes-evidence-event-and-correction-markers
  (let [ordinary (ordinary-kind-original)]
    (doseq [extra [{:origin evidence/evidence-receipt-origin}
                   {:evidence/anything nil}
                   {:event/type nil} {:event/type :unknown}
                   {:event/type :receipt-river/receipt-recorded :event/id "evt"}
                   {:envelope/corrected-fields nil}
                   {:envelope/corrected-fields {:kind :test-run} :extra true}
                   {:correction/entries nil}]]
      (is (seq (:receipt/errors
                (segmented-view (segmented-fixture (merge ordinary extra)
                                                   {:kind :test-run}))))
          (pr-str extra)))
    (doseq [extra [{} {:event/id nil} {:event/id "ordinary data"}
                   {:evidence-like/result :data}]]
      (is (= [] (:receipt/errors
                  (segmented-view (segmented-fixture (merge ordinary extra)
                                                     {:kind :test-run}))))))
    (doseq [[symbol key] [['event-envelope? :event/type]
                          ['correction-entry? :envelope/corrected-fields]
                          ['correction-document? :correction/entries]]]
      (if-let [predicate (resolve (cljs.core/symbol (str "foresight.evidence/" symbol)))]
        (do (is (false? (predicate {:event/id "plain"})))
            (is (false? (predicate 42)))
            (is (true? (predicate {key nil :additional true}))))
        (is false (str "missing pure predicate " symbol))))))

(deftest segmented-correction-binding-and-entry-boundaries
  (let [fixture (segmented-fixture (ordinary-kind-original) {:kind :test-run})
        key (first (keys (:targets fixture)))]
    (doseq [[field bad] [[:binding/verified? false]
                         [:receipt/identity (apply str (repeat 64 "e"))]
                         [:receipt/record {}]
                         [:receipt/source-path ".ημ/receipts/other.edn"]
                         [:receipt/line 1] [:receipt/origin "other"]
                         [:receipt/source-revision revision-b]
                         [:receipt/source-sha256 (apply str (repeat 64 "e"))]]]
      (is (seq (:receipt/errors
                (segmented-view (assoc-in fixture [:targets key field] bad))))
          (str field)))
    (doseq [entry [(assoc (:entry fixture) :unexpected true)
                   (dissoc (:entry fixture) :receipt/identity)
                   (assoc (:entry fixture) :envelope/corrected-fields {})
                   (assoc (:entry fixture) :receipt/line 0) 42]]
      (is (seq (:receipt/errors
                (segmented-view (assoc-in fixture
                                          [:items 1 :receipt/record :correction/entries]
                                          [entry])))))))
  (let [fixture (segmented-fixture (ordinary-kind-original) {:kind :test-run})
        duplicate (update fixture :items conj
                          {:receipt/identity (apply str (repeat 64 "e"))
                           :receipt/record (assoc (documentary-correction [(:entry fixture)])
                                                 :origin "second-correction")})
        result (segmented-view duplicate)]
    (is (some #(= :receipt-correction/duplicate-target (:error %))
              (:receipt/errors result)))
    (is (= [] (:receipt/corrections result)))))

(deftest historical-import-exceptions-bind-only-two-reviewed-originals
  (let [approved [["1c0ac43b287497ceac38d792feede62f5f05c2bdf12099570d06a767f64e3914"
                   "8c32195ba96bb3f406931eb4f9829c13bb76313a56aca34dc35de513e85861f2"]
                  ["545730a0e14d1288b7796717ea14924657e0ad9156a86d0bd7589fb39261d75a"
                   "03fef2783af6afe1535245b6696976bf9731ab97726cf7e4eb03f7b8ff20dfe4"]]
        allowed? (resolve 'foresight.evidence/historical-receipt-import?)]
    (is (some? allowed?))
    (when allowed?
      (doseq [[digest id] approved]
        (let [binding {:binding/verified? true :receipt/source-sha256 digest
                       :receipt/identity id}]
          (is (true? (allowed? binding)))
          (doseq [other [(dissoc binding :binding/verified?)
                         (assoc binding :binding/verified? false)
                         (assoc binding :receipt/identity "new-record")
                         (assoc binding :receipt/source-sha256 "different-bytes")]]
            (is (false? (allowed? other)))))))))

(test/run-tests 'evidence-test)
