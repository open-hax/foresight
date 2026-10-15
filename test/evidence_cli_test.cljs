;; SPDX-License-Identifier: GPL-3.0-or-later
(ns evidence-cli-test
  (:require [cljs.test :as test :refer [deftest is]]
            [cljs.reader]
            [clojure.string :as str]
            [evidence :as cli]
            [foresight.evidence :as law]
            [workspace :as workspace]
            ["child_process" :as child-process]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]))

(def test-catalog-identity
  {:catalog/path "config/quality-gates.edn"
   :catalog/sha256 (apply str (repeat 64 "a"))})

(def child-revision (apply str (repeat 40 "1")))

(def sha256-child-revision (apply str (repeat 64 "3")))

(def reviewed-root-revision (apply str (repeat 40 "c")))

(def trusted-base-revision (apply str (repeat 40 "e")))

(def blob-revision (apply str (repeat 40 "b")))

(defn regular-blob-entry [repository-path]
  (str "100644 blob " blob-revision "\t" repository-path "\u0000"))

(def local-gate
  {:gate/id :repo/unit
   :gate/kind :unit
   :gate/execution :local
   :gate/command ["test" "--exact"]
   :gate/source "repo/package.json"})

(def passed-result
  {:gate/id :repo/unit
   :result/outcome :passed
   :result/exit 0
   :result/revision child-revision
   :result/execution :local
   :result/command ["test" "--exact"]
   :result/catalog test-catalog-identity
   :result/source {:source/path "repo/package.json"
                   :source/repository "repo"
                   :source/revision child-revision}})

(defn with-receipt-fixture [run!]
  (let [fixture (fs/mkdtempSync
                 (path/join (os/tmpdir) "foresight-receipt-append-"))
        directory (path/join fixture ".ημ")
        segments (path/join directory "receipts")
        file (path/join directory "receipts.edn")]
    (fs/mkdirSync directory)
    (fs/mkdirSync segments)
    (try
      (run! {:fixture fixture :directory directory :segments segments
             :file file})
      (finally
        (fs/rmSync fixture #js {:recursive true :force true})))))

(defn segment-names [segments]
  (vec (sort (filter #(str/ends-with? % ".edn") (fs/readdirSync segments)))))

(defn segment-records [segments]
  (vec (mapcat #(cli/read-receipt-records!
                 (fs/readFileSync (path/join segments %) "utf8"))
               (segment-names segments))))

(defn lock-files [segments]
  (vec (filter #(str/ends-with? % ".append.lock") (fs/readdirSync segments))))

(defn empty-committed-ledger [_revision]
  {:ledger/identity {:ledger/path law/receipt-ledger-path
                     :ledger/revision reviewed-root-revision
                     :ledger/sha256 (cli/sha256 "")}
   :ledger/bytes (js/Buffer.alloc 0)
   :ledger/records []})

(def one-gate-catalog
  {:catalog/repositories
   {"repo" {:repository/path "repo"
            :repository/gates [local-gate]}}})

(def one-gate-repositories
  {"repo" {:path "repo"
           :absolute "/repo"
           :revision child-revision}})

(defn run-one-gate!
  "Runs `local-gate` from `one-gate-catalog` through `run-selected-gates!`
  against the working legacy ledger `:file`, with HEAD, the committed ledger
  and the gate's repository stubbed. Optional overrides: `:segment` (the
  reserved segment path), `:gate!` (a side effect run by the gate before it
  returns `passed-result`), `:run-gate` (replaces the whole gate), `:git-capture`,
  `:committed` (revision -> committed ledger), `:segments` (revision ->
  committed segment entries), `:repositories` and `:validate-working`."
  [{:keys [file segment gate! run-gate git-capture committed segments
           repositories validate-working]}]
  (let [new-segment-file cli/new-receipt-segment-file
        validate-working-ledger cli/validate-working-receipt-ledger!]
    (with-redefs [cli/receipt-file file
                  cli/new-receipt-segment-file (if segment
                                                 (fn [] segment)
                                                 new-segment-file)
                  cli/git-capture! (or git-capture
                                       (fn [_] reviewed-root-revision))
                  cli/read-immutable-receipt-ledger! (or committed
                                                         empty-committed-ledger)
                  cli/read-immutable-receipt-segment-entries! (or segments (fn [_] []))
                  cli/validate-working-receipt-ledger! (or validate-working
                                                           validate-working-ledger)
                  cli/require-repositories! (fn [& _]
                                              (or repositories
                                                  one-gate-repositories))
                  cli/run-gate! (or run-gate
                                    (fn [& _]
                                      (when gate! (gate!))
                                      passed-result))]
      (cli/run-selected-gates!
       one-gate-catalog test-catalog-identity
       {:only #{"repo"} :kinds #{:unit}}))))

(defn run-semantic-gate-with! [file segment gate!]
  (run-one-gate! {:file file :segment segment :gate! gate!}))

(deftest secure-append-creates-and-serializes-regular-receipts
  (with-receipt-fixture
    (fn [{:keys [file]}]
      (let [first-record {:kind :observation :note "first"}
            second-record {:kind :test-run :tests ["pass"]}]
        (is (= first-record (cli/append-edn-line! file first-record)))
        (is (= (str (pr-str first-record) "\n")
               (fs/readFileSync file "utf8")))
        (is (= second-record (cli/append-edn-line! file second-record)))
        (is (= (str (pr-str first-record) "\n"
                    (pr-str second-record) "\n")
               (fs/readFileSync file "utf8")))
        (is (.isFile (fs/lstatSync file)))
        (is (= 1 (.-nlink (fs/lstatSync file))))))))

(deftest secure-append-repairs-a-missing-terminal-newline
  (with-receipt-fixture
    (fn [{:keys [file]}]
      (let [first-record {:kind :observation :note "unterminated"}
            second-record {:kind :test-run :note "bounded"}]
        (fs/writeFileSync file (pr-str first-record) "utf8")
        (cli/append-edn-line! file second-record)
        (is (= (str (pr-str first-record) "\n"
                    (pr-str second-record) "\n")
               (fs/readFileSync file "utf8")))))))

(deftest secure-append-rejects-a-final-symlink
  (with-receipt-fixture
    (fn [{:keys [fixture file]}]
      (let [outside (path/join fixture "outside.edn")]
        (fs/writeFileSync outside "{:outside true}\n" "utf8")
        (fs/symlinkSync outside file "file")
        (is (thrown-with-msg?
             js/Error #"Receipt River file must not be a symbolic link"
             (cli/append-edn-line! file {:inside true})))
        (is (= "{:outside true}\n" (fs/readFileSync outside "utf8")))))))

(deftest secure-append-rejects-a-parent-symlink
  (let [fixture (fs/mkdtempSync
                 (path/join (os/tmpdir) "foresight-receipt-parent-"))
        actual (path/join fixture "actual")
        linked (path/join fixture ".ημ")
        file (path/join linked "receipts.edn")]
    (fs/mkdirSync actual)
    (fs/symlinkSync actual linked "dir")
    (try
      (is (thrown-with-msg?
           js/Error #"parent directory must not be a symbolic link"
           (cli/append-edn-line! file {:unsafe true})))
      (is (not (fs/existsSync (path/join actual "receipts.edn"))))
      (finally
        (fs/rmSync fixture #js {:recursive true :force true})))))

(deftest secure-append-fails-closed-when-the-writer-lock-exists
  (with-receipt-fixture
    (fn [{:keys [directory file]}]
      (let [lock-file (path/join directory ".receipts.edn.append.lock")]
        (fs/writeFileSync lock-file "held-by-another-writer\n"
                          #js {:encoding "utf8" :flag "wx"})
        (is (thrown-with-msg?
             js/Error #"another writer holds the append lock"
             (cli/append-edn-line! file {:contended true})))
        (is (not (fs/existsSync file)))
        (is (= "held-by-another-writer\n"
               (fs/readFileSync lock-file "utf8")))))))

(deftest secure-append-serializes-a-reentrant-writer
  (with-receipt-fixture
    (fn [{:keys [directory file]}]
      (let [contender-error (atom nil)
            attempted? (atom false)
            winning-record {:writer :first}
            losing-record {:writer :second}]
        (binding [cli/*secure-append-phase-hook*
                  (fn [phase _context]
                    (when (and (= :before-write phase) (not @attempted?))
                      (reset! attempted? true)
                      (try
                        (cli/append-edn-line! file losing-record)
                        (catch :default error
                          (reset! contender-error error)))))]
          (is (= winning-record
                 (cli/append-edn-line! file winning-record))))
        (is @attempted?)
        (is (instance? js/Error @contender-error))
        (is (re-find #"another writer holds the append lock"
                     (.-message @contender-error)))
        (is (= (str (pr-str winning-record) "\n")
               (fs/readFileSync file "utf8")))
        (is (not (fs/existsSync
                  (path/join directory ".receipts.edn.append.lock"))))))))

(deftest secure-append-lock-excludes-a-separate-process
  (with-receipt-fixture
    (fn [{:keys [directory file]}]
      (let [contender (atom nil)
            attempted? (atom false)
            lock-file (path/join directory ".receipts.edn.append.lock")
            script (str
                    "const fs=require('fs');"
                    "const c=fs.constants;"
                    "try {"
                    "fs.openSync(process.argv[1],"
                    "c.O_WRONLY|c.O_CREAT|c.O_EXCL|c.O_NOFOLLOW,0o600);"
                    "process.exit(7);"
                    "} catch (error) {"
                    "process.exit(error.code==='EEXIST'?0:8);"
                    "}")]
        (binding [cli/*secure-append-phase-hook*
                  (fn [phase _context]
                    (when (and (= :before-write phase) (not @attempted?))
                      (reset! attempted? true)
                      (reset! contender
                              (child-process/spawnSync
                               "node" #js ["-e" script lock-file]
                               #js {:encoding "utf8" :shell false}))))]
          (is (= {:writer :parent}
                 (cli/append-edn-line! file {:writer :parent}))))
        (is @attempted?)
        (is (= 0 (.-status @contender)))
        (is (= (str (pr-str {:writer :parent}) "\n")
               (fs/readFileSync file "utf8")))
        (is (not (fs/existsSync lock-file)))))))

(deftest secure-append-rejects-a-final-file-swap-before-writing
  (with-receipt-fixture
    (fn [{:keys [directory file]}]
      (let [original "{:original true}\n"
            replacement "{:replacement true}\n"
            moved (path/join directory "original.edn")]
        (fs/writeFileSync file original "utf8")
        (binding [cli/*secure-append-phase-hook*
                  (fn [phase _context]
                    (when (= :before-write phase)
                      (fs/renameSync file moved)
                      (fs/writeFileSync file replacement "utf8")))]
          (is (thrown-with-msg?
               js/Error #"Receipt River file identity changed"
               (cli/append-edn-line! file {:must-not-append true}))))
        (is (= original (fs/readFileSync moved "utf8")))
        (is (= replacement (fs/readFileSync file "utf8")))
        (is (not (fs/existsSync
                  (path/join directory ".receipts.edn.append.lock"))))))))

(deftest secure-append-rejects-a-parent-swap-before-writing
  (with-receipt-fixture
    (fn [{:keys [fixture directory file]}]
      (let [original "{:original true}\n"
            moved (path/join fixture "moved-parent")]
        (fs/writeFileSync file original "utf8")
        (binding [cli/*secure-append-phase-hook*
                  (fn [phase _context]
                    (when (= :before-write phase)
                      (fs/renameSync directory moved)
                      (fs/mkdirSync directory)))]
          (is (thrown-with-msg?
               js/Error #"parent directory identity changed"
               (cli/append-edn-line! file {:must-not-append true}))))
        (is (= original
               (fs/readFileSync (path/join moved "receipts.edn") "utf8")))
        (is (not (fs/existsSync file)))
        (is (not (fs/existsSync
                  (path/join moved ".receipts.edn.append.lock"))))))))

(deftest secure-append-quarantines-a-post-write-file-swap
  (with-receipt-fixture
    (fn [{:keys [directory file]}]
      (let [record {:append :durable-but-path-swapped}
            original "{:original true}\n"
            replacement "{:replacement true}\n"
            moved (path/join directory "written-before-swap.edn")
            lock-file (path/join directory ".receipts.edn.append.lock")]
        (fs/writeFileSync file original "utf8")
        (binding [cli/*secure-append-phase-hook*
                  (fn [phase _context]
                    (when (= :after-write phase)
                      (fs/renameSync file moved)
                      (fs/writeFileSync file replacement "utf8")))]
          (is (thrown-with-msg?
               js/Error #"Receipt River file identity changed"
               (cli/append-edn-line! file record))))
        (is (= (str original (pr-str record) "\n")
               (fs/readFileSync moved "utf8")))
        (is (= replacement (fs/readFileSync file "utf8")))
        (is (fs/existsSync lock-file))
        (is (thrown-with-msg?
             js/Error #"another writer holds the append lock"
             (cli/append-edn-line! file {:blocked :until-adjudicated})))))))

(deftest secure-append-quarantines-a-post-write-in-place-overwrite
  (with-receipt-fixture
    (fn [{:keys [directory file]}]
      (let [record {:append :durable-but-prefix-overwritten}
            original "{:original true}\n"
            replacement "{:forgedxx true}\n"
            lock-file (path/join directory ".receipts.edn.append.lock")]
        (is (= (.-byteLength (js/Buffer.from original "utf8"))
               (.-byteLength (js/Buffer.from replacement "utf8"))))
        (fs/writeFileSync file original "utf8")
        (binding [cli/*secure-append-phase-hook*
                  (fn [phase _context]
                    (when (= :after-write phase)
                      (let [fd (fs/openSync file "r+")
                            bytes (js/Buffer.from replacement "utf8")]
                        (try
                          (fs/writeSync fd bytes 0 (.-byteLength bytes) 0)
                          (fs/fsyncSync fd)
                          (finally
                            (fs/closeSync fd))))))]
          (is (thrown-with-msg?
               js/Error #"changed after append"
               (cli/append-edn-line! file record))))
        (is (= (str replacement (pr-str record) "\n")
               (fs/readFileSync file "utf8")))
        (is (fs/existsSync lock-file))
        (is (thrown-with-msg?
             js/Error #"another writer holds the append lock"
             (cli/append-edn-line! file {:blocked :until-adjudicated})))))))

(deftest secure-append-rejects-oversized-receipt-lines-before-opening
  (with-receipt-fixture
    (fn [{:keys [file]}]
      (let [oversized {:note (.repeat "x" (inc cli/max-receipt-line-bytes))}]
        (is (thrown-with-msg?
             js/Error #"receipt exceeds"
             (cli/append-edn-line! file oversized)))
        (is (not (fs/existsSync file)))))))

(deftest parses-explicit-repository-and-kind-selection
  (is (= {:only #{"katamorph"}
         :kinds #{:unit :static}
          :at nil
          :base nil}
         (cli/parse-args ["--only" "katamorph"
                          "--kind" "unit,static"])))
  (is (= (apply str (repeat 40 "a"))
         (:at (cli/parse-args
               ["--at" (apply str (repeat 40 "a"))]))))
  (is (= reviewed-root-revision
         (:base (cli/parse-args ["--base" reviewed-root-revision]))))
  (is (thrown-with-msg? js/Error #"Unknown gate kinds"
                        (cli/parse-args ["--kind" "pretend-e2e"]))))

(deftest appends-complete-receipt-river-evidence
  (let [captured (atom nil)]
    (with-redefs [cli/with-append-reservation!
                  (fn [file validate-ledger? append!]
                    (is validate-ledger?)
                    (append! {:file file}))
                  cli/append-reserved-edn-line!
                  (fn [reservation receipt line]
                    (reset! captured {:file (:file reservation)
                                      :receipt receipt
                                      :line line})
                    receipt)]
      (let [receipt (cli/append-evidence-receipt! passed-result)]
        (is (= receipt (:receipt @captured)))
        (is (= (pr-str receipt) (:line @captured)))
        (is (law/evidence-receipt? receipt))
        (is (= 2 (:evidence/schema receipt)))
        (is (re-find #"^nbb/node@v" (:evidence/adapter receipt)))
        (is (not= "local" (:host receipt)))
        (is (= passed-result (:evidence/result receipt)))
        (is (= [(str "repo@" child-revision) ":repo/unit"]
               (:refs receipt)))
        ;; New evidence lands in a fresh immutable segment, never the legacy file.
        (is (re-find #"[.]ημ/receipts/[0-9T-]+Z-[0-9a-f]{12}[.]edn$"
                     (:file @captured)))))))

(deftest evidence-append-refuses-to-recreate-a-missing-ledger
  (with-receipt-fixture
    (fn [{:keys [file segments]}]
      (with-redefs [cli/receipt-file file]
        (is (thrown-with-msg?
             js/Error #"could not be securely opened"
             (cli/append-evidence-receipt! passed-result)))
        (is (not (fs/existsSync file)))
        (is (= [] (segment-names segments)))
        (is (= [] (lock-files segments)))))))

(deftest evidence-append-requires-the-segment-directory
  (with-receipt-fixture
    (fn [{:keys [file segments]}]
      (fs/writeFileSync file "" "utf8")
      (fs/rmdirSync segments)
      (with-redefs [cli/receipt-file file]
        (is (thrown-with-msg?
             js/Error #"parent directory does not exist"
             (cli/append-evidence-receipt! passed-result)))
        (is (not (fs/existsSync segments)))))))

(deftest gate-runs-preflight-durable-receipt-support
  (let [calls (atom [])
        catalog {:catalog/repositories
                 {"repo" {:repository/path "repo"
                          :repository/gates [local-gate]}}}]
    (with-redefs [cli/require-secure-append-support!
                  (fn []
                    (swap! calls conj :preflight)
                    (throw (js/Error. "unsupported receipt host")))
                  cli/require-repositories!
                  (fn [& _]
                    (swap! calls conj :repositories)
                    {})
                  cli/run-gate!
                  (fn [& _]
                    (swap! calls conj :gate)
                    passed-result)]
      (is (thrown-with-msg?
           js/Error #"unsupported receipt host"
           (cli/run-selected-gates!
            catalog test-catalog-identity
            {:only #{"repo"} :kinds #{:unit}})))
      (is (= [:preflight] @calls)))))

(deftest selected-gates-hold-the-receipt-reservation-before-execution
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [gate-ran? (atom false)
            segment (path/join segments "2026-10-13T000000000Z-test.edn")
            lock-file (path/join segments ".2026-10-13T000000000Z-test.edn.append.lock")]
        ;; Semantic evidence appends require an explicitly initialized ledger.
        (fs/writeFileSync file "" "utf8")
        (is (zero? (run-one-gate!
                    {:file file
                     :segment segment
                     :git-capture (fn [args]
                                    (is (= ["rev-parse" "HEAD"] args))
                                    reviewed-root-revision)
                     :committed (fn [revision]
                                  (is (= reviewed-root-revision revision))
                                  (empty-committed-ledger revision))
                     :gate! (fn []
                              (reset! gate-ran? true)
                              (is (fs/existsSync lock-file))
                              (is (fs/existsSync segment)))})))
        (is @gate-ran?)
        (is (not (fs/existsSync lock-file)))
        (is (= "" (fs/readFileSync file "utf8")))
        (let [records (segment-records segments)]
          (is (= 1 (count records)))
          (is (= passed-result (:evidence/result (first records)))))))))

(deftest selected-gates-reject-an-unsafe-receipt-target-before-execution
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [gate-ran? (atom false)
            segment (path/join segments "2026-10-13T000000000Z-test.edn")
            lock-file (path/join segments ".2026-10-13T000000000Z-test.edn.append.lock")]
        (fs/writeFileSync file "" "utf8")
        (fs/writeFileSync lock-file "stale-or-held\n"
                          #js {:encoding "utf8" :flag "wx"})
        (is (thrown-with-msg?
             js/Error #"another writer holds the append lock"
             (run-one-gate! {:file file :segment segment
                             :gate! #(reset! gate-ran? true)})))
        (is (false? @gate-ran?))
        (is (not (fs/existsSync segment)))
        (is (= "stale-or-held\n"
               (fs/readFileSync lock-file "utf8")))))))

(deftest selected-gates-refuse-to-overwrite-an-existing-segment
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [gate-ran? (atom false)
            segment (path/join segments "2026-10-13T000000000Z-test.edn")]
        (fs/writeFileSync file "" "utf8")
        (fs/writeFileSync segment "" "utf8")
        (is (thrown-with-msg?
             js/Error #"could not be securely opened"
             (run-one-gate! {:file file :segment segment
                             :validate-working (fn [_] nil)
                             :repositories {"repo" {}}
                             :gate! #(reset! gate-ran? true)})))
        (is (false? @gate-ran?))
        (is (= "" (fs/readFileSync segment "utf8")))
        (is (= [] (lock-files segments)))))))

(deftest selected-gates-refuse-to-recreate-a-missing-ledger
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [gate-ran? (atom false)]
        (is (thrown-with-msg?
             js/Error #"could not be securely opened"
             (run-one-gate! {:file file :gate! #(reset! gate-ran? true)})))
        (is (false? @gate-ran?))
        (is (not (fs/existsSync file)))
        (is (= [] (segment-names segments)))
        (is (= [] (lock-files segments)))))))

(deftest selected-gates-reject-a-valid-truncated-committed-ledger
  (let [first-record {:ts "2026-08-29T22:58:48Z"
                      :kind :decision
                      :repo "."
                      :origin "test"
                      :owner "test"
                      :dod "preserve first"
                      :pi "eta-mu"
                      :host "test"
                      :manifest ["test"]
                      :refs ["first"]}
        second-record (assoc first-record
                             :ts "2026-08-29T22:58:49Z"
                             :dod "preserve second"
                             :refs ["second"])
        first-line (str (pr-str first-record) "\n")
        second-line (str (pr-str second-record) "\n")
        committed-text (str first-line second-line)
        committed-bytes (js/Buffer.from committed-text "utf8")
        committed-segment ".ημ/receipts/2026-08-29T225849Z-second.edn"]
    (is (law/receipt-envelope? first-record))
    (is (law/receipt-envelope? second-record))
    ;; Dropping a committed legacy record, or deleting or editing a committed
    ;; segment, must all be rejected before the gate runs.
    (doseq [[legacy-text committed-segments held-segment]
            [["" {} nil]
             [first-line {} nil]
             [first-line {committed-segment second-line} nil]
             [first-line {committed-segment second-line} first-line]]]
      (with-receipt-fixture
        (fn [{:keys [segments file]}]
          (let [gate-ran? (atom false)
                legacy-committed (if (seq committed-segments)
                                   (js/Buffer.from first-line "utf8")
                                   committed-bytes)]
            (fs/writeFileSync file legacy-text "utf8")
            (when held-segment
              (fs/writeFileSync (path/join segments "2026-08-29T225849Z-second.edn")
                                held-segment "utf8"))
            (is (thrown-with-msg?
                 js/Error #"does not preserve the committed ledger"
                 (run-one-gate!
                  {:file file
                   :git-capture (fn [args]
                                  (is (= ["rev-parse" "HEAD"] args))
                                  reviewed-root-revision)
                   :committed (fn [revision]
                                (is (= reviewed-root-revision revision))
                                (assoc (empty-committed-ledger revision)
                                       :ledger/bytes legacy-committed
                                       :ledger/records
                                       (cli/read-receipt-records!
                                        (.toString legacy-committed "utf8"))))
                   :segments (fn [_]
                               (mapv (fn [[k v]]
                                       (cli/segment-entry
                                        k (js/Buffer.from v "utf8")))
                                     (sort committed-segments)))
                   :gate! #(reset! gate-ran? true)})))
            (is (false? @gate-ran?))
            (is (= legacy-text (fs/readFileSync file "utf8")))
            (is (= [] (lock-files segments)))))))))

(deftest selected-gates-reject-an-invalid-held-ledger-before-execution
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [gate-ran? (atom false)
            invalid-segment "{:kind :observation}\n"
            invalid-file (path/join segments "2026-10-13T000000000Z-bad.edn")]
        (fs/writeFileSync file "" "utf8")
        (fs/writeFileSync invalid-file invalid-segment "utf8")
        (is (thrown-with-msg?
             js/Error #"invalid receipt envelopes"
             (run-one-gate! {:file file :gate! #(reset! gate-ran? true)})))
        (is (false? @gate-ran?))
        (is (= invalid-segment (fs/readFileSync invalid-file "utf8")))
        (is (= ["2026-10-13T000000000Z-bad.edn"] (segment-names segments)))
        (is (= [] (lock-files segments)))))))

(deftest selected-gates-reject-legacy-appends-and-stray-files
  (doseq [[setup! message]
          [[(fn [{:keys [file]}]
              (fs/writeFileSync file (str (pr-str (cli/evidence-receipt
                                                   passed-result
                                                   "2026-08-29T17:22:40Z"
                                                   "test" "nbb/node@test"))
                                          "\n")
                                "utf8"))
            #"legacy Receipt River ledger .* is frozen"]
           [(fn [{:keys [segments]}]
              (fs/writeFileSync (path/join segments "notes.txt") "x" "utf8"))
            #"unexpected file in the Receipt River segment directory"]
           [(fn [{:keys [segments]}]
              (fs/writeFileSync (path/join segments "2026-10-13T000000Z-empty.edn")
                                "" "utf8"))
            #"segment must not be empty"]
           [(fn [{:keys [segments]}]
              (fs/writeFileSync (path/join segments "receipt.edn") "{}\n" "utf8"))
            #"unexpected file in the Receipt River segment directory"]
           [(fn [{:keys [segments]}]
              (fs/writeFileSync (path/join segments "2026-10-13T000000Z-two.edn")
                                "{:a 1}\n{:a 2}\n" "utf8"))
            #"exactly one record"]
           [(fn [{:keys [segments]}]
              (fs/writeFileSync
               (path/join segments ".2026-10-13T000000Z-old.edn.append.lock")
               "pid=1\n" "utf8"))
            #"retained append lock quarantines"]]]
    (with-receipt-fixture
      (fn [{:keys [file] :as context}]
        (fs/writeFileSync file "" "utf8")
        (setup! context)
        (let [gate-ran? (atom false)]
          (is (thrown-with-msg?
               js/Error message
               (run-one-gate! {:file file
                               :repositories {"repo" {}}
                               :gate! #(reset! gate-ran? true)})))
          (is (false? @gate-ran?)))))))

(deftest selected-gates-reject-held-ledger-mutation-after-execution
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [gate-ran? (atom false)
            changed-ledger "{:kind :observation}\n"
            segment (path/join segments "2026-10-13T000000000Z-test.edn")]
        (fs/writeFileSync file "" "utf8")
        (is (thrown-with-msg?
             js/Error #"changed after held ledger validation"
             (run-one-gate!
              {:file file
               :segment segment
               :gate! (fn []
                        (reset! gate-ran? true)
                        ;; Model a non-cooperating writer that ignores the held lock.
                        (fs/writeFileSync segment changed-ledger "utf8"))})))
        (is @gate-ran?)
        (is (= changed-ledger (fs/readFileSync segment "utf8")))
        ;; The injected content cannot be safely removed, so the writer lock
        ;; is retained as a quarantine marker instead of being released.
        (is (= [(str "." (path/basename segment) ".append.lock")]
               (lock-files segments)))
        (with-redefs [cli/receipt-file file]
          (is (thrown-with-msg?
               js/Error #"retained append lock quarantines"
               (cli/read-working-receipt-segments! nil))))))))

(deftest selected-gates-reject-segments-added-while-the-gate-runs
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            forged (path/join segments "2026-10-13T000001000Z-forged.edn")
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))]
        (fs/writeFileSync file "" "utf8")
        (is (thrown-with-msg?
             js/Error #"ledger changed while the gate ran"
             (run-semantic-gate-with!
              file segment
              ;; Candidate gate code promotes its own schema-valid output into
              ;; a second, well-named segment.
              #(fs/writeFileSync forged (str forged-line "\n") "utf8"))))
        (is (= [(path/basename forged)] (segment-names segments))
            "no receipt was written for the gate")
        (is (= [(str "." (path/basename segment) ".append.lock")]
               (lock-files segments))
            "the lock quarantines the ledger until the forged segment is adjudicated")))))

(defn run-gate-against-reserved-segment! [segments file mutate!]
  (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
        renamed (path/join segments "2026-10-13T000001000Z-forged.edn")
        forged-line (cli/encode-edn-line!
                     (cli/validated-evidence-receipt!
                      (assoc passed-result :result/command ["forged"])))]
    (is (thrown?
         js/Error
         (run-one-gate! {:file file
                         :segment segment
                         :gate! #(mutate! {:segment segment :renamed renamed
                                           :forged-line forged-line})})))
    {:segment segment :renamed renamed}))

(deftest selected-gates-retain-writer-lock-when-segment-lock-vanishes
  ;; Candidate gate code can unlink the per-segment lock and add its own
  ;; well-named segment. Pre-write lock-identity then aborts before ledger
  ;; revalidation; cleanup compares the unwritten reservation's ledger with
  ;; the pre-gate snapshot, so the vanished segment lock is recreated as a
  ;; durable quarantine marker and the forged segment cannot be accepted as an
  ;; ordinary extension on the next run.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            forged (path/join segments "2026-10-13T000001000Z-forged.edn")
            append-lock (path/join segments
                                   (str "." (path/basename segment) ".append.lock"))
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))]
        (fs/writeFileSync file "" "utf8")
        (is (thrown-with-msg?
             js/Error #"append lock"
             (run-semantic-gate-with!
              file segment
              (fn []
                (fs/unlinkSync append-lock)
                (fs/writeFileSync forged (str forged-line "\n") "utf8")))))
        (is (= [(path/basename forged)] (segment-names segments))
            "no receipt was written for the gate")
        (is (= [(path/basename append-lock)] (lock-files segments))
            "the vanished segment lock is recreated as the quarantine marker")
        (with-redefs [cli/receipt-file file]
          (is (thrown-with-msg?
               js/Error #"quarantines the Receipt River ledger"
               (cli/open-append-reservation!
                (path/join segments "2026-10-13T000002000Z-next.edn") true))))))))

(defn next-writer-refused? [segments file]
  (with-redefs [cli/receipt-file file
                cli/git-capture! (fn [_] reviewed-root-revision)
                cli/read-immutable-receipt-ledger! empty-committed-ledger
                cli/read-immutable-receipt-segment-entries! (fn [_] [])]
    (try
      (cli/close-append-reservation!
       (cli/open-append-reservation!
        (path/join segments "2026-10-13T000002000Z-next.edn") true))
      false
      (catch :default _error true))))

(deftest selected-gates-reestablish-quarantine-when-both-locks-vanish
  ;; Gate code unlinks the per-segment lock and the writer lock, then adds a
  ;; valid forged segment. Closing the held writer-lock descriptor preserves
  ;; nothing once its path is gone, so cleanup must verify the writer lock and
  ;; durably recreate a quarantine marker before relying on it.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            forged (path/join segments "2026-10-13T000001000Z-forged.edn")
            writer-lock (path/join segments cli/receipt-writer-lock-name)
            append-lock (path/join segments
                                   (str "." (path/basename segment) ".append.lock"))
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))]
        (fs/writeFileSync file "" "utf8")
        (is (thrown?
             js/Error
             (run-semantic-gate-with!
              file segment
              (fn []
                (fs/unlinkSync append-lock)
                (fs/unlinkSync writer-lock)
                (fs/writeFileSync forged (str forged-line "\n") "utf8")))))
        (is (= [(path/basename forged)] (segment-names segments))
            "no receipt was written for the gate")
        (is (fs/existsSync writer-lock)
            "a quarantine marker replaces the vanished writer lock")
        (is (next-writer-refused? segments file)
            "the forged segment cannot be accepted as an ordinary extension")))))

(deftest selected-gates-reestablish-a-vanished-segment-quarantine
  ;; Gate code writes forged content into the reserved segment and unlinks
  ;; its lock. The reservation is retained as quarantine, so the segment lock
  ;; must be recreated rather than merely closed.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            append-lock (path/join segments
                                   (str "." (path/basename segment) ".append.lock"))
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))]
        (fs/writeFileSync file "" "utf8")
        (is (thrown?
             js/Error
             (run-semantic-gate-with!
              file segment
              (fn []
                (fs/unlinkSync append-lock)
                (fs/writeFileSync segment (str forged-line "\n") "utf8")))))
        (is (= [(path/basename append-lock)] (lock-files segments))
            "the segment quarantine marker is recreated")
        (is (next-writer-refused? segments file))))))

(deftest selected-gates-quarantine-a-replaced-segment-directory
  ;; Gate code renames the segment directory away and installs a fresh real
  ;; directory holding a schema-valid forged segment. Every lock and cleanup
  ;; step then goes through the held descriptor of the renamed directory, so
  ;; the quarantine must be recorded through the stable `.ημ` ancestor that
  ;; the next writer actually reads.
  (with-receipt-fixture
    (fn [{:keys [directory segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            moved (path/join directory "receipts.moved")
            forged (path/join segments "2026-10-13T000001000Z-forged.edn")
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))]
        (fs/writeFileSync file "" "utf8")
        (is (thrown?
             js/Error
             (run-semantic-gate-with!
              file segment
              (fn []
                (fs/renameSync segments moved)
                (fs/mkdirSync segments)
                (fs/writeFileSync forged (str forged-line "\n") "utf8")))))
        (is (= [(path/basename forged)] (segment-names segments))
            "no receipt was written for the gate")
        (is (fs/existsSync (path/join directory cli/receipt-quarantine-marker-name))
            "the quarantine marker lives in the stable `.ημ` ancestor")
        (with-redefs [cli/receipt-file file]
          (is (thrown-with-msg?
               js/Error #"quarantine"
               (cli/read-working-receipt-segments! nil))
              "the replacement directory is quarantined"))
        (is (next-writer-refused? segments file)
            "the forged segment cannot be accepted as an ordinary extension")))))

(deftest selected-gates-quarantine-directory-replaced-during-lock-cleanup
  ;; After a verified write, cleanup releases the per-segment lock. Background
  ;; work that watches for that unlock can rename the segment directory away
  ;; and install a fresh one before the writer lock is released. The one-time
  ;; identity snapshot at the start of close then misses the replacement, so
  ;; cleanup would follow the held descriptor into the renamed directory,
  ;; release the writer lock there, and leave the canonical ledger without
  ;; `.ημ/.receipt-river.quarantine`. Recheck after segment-lock cleanup.
  (with-receipt-fixture
    (fn [{:keys [directory segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            moved (path/join directory "receipts.moved")
            forged (path/join segments "2026-10-13T000001000Z-forged.edn")
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))
            release-append-lock! cli/release-append-lock!
            replaced-during-unlock? (atom false)]
        (fs/writeFileSync file "" "utf8")
        (with-redefs [cli/release-append-lock!
                      (fn [parent lock]
                        (release-append-lock! parent lock)
                        ;; release-writer-lock! also calls release-append-lock!;
                        ;; rename only on the first (per-segment) unlock.
                        (when (compare-and-set! replaced-during-unlock? false true)
                          (fs/renameSync segments moved)
                          (fs/mkdirSync segments)
                          (fs/writeFileSync forged (str forged-line "\n") "utf8")))]
          (is (thrown-with-msg?
               js/Error #"replaced during lock cleanup|quarantine"
               (run-semantic-gate-with! file segment (fn [])))))
        (is (fs/existsSync (path/join directory cli/receipt-quarantine-marker-name))
            "the quarantine marker lives in the stable `.ημ` ancestor")
        (with-redefs [cli/receipt-file file]
          (is (thrown-with-msg?
               js/Error #"quarantine"
               (cli/read-working-receipt-segments! nil))
              "the replacement directory is quarantined"))
        (is (next-writer-refused? segments file)
            "the forged segment cannot be accepted as an ordinary extension")))))

(deftest selected-gates-quarantine-a-segment-added-after-the-receipt-write
  ;; Gate-spawned work can wait for the reserved segment to become nonempty
  ;; and then add a schema-valid forged segment. The pre-write comparison has
  ;; already passed, so the whole working ledger is compared again after the
  ;; write; an unexpected segment quarantines before the locks are released.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            forged (path/join segments "2026-10-13T000001000Z-forged.edn")
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))]
        (fs/writeFileSync file "" "utf8")
        (binding [cli/*secure-append-phase-hook*
                  (fn [phase _]
                    (when (= :after-write phase)
                      (fs/writeFileSync forged (str forged-line "\n") "utf8")))]
          (is (thrown-with-msg?
               js/Error #"changed after the receipt was written"
               (run-semantic-gate-with! file segment (fn [])))))
        (is (next-writer-refused? segments file)
            "the forged segment cannot be accepted as an ordinary extension")))))

(deftest selected-gates-quarantine-a-segment-added-during-lock-cleanup
  ;; The same forged segment can be added once the per-segment lock is
  ;; released, while the writer lock still serializes the checkout. Compare
  ;; the whole ledger again before releasing the writer lock and retain it as
  ;; the quarantine marker on any change.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            forged (path/join segments "2026-10-13T000001000Z-forged.edn")
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))
            release-append-lock! cli/release-append-lock!
            forged? (atom false)]
        (fs/writeFileSync file "" "utf8")
        (with-redefs [cli/release-append-lock!
                      (fn [parent lock]
                        (release-append-lock! parent lock)
                        ;; Forge only after the first (per-segment) unlock.
                        (when (compare-and-set! forged? false true)
                          (fs/writeFileSync forged (str forged-line "\n")
                                            "utf8")))]
          (is (thrown-with-msg?
               js/Error #"changed during lock cleanup"
               (run-semantic-gate-with! file segment (fn [])))))
        (is (fs/existsSync (path/join segments cli/receipt-writer-lock-name))
            "the writer lock is retained as the quarantine marker")
        (is (next-writer-refused? segments file)
            "the forged segment cannot be accepted as an ordinary extension")))))

(deftest selected-gates-quarantine-a-segment-changed-after-it-is-closed
  ;; Gate-spawned work can watch the reserved segment for CLOSE_WRITE and
  ;; overwrite it in place or rename another file over it right after the
  ;; verified close. The written bytes are kept and the reserved path is
  ;; revalidated after closing; any mismatch quarantines.
  (doseq [[label mutate!]
          [["overwritten in place"
            (fn [segment forged-line]
              (fs/writeFileSync segment (str forged-line "\n") "utf8"))]
           ["replaced by rename"
            (fn [segment forged-line]
              (let [staged (str segment ".staged")]
                (fs/writeFileSync staged (str forged-line "\n") "utf8")
                (fs/renameSync staged segment)))]]]
    (with-receipt-fixture
      (fn [{:keys [segments file]}]
        (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
              forged-line (cli/encode-edn-line!
                           (cli/validated-evidence-receipt!
                            (assoc passed-result :result/command ["forged"])))]
          (fs/writeFileSync file "" "utf8")
          (binding [cli/*secure-append-phase-hook*
                    (fn [phase _]
                      (when (= :after-close phase)
                        (mutate! segment forged-line)))]
            (is (thrown-with-msg?
                 js/Error #"changed after it was closed"
                 (run-semantic-gate-with! file segment (fn [])))
                label))
          (is (next-writer-refused? segments file)
              (str label ": the altered receipt cannot become ordinary evidence")))))))

(deftest selected-gates-quarantine-a-segment-changed-during-lock-cleanup
  ;; The same overwrite can land once the per-segment lock is released. The
  ;; reserved segment is revalidated before the writer lock is released, and
  ;; the writer lock is retained as the quarantine marker on any change.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))
            release-append-lock! cli/release-append-lock!
            forged? (atom false)]
        (fs/writeFileSync file "" "utf8")
        (with-redefs [cli/release-append-lock!
                      (fn [parent lock]
                        (release-append-lock! parent lock)
                        (when (compare-and-set! forged? false true)
                          (fs/writeFileSync segment (str forged-line "\n")
                                            "utf8")))]
          (is (thrown-with-msg?
               js/Error #"changed during lock cleanup"
               (run-semantic-gate-with! file segment (fn [])))))
        (is (fs/existsSync (path/join segments cli/receipt-writer-lock-name))
            "the writer lock is retained as the quarantine marker")
        (is (next-writer-refused? segments file)
            "the altered receipt cannot become ordinary evidence")))))

(deftest selected-gates-quarantine-ledger-changes-after-writer-lock-release
  ;; Gate-spawned work can wait for `.receipt-river.writer.lock` to be
  ;; unlinked and then add or rewrite a segment. The post-release path must
  ;; revalidate ledger contents and reserved bytes, not only directory
  ;; identity, and establish a quarantine marker on any mismatch.
  (doseq [[label mutate!]
          [["added segment"
            (fn [segments _segment forged-line]
              (fs/writeFileSync
               (path/join segments "2026-10-13T000001000Z-forged.edn")
               (str forged-line "\n") "utf8"))]
           ["rewritten reserved segment"
            (fn [_segments segment forged-line]
              (fs/writeFileSync segment (str forged-line "\n") "utf8"))]]]
    (with-receipt-fixture
      (fn [{:keys [segments file]}]
        (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
              forged-line (cli/encode-edn-line!
                           (cli/validated-evidence-receipt!
                            (assoc passed-result :result/command ["forged"])))
              release-writer-lock! cli/release-writer-lock!]
          (fs/writeFileSync file "" "utf8")
          (with-redefs [cli/release-writer-lock!
                        (fn [parent writer-lock]
                          (release-writer-lock! parent writer-lock)
                          (mutate! segments segment forged-line))]
            (is (thrown-with-msg?
                 js/Error #"changed after writer-lock release"
                 (run-semantic-gate-with! file segment (fn [])))
                label))
          (is (fs/existsSync (path/join segments cli/receipt-writer-lock-name))
              (str label ": writer lock recreated as the quarantine marker"))
          (is (next-writer-refused? segments file)
              (str label ": forged content cannot become ordinary evidence")))))))

(deftest lock-acquisition-failures-remove-the-new-lock
  ;; A lock this process created with O_EXCL is removed again when marker
  ;; setup fails before any gate ran, so later writers are not blocked by a
  ;; lock nobody holds. A path that no longer names the created lock is left
  ;; for adjudication.
  (with-receipt-fixture
    (fn [{:keys [segments]}]
      (let [parent (cli/open-parent-directory! segments)
            segment-lock (path/join segments ".test.edn.append.lock")
            writer-lock (path/join segments cli/receipt-writer-lock-name)]
        (try
          (with-redefs [cli/write-all! (fn [& _] (throw (js/Error. "marker write failed")))]
            (is (thrown-with-msg?
                 js/Error #"marker write failed"
                 (cli/acquire-append-lock! parent "test.edn")))
            (is (not (fs/existsSync segment-lock))
                "an aborted segment-lock acquisition leaves no lock")
            (is (thrown-with-msg?
                 js/Error #"marker write failed"
                 (cli/acquire-receipt-writer-lock! parent)))
            (is (not (fs/existsSync writer-lock))
                "an aborted writer-lock acquisition leaves no lock"))
          (with-redefs [cli/write-all!
                        (fn [& _]
                          (fs/unlinkSync segment-lock)
                          (fs/writeFileSync segment-lock "other\n" "utf8")
                          (throw (js/Error. "marker write failed")))]
            (is (thrown-with-msg?
                 js/Error #"marker write failed"
                 (cli/acquire-append-lock! parent "test.edn")))
            (is (= "other\n" (fs/readFileSync segment-lock "utf8"))
                "a replaced lock path is not removed"))
          (finally
            (fs/closeSync (:fd parent))))))))

(deftest selected-gates-quarantine-when-only-the-writer-lock-vanishes
  ;; Gate code can unlink the ledger-wide writer lock and leave the segment
  ;; lock intact, before or after the receipt write. The writer lock is
  ;; verified around the write, and a failed writer-lock release recreates a
  ;; quarantine marker at its path, so the run's receipt can never become an
  ;; ordinary extension.
  (doseq [[label gate! hook]
          [["unlinked by the gate"
            (fn [segments]
              (fs/unlinkSync (path/join segments cli/receipt-writer-lock-name)))
            (fn [_segments _phase] nil)]
           ["unlinked after the write"
            (fn [_segments] nil)
            (fn [segments phase]
              (when (= :after-write phase)
                (fs/unlinkSync
                 (path/join segments cli/receipt-writer-lock-name))))]]]
    (with-receipt-fixture
      (fn [{:keys [segments file]}]
        (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")]
          (fs/writeFileSync file "" "utf8")
          (binding [cli/*secure-append-phase-hook*
                    (fn [phase _] (hook segments phase))]
            (is (thrown? js/Error
                         (run-semantic-gate-with!
                          file segment #(gate! segments)))
                label))
          (is (next-writer-refused? segments file)
              (str label ": the run cannot leave ordinary evidence")))))))


(deftest selected-gates-quarantine-a-segment-added-before-a-gate-throws
  ;; A gate can add a schema-valid segment and then throw, so
  ;; append-reserved-edn-line! never runs. Cleanup of the unwritten
  ;; reservation compares the ledger with the pre-gate snapshot too.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            forged (path/join segments "2026-10-13T000001000Z-forged.edn")
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))]
        (fs/writeFileSync file "" "utf8")
        (is (thrown-with-msg?
             js/Error #"gate exploded"
             (run-semantic-gate-with!
              file segment
              (fn []
                (fs/writeFileSync forged (str forged-line "\n") "utf8")
                (throw (js/Error. "gate exploded"))))))
        (is (not (fs/existsSync segment))
            "the unwritten reservation is still removed")
        (is (next-writer-refused? segments file)
            "the forged segment cannot be accepted as an ordinary extension")))))


(deftest selected-gates-quarantine-a-segment-added-after-a-failed-gate-unlocks
  ;; A gate can throw and leave delayed work that waits for the per-segment
  ;; lock to disappear before adding a forged segment. The unwritten
  ;; reservation's ledger is compared again before the writer lock is
  ;; released, and the writer lock is retained on any change.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            forged (path/join segments "2026-10-13T000001000Z-forged.edn")
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))
            release-append-lock! cli/release-append-lock!
            forged? (atom false)]
        (fs/writeFileSync file "" "utf8")
        (with-redefs [cli/release-append-lock!
                      (fn [parent lock]
                        (release-append-lock! parent lock)
                        (when (compare-and-set! forged? false true)
                          (fs/writeFileSync forged (str forged-line "\n")
                                            "utf8")))]
          (is (thrown? js/Error
                       (run-semantic-gate-with!
                        file segment
                        (fn [] (throw (js/Error. "gate exploded")))))))
        (is (fs/existsSync (path/join segments cli/receipt-writer-lock-name))
            "the writer lock is retained as the quarantine marker")
        (is (next-writer-refused? segments file)
            "the forged segment cannot be accepted as an ordinary extension")))))

(deftest selected-gates-quarantine-a-recreated-reserved-basename-after-failed-unlock
  ;; Delayed work can recreate the just-deleted reserved segment under its
  ;; own basename after the per-segment lock is gone. After an unwritten
  ;; reservation is removed, cleanup must notice that basename's return.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))
            release-append-lock! cli/release-append-lock!
            forged? (atom false)]
        (fs/writeFileSync file "" "utf8")
        (with-redefs [cli/release-append-lock!
                      (fn [parent lock]
                        (release-append-lock! parent lock)
                        (when (compare-and-set! forged? false true)
                          (fs/writeFileSync segment (str forged-line "\n")
                                            "utf8")))]
          (is (thrown-with-msg?
               js/Error #"changed during lock cleanup|reappeared|basename"
               (run-semantic-gate-with!
                file segment
                (fn [] (throw (js/Error. "gate exploded")))))))
        (is (fs/existsSync (path/join segments cli/receipt-writer-lock-name))
            "the writer lock is retained as the quarantine marker")
        (is (next-writer-refused? segments file)
            "the recreated reserved basename cannot become ordinary evidence")))))

(deftest selected-gates-quarantine-a-reserved-basename-recreated-after-writer-unlock
  ;; Delayed work can wait for the writer-lock unlink itself and only then
  ;; recreate the removed reservation under its own basename. The final
  ;; post-release check skips that basename in the whole-ledger comparison,
  ;; so it must look for it explicitly when the reservation was unwritten.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))
            release-writer-lock! cli/release-writer-lock!]
        (fs/writeFileSync file "" "utf8")
        (with-redefs [cli/release-writer-lock!
                      (fn [parent writer-lock]
                        (release-writer-lock! parent writer-lock)
                        (fs/writeFileSync segment (str forged-line "\n")
                                          "utf8"))]
          (is (thrown-with-msg?
               js/Error #"after writer-lock release"
               (run-semantic-gate-with!
                file segment
                (fn [] (throw (js/Error. "gate exploded")))))))
        (is (fs/existsSync (path/join segments cli/receipt-writer-lock-name))
            "a quarantine marker is recreated at the writer-lock path")
        (is (next-writer-refused? segments file)
            "the recreated reserved basename cannot become ordinary evidence")))))

(deftest selected-gates-durably-quarantine-a-replaced-ledger-directory
  ;; Gate code renames `.ημ` itself away and installs a fresh one holding a
  ;; durable forged segment. The held `.ημ` descriptor then names the moved
  ;; directory, so the canonical replacement gets its own marker, and that
  ;; marker's directory entry must be fsynced through the canonical
  ;; replacement: a crash must not keep the forged ledger but lose the
  ;; quarantine.
  (with-receipt-fixture
    (fn [{:keys [fixture directory segments file]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-test.edn")
            moved (path/join fixture ".ημ.moved")
            forged (path/join segments "2026-10-13T000001000Z-forged.edn")
            marker (path/join directory cli/receipt-quarantine-marker-name)
            forged-line (cli/encode-edn-line!
                         (cli/validated-evidence-receipt!
                          (assoc passed-result :result/command ["forged"])))
            synced-parents (atom [])
            create-marker! cli/create-quarantine-marker!]
        (fs/writeFileSync file "" "utf8")
        (with-redefs [cli/create-quarantine-marker!
                      (fn [parent marker-path]
                        ;; Record which directory this marker's entry is
                        ;; fsynced through (nil means none).
                        (swap! synced-parents conj
                               (when parent
                                 (let [stats (fs/fstatSync (:fd parent))]
                                   [(.-dev stats) (.-ino stats)])))
                        (create-marker! parent marker-path))]
          (is (thrown?
               js/Error
               (run-semantic-gate-with!
                file segment
                (fn []
                  (fs/renameSync directory moved)
                  (fs/mkdirSync directory)
                  (fs/mkdirSync segments)
                  (fs/writeFileSync file "" "utf8")
                  (fs/writeFileSync forged (str forged-line "\n") "utf8"))))))
        (is (fs/existsSync marker)
            "the canonical replacement `.ημ` carries a quarantine marker")
        (let [canonical (fs/statSync directory)]
          (is (some #{[(.-dev canonical) (.-ino canonical)]} @synced-parents)
              "the canonical marker is fsynced through the canonical directory"))
        (with-redefs [cli/receipt-file file]
          (is (thrown-with-msg?
               js/Error #"quarantine"
               (cli/read-working-receipt-segments! nil))
              "the replacement ledger is quarantined"))))))

(deftest working-ledger-rejects-executable-files
  ;; The immutable reader accepts only 100644 blobs, so an executable working
  ;; file would yield evidence that cannot be verified once committed.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [segment (path/join segments "2026-10-12T000000000Z-a.edn")
            line (cli/encode-edn-line!
                  (cli/validated-evidence-receipt!
                   (assoc passed-result :result/command ["earlier"])))]
        (fs/writeFileSync file "" "utf8")
        (fs/writeFileSync segment (str line "\n") "utf8")
        (with-redefs [cli/receipt-file file]
          (is (= 2 (count (cli/read-working-receipt-segments! nil))))
          (fs/chmodSync segment 0755)
          (is (thrown-with-msg?
               js/Error #"must not be executable"
               (cli/read-working-receipt-segments! nil)))
          (fs/chmodSync segment 0644)
          (fs/chmodSync file 0744)
          (is (thrown-with-msg?
               js/Error #"must not be executable"
               (cli/read-working-receipt-segments! nil))))))))

(deftest selected-gates-reject-mode-changes-while-the-gate-runs
  ;; Each scenario gets a fresh fixture: a mode change during the gate fails
  ;; revalidation and quarantines (retains the append lock).
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [existing (path/join segments "2026-10-12T000000000Z-a.edn")
            segment (path/join segments "2026-10-13T000000000Z-test.edn")
            line (cli/encode-edn-line!
                  (cli/validated-evidence-receipt!
                   (assoc passed-result :result/command ["earlier"])))]
        (fs/writeFileSync file "" "utf8")
        (fs/writeFileSync existing (str line "\n") "utf8")
        (is (thrown-with-msg?
             js/Error #"must not be executable"
             (run-semantic-gate-with!
              file segment #(fs/chmodSync existing 0755))))
        (is (= [(path/basename existing)] (segment-names segments))
            "no receipt was written")
        (is (seq (lock-files segments))
            "revalidation failure quarantines the ledger"))))
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (let [existing (path/join segments "2026-10-12T000000000Z-a.edn")
            segment (path/join segments "2026-10-13T000000000Z-test.edn")
            line (cli/encode-edn-line!
                  (cli/validated-evidence-receipt!
                   (assoc passed-result :result/command ["earlier"])))]
        (fs/writeFileSync file "" "utf8")
        (fs/writeFileSync existing (str line "\n") "utf8")
        (is (thrown-with-msg?
             js/Error #"must not be executable"
             (run-semantic-gate-with!
              file segment #(fs/chmodSync segment 0700))))
        (is (= [(path/basename existing)] (segment-names segments))
            "the executable reservation was not written or kept")
        ;; Own reserved basename is skipped in working-ledger validation, so the
        ;; executable-bit refusal on the target does not set quarantined?; cleanup
        ;; removes the unwritten reservation and releases the lock.
        (is (= [] (lock-files segments)))))))

(deftest selected-gates-quarantine-a-renamed-reservation
  ;; Path absence is not proof of removal: the reserved inode may live on
  ;; under another well-named segment holding forged content.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (fs/writeFileSync file "" "utf8")
      (with-redefs [cli/receipt-file file]
        (let [{:keys [segment renamed]}
              (run-gate-against-reserved-segment!
               segments file
               (fn [{:keys [segment renamed forged-line]}]
                 (fs/renameSync segment renamed)
                 (fs/writeFileSync renamed (str forged-line "\n") "utf8")))]
          (is (= [(path/basename renamed)] (segment-names segments)))
          (is (= [(str "." (path/basename segment) ".append.lock")]
                 (lock-files segments))
              "the lock is retained because the held inode was not unlinked")
          (is (thrown-with-msg?
               js/Error #"retained append lock quarantines"
               (cli/read-working-receipt-segments! nil))))))))

(deftest selected-gates-release-an-unlinked-reservation
  ;; When the held inode is provably gone (no links remain), nothing of the
  ;; reservation survives and the lock is released.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (fs/writeFileSync file "" "utf8")
      (with-redefs [cli/receipt-file file]
        (run-gate-against-reserved-segment!
         segments file
         (fn [{:keys [segment]}] (fs/unlinkSync segment)))
        (is (= [] (segment-names segments)))
        (is (= [] (lock-files segments)))))))

(deftest selected-gates-quarantine-a-hard-linked-reservation
  ;; Unlinking the reserved path is not proof either: a hard link made by gate
  ;; code keeps the held inode alive under another well-named segment.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (fs/writeFileSync file "" "utf8")
      (with-redefs [cli/receipt-file file]
        (let [{:keys [segment renamed]}
              (run-gate-against-reserved-segment!
               segments file
               (fn [{:keys [segment renamed]}]
                 (fs/linkSync segment renamed)))]
          (is (not (fs/existsSync segment)))
          (is (= [(str "." (path/basename segment) ".append.lock")]
                 (lock-files segments))
              "the lock is retained because the held inode still has a link")
          ;; A detached gate process later writes through the surviving link.
          (fs/writeFileSync renamed "{:kind :observation}\n" "utf8")
          (is (thrown-with-msg?
               js/Error #"retained append lock quarantines"
               (cli/read-working-receipt-segments! nil))))))))

(deftest writer-lock-release-failures-are-reported
  ;; A replaced writer lock would block every later writer; the command that
  ;; left it behind must fail instead of reporting success.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (fs/writeFileSync file "" "utf8")
      (let [writer-lock (path/join segments cli/receipt-writer-lock-name)
            segment (path/join segments "2026-10-13T000000000Z-test.edn")]
        (is (thrown-with-msg?
             js/Error #"writer lock could not be released"
             (run-semantic-gate-with!
              file segment
              (fn []
                (fs/unlinkSync writer-lock)
                (fs/writeFileSync writer-lock "pid=0\n" "utf8")))))
        (is (= [] (segment-records segments))
            "no receipt is written once the writer lock was replaced")
        (is (= "pid=0\n" (fs/readFileSync writer-lock "utf8"))
            "the replaced lock is left for adjudication")
        (is (= [] (lock-files segments)))))))

(deftest concurrent-writers-are-told-apart-from-quarantine
  ;; Same-checkout writers serialize on one ledger-wide writer lock, so a
  ;; second writer is refused as "active", never as a quarantine verdict, and
  ;; any segment that appears while a gate runs cannot be a cooperating
  ;; writer's receipt.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (fs/writeFileSync file "" "utf8")
      (with-redefs [cli/receipt-file file
                    cli/git-capture! (fn [_] reviewed-root-revision)
                    cli/read-immutable-receipt-ledger! empty-committed-ledger
                    cli/read-immutable-receipt-segment-entries! (fn [_] [])]
        (let [first-segment (path/join segments "2026-10-13T000000000Z-a.edn")
              second-segment (path/join segments "2026-10-13T000001000Z-b.edn")
              active (cli/open-append-reservation! first-segment true)]
          (try
            (is (thrown-with-msg?
                 js/Error #"another writer holds an active Receipt River reservation"
                 (cli/open-append-reservation! second-segment true)))
            (is (not (fs/existsSync second-segment))
                "the refused writer reserves nothing")
            (finally
              (cli/close-append-reservation! active)))
          (is (= [] (lock-files segments)))
          (is (= [] (segment-names segments)))
          (is (not (fs/existsSync (path/join segments cli/receipt-writer-lock-name))))
          (cli/close-append-reservation!
           (cli/open-append-reservation! second-segment true))
          (is (= [] (segment-names segments))))))))

(deftest quarantine-outlives-the-writer-lock
  ;; A quarantine is the retained per-segment lock; the writer lock itself is
  ;; released, so the next writer reports the quarantine, not an active writer.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (fs/writeFileSync file "" "utf8")
      (with-redefs [cli/receipt-file file]
        (run-gate-against-reserved-segment!
         segments file
         (fn [{:keys [segment renamed forged-line]}]
           (fs/renameSync segment renamed)
           (fs/writeFileSync renamed (str forged-line "\n") "utf8")))
        (is (not (fs/existsSync (path/join segments cli/receipt-writer-lock-name))))
        (with-redefs [cli/git-capture! (fn [_] reviewed-root-revision)
                      cli/read-immutable-receipt-ledger! empty-committed-ledger
                      cli/read-immutable-receipt-segment-entries! (fn [_] [])]
          (is (thrown-with-msg?
               js/Error #"retained append lock quarantines"
               (cli/open-append-reservation!
                (path/join segments "2026-10-13T000002000Z-c.edn") true))))))))

(deftest working-ledger-files-are-read-through-the-validated-descriptor
  (with-receipt-fixture
    (fn [{:keys [segments]}]
      (let [segment (path/join segments "2026-10-13T000000000Z-a.edn")
            copy (path/join segments "copy")
            line "{:kind :observation}\n"
            original-lstat cli/lstat-if-present!]
        (fs/writeFileSync segment line "utf8")
        (fs/writeFileSync copy line "utf8")
        (with-redefs [cli/lstat-if-present!
                      (fn [file description]
                        (let [stats (original-lstat file description)]
                          (when (= file segment)
                            ;; Swap the validated file for a symlink to a
                            ;; byte-identical copy before it is read.
                            (fs/unlinkSync segment)
                            (fs/symlinkSync copy segment))
                          stats))]
          (is (thrown-with-msg?
               js/Error #"Receipt River segment"
               (cli/read-working-regular-file! segment "Receipt River segment"))))))))

(deftest receipt-lines-require-one-complete-edn-form
  (is (= [{:a 1}] (cli/read-receipt-records! "{:a 1}\n")))
  (is (thrown-with-msg?
       js/Error #"exactly one EDN form"
       (cli/read-receipt-records! "{:a 1} {:b 2}\n")))
  (is (thrown-with-msg?
       js/Error #"exactly one EDN form"
       (cli/read-receipt-records! "{:a 1} trailing\n"))))

(deftest receipt-numbers-must-round-trip-exactly
  ;; cljs.reader turns every EDN number into a JS double before record IDs
  ;; are hashed, so distinct lexemes such as 9007199254740992 and
  ;; 9007199254740993 would share one identity. Only numbers whose exact
  ;; value survives reading are accepted.
  (is (= [{:n 9007199254740991 :m -9007199254740991}]
         (cli/read-receipt-records!
          "{:n 9007199254740991 :m -9007199254740991}\n")))
  (is (= [{:score 0.2 :other 0.9 :tiny 1.5e-7}]
         (cli/read-receipt-records! "{:score 0.20 :other 0.9 :tiny 1.5e-7}\n")))
  (is (= [{:text "9007199254740993 0.10000000000000001" :k :a9007199254740993
           :c \9}]
         (cli/read-receipt-records!
          "{:text \"9007199254740993 0.10000000000000001\" :k :a9007199254740993 :c \\9}\n"))
      "digits inside strings, keywords and characters are not numbers")
  (doseq [lexeme ["9007199254740992" "9007199254740993" "-9007199254740993"
                  "0.10000000000000001" "1.0" "2e3" "1e400" "1N" "1.5M"
                  "1/2" "0x10" "017"]]
    (is (thrown-with-msg?
         js/Error #"must round-trip exactly"
         (cli/read-receipt-records! (str "{:n " lexeme "}\n")))
        lexeme))
  (doseq [lexeme ["##Inf" "##-Inf" "##NaN"]]
    (is (thrown-with-msg?
         js/Error #"must round-trip exactly"
         (cli/read-receipt-records! (str "{:n " lexeme "}\n")))
        (str "symbolic non-finite " lexeme)))
  (is (thrown-with-msg?
       js/Error #"must round-trip exactly"
       (cli/encode-edn-line! {:n 9007199254740992}))
      "the writer refuses a receipt its readers would reject")
  (is (not= (cli/record-id {:n 1}) (cli/record-id {:n 2}))))

(deftest receipt-tagged-instants-must-round-trip-exactly
  ;; cljs.reader truncates #inst fractions to milliseconds, so .0001Z and
  ;; .0002Z would share one record ID. Reject tagged instants that cannot
  ;; round-trip before deriving IDs.
  (doseq [text ["{:ts #inst \"2026-01-01T00:00:00.0001Z\"}\n"
                "{:ts #inst \"2026-01-01T00:00:00.12345Z\"}\n"
                "{:ts #inst,\"2026-01-01T00:00:00.0001Z\"}\n"
                "{:ts #inst , \"2026-01-01T00:00:00.0002Z\"}\n"
                "{:ts #inst #_ nil \"2026-01-01T00:00:00.0001Z\"}\n"
                "{:ts #inst #_ 0 \"2026-01-01T00:00:00.0002Z\"}\n"
                "{:ts #inst #_ {:a [1 2]} \"2026-01-01T00:00:00.0001Z\"}\n"]]
    (is (thrown-with-msg?
         js/Error #"must round-trip exactly"
         (cli/read-receipt-records! text))
        (pr-str text)))
  (is (= [{:ts (cli/read-single-edn!
                "#inst \"2026-01-01T00:00:00.001Z\"" "inst")}]
         (cli/read-receipt-records!
          "{:ts #inst \"2026-01-01T00:00:00.001Z\"}\n"))
      "millisecond precision remains accepted"))

(deftest receipt-inst-text-inside-strings-is-not-a-tag
  ;; The exactness check lexes EDN like the number check: `#inst` text that
  ;; ends a string value is not a tag, so it must not pair the string's
  ;; closing quote with the next quoted span. Real tags are still checked.
  (doseq [record [{:note "round-trips as #inst" :pi "eta-mu"}
                  {:note "a #inst\"b\" c" :pi "eta-mu"}
                  {:note "#inst" :dod "x" :ts "2026-01-01T00:00:00Z"}]]
    (let [text (str (pr-str record) "\n")]
      (is (= [record] (cli/read-receipt-records! text)) (pr-str text))))
  (is (= [{:note "#inst"
           :ts (cli/read-single-edn! "#inst \"2026-01-01T00:00:00.001Z\"" "inst")}]
         (cli/read-receipt-records!
          "{:note \"#inst\" :ts #inst \"2026-01-01T00:00:00.001Z\"}\n")))
  (is (thrown-with-msg?
       js/Error #"must round-trip exactly"
       (cli/read-receipt-records!
        "{:note \"#inst\" :ts #inst \"2026-01-01T00:00:00.0001Z\"}\n"))
      "a real tag after a string mentioning #inst is still checked")
  (is (= ["2026-01-01T00:00:00Z"]
         (cli/edn-inst-strings
          "{:a \"#inst\" ; #inst \"x\"\n :b #inst,\"2026-01-01T00:00:00Z\"}")))
  (is (= ["2026-01-01T00:00:00.0001Z"]
         (cli/edn-inst-strings
          "{:ts #inst #_ nil \"2026-01-01T00:00:00.0001Z\"}"))
      "reader-discard forms between #inst and its string are skipped")
  (is (= ["2026-01-01T00:00:00.0001Z"]
         (cli/edn-inst-strings
          "{:ts #inst #_ {:a [1]} \"2026-01-01T00:00:00.0001Z\"}"))
      "discarded collections between #inst and its string are skipped"))

(deftest encode-edn-line-counts-the-segment-newline
  ;; Immutable verification measures the complete segment blob (record +
  ;; newline). The writer must use the same bound so a receipt it emits can
  ;; still pass verify-receipts after commit.
  (let [target cli/max-receipt-line-bytes
        prefix "{:pad \""
        suffix "\"}"
        pad (- target (count prefix) (count suffix))
        record {:pad (apply str (repeat pad "x"))}
        line (pr-str record)]
    (is (= target (.-length line)))
    (is (thrown-with-msg?
         js/Error #"exceeds"
         (cli/encode-edn-line! record))
        "exact max-length text leaves no room for the mandatory newline")
    (let [ok {:pad (apply str (repeat (dec pad) "x"))}]
      (is (string? (cli/encode-edn-line! ok))
          "one byte under the bound still encodes"))))

(deftest working-segment-reader-enforces-the-complete-segment-size-bound
  ;; Immutable batch reads reject blobs over max-receipt-line-bytes. The
  ;; working reader must apply the same complete-segment bound so an
  ;; externally added oversized segment cannot be approved then fail after
  ;; commit.
  (let [n (inc cli/max-receipt-line-bytes)
        body (str (apply str (repeat (dec n) "x")) "\n")]
    (is (thrown-with-msg?
         js/Error (re-pattern (str "exceeds " cli/max-receipt-line-bytes))
         (cli/read-segment-records! "s.edn" (js/Buffer.from body))))))

(deftest working-segment-size-is-checked-before-reading
  ;; A huge or sparse well-named working segment must fail closed with the
  ;; documented size error from the held descriptor's size, before the
  ;; reader allocates the file (3 GiB is past Node's whole-file read limit).
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (fs/writeFileSync file "" "utf8")
      (let [big (path/join segments "2026-10-14T000000000Z-sparse.edn")
            fd (fs/openSync big "w")]
        (try
          (fs/ftruncateSync fd (* 3 1024 1024 1024))
          (finally (fs/closeSync fd)))
        (with-redefs [cli/receipt-file file]
          (is (thrown-with-msg?
               js/Error (re-pattern (str "exceeds " cli/max-receipt-line-bytes
                                         " bytes"))
               (cli/read-working-receipt-segments! nil))))))))

(deftest gate-descendants-are-killed-before-the-gate-result-returns
  ;; A gate can leave a detached child waiting for the locks to disappear.
  ;; The gate runs as its own process group, which is killed and drained
  ;; before the result (and so the receipt write and lock release) proceeds.
  (let [directory (fs/mkdtempSync (path/join (os/tmpdir) "foresight-gate-"))
        pid-file (path/join directory "child.pid")]
    (try
      (let [result (cli/spawn-result
                    directory
                    ["sh" "-c" (str "sleep 30 >/dev/null 2>&1 & echo $! > "
                                    pid-file)])
            child (js/parseInt (str/trim (fs/readFileSync pid-file "utf8")) 10)]
        (is (= 0 (.-status result)))
        (is (pos? child))
        (is (not (try (js/process.kill child 0) true
                      (catch :default _ false)))
            "the backgrounded descendant no longer exists")
        (try (js/process.kill child "SIGKILL") (catch :default _ nil)))
      (finally
        (fs/rmSync directory #js {:recursive true :force true})))))

(deftest working-legacy-ledger-is-bounded-by-its-committed-size
  ;; The frozen legacy file can only lose lines, so the working copy is
  ;; bounded by its committed size plus one segment of slack; a huge or
  ;; sparse replacement fails closed with the size error before allocation.
  (with-receipt-fixture
    (fn [{:keys [file]}]
      (let [fd (fs/openSync file "w")]
        (try
          (fs/ftruncateSync fd (* 3 1024 1024 1024))
          (finally (fs/closeSync fd))))
      (with-redefs [cli/receipt-file file
                    cli/current-committed-receipt-segments!
                    (fn [] [(cli/legacy-segment-entry (js/Buffer.alloc 0) [])])]
        (is (thrown-with-msg?
             js/Error
             (re-pattern (str "Receipt River file exceeds "
                              cli/max-receipt-line-bytes " bytes"))
             (cli/validate-working-receipt-ledger! nil)))))))

(deftest receipt-blob-chunks-are-linear-in-segment-count
  ;; Chunk construction must carry a running total; recomputing the sum of
  ;; the current chunk for every ID is quadratic and hangs large ledgers.
  (let [size (quot cli/max-receipt-batch-bytes 50)
        n 200
        ids (mapv #(str "oid-" %) (range n))
        sizes (into {} (map (fn [id] [id size]) ids))
        chunks (cli/receipt-blob-chunks ids sizes)]
    (is (= 4 (count chunks)) "four equal chunks fill the batch bound")
    (is (every? #(= 50 (count %)) chunks))))

(deftest receipt-segments-hold-exactly-one-physical-line
  ;; The segment contract is one physical EDN line; blank lines before or
  ;; after the record are not discarded silently. Embedded carriage returns
  ;; are physical breaks (encode-edn-line! already rejects them on writes),
  ;; but a single trailing CR is kept so a CRLF legacy row can migrate
  ;; byte-identical into a segment.
  (is (= [{:a 1}] (cli/read-segment-records! "s.edn" (js/Buffer.from "{:a 1}\n"))))
  (is (= [{:a 1}] (cli/read-segment-records! "s.edn" (js/Buffer.from "{:a 1}\r\n")))
      "CRLF-terminated legacy rows migrate intact")
  (doseq [text ["\n{:a 1}\n" "{:a 1}\n\n" "{:a 1}\n \n"
                "{:a\r1}\n" "\r{:a 1}\n" "{:a 1}\r\r\n"]]
    (is (thrown-with-msg?
         js/Error #"exactly one physical line"
         (cli/read-segment-records! "s.edn" (js/Buffer.from text)))
        (pr-str text))))

(deftest working-readme-must-be-a-regular-file
  ;; Immutable review accepts the README only as a 100644 blob, so the working
  ;; ledger rejects a directory (or other non-regular entry) at that path.
  (with-receipt-fixture
    (fn [{:keys [segments file]}]
      (fs/writeFileSync file "" "utf8")
      ;; A directory without execute bits passes an executable-bit check.
      (fs/mkdirSync (path/join segments "README.md"))
      (fs/chmodSync (path/join segments "README.md") 420)
      (with-redefs [cli/receipt-file file]
        (is (thrown-with-msg?
             js/Error #"README must be a regular file"
             (cli/read-working-receipt-segments! "x.edn")))))))

(deftest immutable-catalog-requires-one-complete-edn-form
  (let [contents "{:catalog/version 1}\n"
        calls (atom [])]
    (with-redefs [cli/git-capture!
                  (fn [args]
                    (swap! calls conj args)
                    (if (= "cat-file" (first args))
                      "commit\n"
                      (regular-blob-entry "config/quality-gates.edn")))
                  cli/git-buffer!
                  (fn [args]
                    (swap! calls conj args)
                    (js/Buffer.from contents "utf8"))]
      (let [bundle (cli/read-immutable-catalog-bundle!
                    reviewed-root-revision)]
        (is (= {:catalog/version 1} (:catalog bundle)))
        (is (= (cli/sha256 contents)
               (get-in bundle [:catalog-identity :catalog/sha256])))
        (is (= [["cat-file" "-t" reviewed-root-revision]
                ["ls-tree" "-z" reviewed-root-revision "--"
                 "config/quality-gates.edn"]
                ["show" (str reviewed-root-revision
                             ":config/quality-gates.edn")]]
               @calls))))
    (with-redefs [cli/git-capture!
                  (fn [args]
                    (if (= "cat-file" (first args))
                      "commit\n"
                      (regular-blob-entry "config/quality-gates.edn")))
                  cli/git-buffer!
                  (fn [_]
                    (js/Buffer.from
                     "{:catalog/version 1} {:forged true}\n" "utf8"))]
      (is (thrown-with-msg?
           js/Error #"exactly one EDN form"
           (cli/read-immutable-catalog-bundle!
            reviewed-root-revision))))))

(deftest immutable-git-bytes-are-hashed-raw-and-decoded-strictly
  (let [invalid-bytes (js/Buffer.from #js [255])]
    (is (= "a8100ae6aa1940d0b663bb31cd466142ebbdbd5187131b92d93818987832eb89"
           (cli/sha256 invalid-bytes)))
    (is (not= (cli/sha256 invalid-bytes)
              (cli/sha256 (js/Buffer.from "�" "utf8"))))
    (with-redefs [cli/git-capture!
                  (fn [args]
                    (if (= "cat-file" (first args))
                      "commit\n"
                      (regular-blob-entry
                       (if (= "ledger" (last args))
                         "ledger"
                         (last args)))))
                  cli/git-buffer! (fn [_] invalid-bytes)]
      (is (thrown-with-msg?
           js/Error #"Invalid UTF-8 in immutable Receipt River ledger"
           (cli/read-immutable-receipt-ledger!
            reviewed-root-revision)))
      (is (thrown-with-msg?
           js/Error #"Invalid UTF-8 in immutable quality gate catalog"
           (cli/read-immutable-catalog-bundle!
            reviewed-root-revision))))))

(deftest immutable-authority-files-must-be-regular-git-blobs
  (with-redefs [cli/git-capture!
                (fn [args]
                  (if (= "cat-file" (first args))
                    "commit\n"
                    (str "120000 blob " blob-revision "\t"
                         law/receipt-ledger-path "\u0000")))
                cli/git-buffer!
                (fn [_]
                  (throw (js/Error. "blob read must not be reached")))]
    (is (thrown-with-msg?
         js/Error #"non-executable regular Git blob"
         (cli/read-immutable-receipt-ledger!
          reviewed-root-revision)))))

(deftest reads-receipts-only-from-an-immutable-git-object
  (let [revision (apply str (repeat 40 "c"))
        receipt (cli/evidence-receipt
                 passed-result "2026-08-29T17:22:40Z" "test"
                 "nbb/node@test")
        contents (str (pr-str receipt) "\n")
        calls (atom [])]
    (with-redefs [cli/git-capture!
                  (fn [args]
                    (swap! calls conj args)
                    (if (= "cat-file" (first args))
                      "commit\n"
                      (regular-blob-entry law/receipt-ledger-path)))
                  cli/git-buffer!
                  (fn [args]
                    (swap! calls conj args)
                    (js/Buffer.from contents "utf8"))]
      (let [ledger (cli/read-immutable-receipt-ledger! revision)]
        (is (= [["cat-file" "-t" revision]
                ["ls-tree" "-z" revision "--" law/receipt-ledger-path]
                ["show" (str revision ":" law/receipt-ledger-path)]]
               @calls))
        (is (= {:ledger/path law/receipt-ledger-path
                :ledger/revision revision
                :ledger/sha256 (cli/sha256 contents)}
               (:ledger/identity ledger)))
        (is (= [receipt] (:ledger/records ledger)))
        (is (law/immutable-receipt-ledger? ledger))))
    (is (thrown-with-msg?
         js/Error #"full lowercase Git commit ID"
         (cli/read-immutable-receipt-ledger! "main")))))

(deftest receipt-river-head-preserves-base-bytes-and-whole-lines
  (let [base (js/Buffer.from "{:a 1}\n" "utf8")
        appended (js/Buffer.from "{:b 2}\n" "utf8")
        head (js/Buffer.concat #js [base appended])]
    (is (= [{:b 2}] (cli/appended-receipt-records! base head)))
    (is (= [] (cli/appended-receipt-records! base base)))
    (is (thrown-with-msg?
         js/Error #"does not preserve the base bytes as a prefix"
         (cli/appended-receipt-records!
          (js/Buffer.from "{:a 0}\n" "utf8") head)))
    (is (thrown-with-msg?
         js/Error #"must end with a newline"
         (cli/appended-receipt-records!
          base (js/Buffer.concat
                #js [base (js/Buffer.from "{:b 2}" "utf8")]))))
    (is (= [{:b 2}]
           (cli/appended-receipt-records!
            (js/Buffer.from "{:a 1}" "utf8")
            (js/Buffer.from "{:a 1}\n{:b 2}\n" "utf8"))))))

(deftest receipt-verification-rejects-invalid-appended-envelopes
  (let [base-record {:ts "2026-08-29T17:22:40Z"
                     :kind :decision
                     :repo "."
                     :origin "test"
                     :owner "test"
                     :dod "retain"
                     :pi "eta-mu"
                     :host "test"
                     :manifest ["test"]
                     :refs ["test"]}
        base-text (str (pr-str base-record) "\n")
        invalid-record (dissoc base-record :origin)
        ledger (fn [revision text records]
                 {:ledger/identity
                  {:ledger/path law/receipt-ledger-path
                   :ledger/revision revision
                   :ledger/sha256 (cli/sha256
                                   (js/Buffer.from text "utf8"))}
                  :ledger/bytes (js/Buffer.from text "utf8")
                  :ledger/records records})]
    (with-redefs [cli/read-immutable-receipt-ledger!
                  (fn [revision] (ledger revision base-text [base-record]))
                  cli/read-immutable-receipt-segment-entries!
                  (fn [revision]
                    (if (= revision child-revision)
                      []
                      [(cli/segment-entry
                        ".ημ/receipts/2026-08-29T172241Z-x.edn"
                        (js/Buffer.from (str (pr-str invalid-record) "\n")
                                        "utf8"))]))]
      (is (thrown-with-msg?
           js/Error #"invalid receipt envelopes"
           (cli/verify-receipts!
            {:base child-revision :at reviewed-root-revision})))))
  (let [base (js/Buffer.from "" "utf8")
        head (js/Buffer.from "42\n" "utf8")]
    (is (= [42] (cli/appended-receipt-records! base head)))))

(defn with-historical-ledger-fixture [terminal-newline? run!]
  (with-receipt-fixture
    (fn [{:keys [fixture file segments] :as context}]
      (with-redefs [cli/root fixture
                    cli/receipt-file file]
        (cli/git-capture! ["init" "--quiet"])
        (let [current (cli/evidence-receipt
                       passed-result "2026-08-29T17:22:40Z" "test"
                       "nbb/node@test")
              historical (-> current
                             (dissoc :evidence/schema :evidence/adapter)
                             (assoc :manifest "historical" :refs "none"))
              base-text (str (pr-str historical)
                             (when terminal-newline? "\n"))
              segment-path (fn [name] (path/join segments name))
              ;; contents: legacy text, plus an optional map of segment
              ;; basename -> text. The working tree is made to match exactly.
              commit! (fn commit-fixture!
                        ([contents] (commit-fixture! contents {}))
                        ([contents segment-files]
                         (fs/writeFileSync file contents)
                         (doseq [name (segment-names segments)]
                           (when-not (contains? segment-files name)
                             (fs/unlinkSync (segment-path name))))
                         (doseq [[name text] segment-files]
                           (fs/writeFileSync (segment-path name) text))
                         (cli/git-capture! ["add" "-A" "--" ".ημ"])
                         (cli/git-capture!
                          ["-c" "user.name=Receipt Test"
                           "-c" "user.email=receipt-test@example.invalid"
                           "-c" "commit.gpgsign=false"
                           "commit" "--quiet" "--allow-empty" "-m" "fixture"])
                         (str/trim
                          (cli/git-capture! ["rev-parse" "HEAD"]))))
              _ (fs/writeFileSync (path/join segments "README.md") "segments\n")
              base (commit! base-text)]
          (run! (assoc context
                       :current current :historical historical
                       :base-text base-text :base base :commit! commit!)))))))

(deftest receipt-verification-accepts-new-segments-over-frozen-history
  (doseq [terminal-newline? [true false]]
    (with-historical-ledger-fixture terminal-newline?
      (fn [{:keys [base base-text historical current commit!]}]
        (is (not (law/receipt-envelope? historical)))
        (is (zero? (cli/verify-receipts! {:base base :at base})))
        (let [head (commit! base-text
                            {"2026-08-29T172240Z-a.edn"
                             (str (pr-str current) "\n")})
              output (with-out-str
                       (is (zero? (cli/verify-receipts!
                                   {:base base :at head}))))]
          (is (str/includes? output ":total-receipts 2"))
          (is (str/includes? output ":appended-receipts 1"))
          (is (str/includes? output ":appended-evidence-receipts 1"))
          (is (str/includes? output ":legacy-evidence-receipts 1"))
          (is (str/includes? output ":segments 2"))
          (is (.equals (js/Buffer.from base-text "utf8")
                       (:ledger/bytes
                        (cli/read-immutable-receipt-ledger! base)))))))))

(deftest immutable-segments-are-read-in-one-git-process
  ;; Every receipt is its own segment and the committed ledger is read
  ;; several times per gate, so one `git show` per segment grows the process
  ;; count with the ledger. Segment blobs are read in one batched process.
  (with-historical-ledger-fixture true
    (fn [{:keys [base-text current commit!]}]
      (let [files (into (sorted-map)
                        (for [i (range 3)]
                          [(str "2026-10-14T00000" i "Z-s" i ".edn")
                           (str (pr-str (assoc current
                                               :ts (str "2026-10-14T00:00:0" i "Z")
                                               :host (str "h" i)))
                                "\n")]))
            head (commit! base-text files)
            shows (atom [])
            git-buffer! cli/git-buffer!]
        (with-redefs [cli/git-buffer!
                      (fn [args]
                        (when (= "show" (first args))
                          (swap! shows conj (second args)))
                        (git-buffer! args))]
          (let [stored (cli/read-immutable-receipt-segments! head)]
            (is (= (mapv #(str law/receipt-segment-directory "/" %) (keys files))
                   (vec (keys stored))))
            (is (= (vec (vals files))
                   (mapv #(.toString % "utf8") (vals stored))))
            (is (= [] (filterv #(str/includes? % (str law/receipt-segment-directory "/"))
                               @shows))
                "no per-segment git show")))))))

(deftest immutable-ledger-parses-each-chunk-and-drops-segment-bytes
  ;; Chunking the cat-file subprocess does not bound memory if every chunk's
  ;; blobs are merged into one lifetime map. Each blob is parsed while its
  ;; chunk is current, and only hashes and records are retained.
  (with-historical-ledger-fixture true
    (fn [{:keys [base-text current commit!]}]
      (let [files (into (sorted-map)
                        (map (fn [n]
                               [(str "2026-10-14T00000" n "000Z-c" n ".edn")
                                (str (pr-str (assoc current :n n)) "\n")]))
                        (range 3))
            head (commit! base-text files)
            chunk-sizes (atom [])
            chunk! cli/git-blobs-batch-chunk!]
        (with-redefs [cli/max-receipt-batch-bytes 1
                      cli/git-blobs-batch-chunk!
                      (fn [& args]
                        (swap! chunk-sizes conj (count (first args)))
                        (apply chunk! args))]
          (let [ledger (cli/read-immutable-merged-ledger! head)
                stored (remove :segment/legacy? (:ledger/segments ledger))]
            (is (= [1 1 1] @chunk-sizes) "one blob per chunk")
            (is (= 3 (count stored)))
            (is (every? #(not (contains? % :segment/bytes)) stored)
                "segment bytes are not retained")
            (is (= (mapv #(cli/sha256 (js/Buffer.from % "utf8")) (vals files))
                   (mapv :segment/sha256 stored)))
            (is (= (mapv #(str law/receipt-segment-directory "/" %) (keys files))
                   (mapv :segment/path stored)))))))))

(deftest git-blobs-batch-rejects-oversized-segment-blobs
  ;; Writer path caps a receipt at max-receipt-line-bytes. Immutable
  ;; cat-file --batch must apply the same bound (and an aggregate cap)
  ;; before buffering up to 1 GiB, so a hostile revision fails closed on a
  ;; typical CI worker instead of exhausting memory.
  (with-historical-ledger-fixture true
    (fn [{:keys [base-text commit!]}]
      (let [n (inc cli/max-receipt-line-bytes)
            body (str (apply str (repeat (dec n) "x")) "\n")
            head (commit! base-text {"2026-10-14T000000000Z-big.edn" body})]
        (is (thrown-with-msg?
             js/Error (re-pattern (str "exceeds " cli/max-receipt-line-bytes))
             (cli/read-immutable-receipt-segments! head)))))))

(deftest receipt-verification-exempts-historical-timestamps
  ;; The canonical-instant rule binds new records only; retained history with
  ;; an earlier free-form :ts still verifies against itself and its extensions.
  (with-historical-ledger-fixture true
    (fn [{:keys [historical current commit!]}]
      (let [legacy-text (str (pr-str (assoc historical :ts "yesterday")) "\n")
            base (commit! legacy-text)
            _ (is (zero? (cli/verify-receipts! {:base base :at base})))
            head (commit! legacy-text
                          {"2026-08-29T172240Z-a.edn"
                           (str (pr-str current) "\n")})
            _ (with-out-str
                (is (zero? (cli/verify-receipts! {:base base :at head}))))
            invalid (commit! legacy-text
                             {"2026-08-29T172240Z-a.edn"
                              (str (pr-str (assoc current :ts
                                                  "2026-02-30T12:00:00Z"))
                                   "\n")})]
        (is (thrown-with-msg?
             js/Error #"invalid receipt envelopes"
             (cli/verify-receipts! {:base base :at invalid})))))))

(deftest merged-ledger-orders-by-timestamp-then-record-id
  (let [a {:ts "2026-01-02T00:00:00Z" :n 1}
        b {:ts "2026-01-01T00:00:00Z" :n 2}
        c {:ts "2026-01-02T00:00:00Z" :n 3}
        segments [{:segment/path "legacy" :segment/legacy? true
                   :segment/records [a]}
                  {:segment/path "s1" :segment/records [c]}
                  {:segment/path "s2" :segment/records [b]}]
        merged (cli/merged-receipt-records! segments)]
    (is (= b (first merged)))
    (is (= (sort-by cli/record-id [a c]) (rest merged)))
    (is (= merged (cli/merged-receipt-records! (reverse segments))))
    (is (thrown-with-msg?
         js/Error #"repeats a record"
         (cli/merged-receipt-records!
          (conj segments {:segment/path "s3" :segment/records [a]}))))))

(deftest record-ids-ignore-map-and-set-iteration-order
  (let [a (cljs.reader/read-string "{:ts \"t\" :b 2 :a 1 :m {:y 2 :x 1} :s #{3 1 2}}")
        b (cljs.reader/read-string "{:s #{2 3 1} :m {:x 1 :y 2} :a 1 :b 2 :ts \"t\"}")]
    (is (= a b))
    (is (= (cli/record-id a) (cli/record-id b)))
    (is (not= (cli/record-id a) (cli/record-id (assoc a :a 9))))
    (is (not= (cli/record-id {:manifest ["x"]})
              (cli/record-id {:manifest '("x")})))
    (let [q (cljs.reader/read-string "#queue [1 2]")
          lst (cljs.reader/read-string "(1 2)")
          vec* (cljs.reader/read-string "[1 2]")]
      (is (= q lst vec*) "queues, lists and vectors compare equal by value")
      (is (not= (cli/record-id {:manifest q})
                (cli/record-id {:manifest lst}))
          "tagged queues must not share a record ID with lists")
      (is (not= (cli/record-id {:manifest q})
                (cli/record-id {:manifest vec*}))
          "tagged queues must not share a record ID with vectors"))
    (is (thrown-with-msg?
         js/Error #"repeats a record"
         (cli/merged-receipt-records!
          [{:segment/path "s1" :segment/records [a]}
           {:segment/path "s2" :segment/records [b]}])))))

(deftest receipt-segment-names-carry-a-timestamp-and-id
  (doseq [name ["2026-10-13T164500123Z-3f9a1c2b7d4e.edn"
                "2026-10-02T064007673951Z-dev-origin-map.edn"
                "2026-08-29T172240Z-a.edn"]]
    (is (cli/receipt-segment-name? name) name))
  (doseq [name ["receipt.edn" "a.edn" "2026-10-13-x.edn"
                "2026-10-13T1645Z-x.edn" "2026-10-13T164500Z-X.edn"
                "2026-10-13T164500Z-.edn" "2026-10-13T164500Z-x.txt"
                ;; Well-shaped digits that are not a real UTC date or time.
                "2026-99-99T999999Z-a.edn" "2026-02-30T120000Z-a.edn"
                "2025-02-29T120000Z-a.edn" "2026-00-10T120000Z-a.edn"
                "2026-10-13T240000Z-a.edn" "2026-10-13T126000Z-a.edn"
                "2026-10-13T120060Z-a.edn"]]
    (is (not (cli/receipt-segment-name? name)) name))
  (is (cli/receipt-segment-name? "2024-02-29T235959999Z-leap.edn"))
  ;; Two-digit years are real years, not 1900-based Date.UTC offsets.
  (is (cli/receipt-segment-name? "0050-01-01T000000Z-a.edn"))
  (is (cli/receipt-segment-name? "0099-12-31T235959Z-a.edn"))
  (is (not (cli/receipt-segment-name? "0050-02-29T000000Z-a.edn")))
  (is (cli/receipt-segment-name?
       (path/basename (cli/new-receipt-segment-file)))))

(deftest aborted-or-overtaken-gates-leave-no-segment
  ;; A gate that only throws leaves no empty reserved segment and no lock.
  ;; Editing the ledger, or forging a segment and then throwing or returning,
  ;; quarantines (retains a lock) so a forged segment cannot become an
  ;; ordinary extension after cleanup.
  (doseq [[run-gate message expect-lock?]
          [[(fn [_] (throw (js/Error. "gate exploded"))) #"gate exploded" false]
           [(fn [{:keys [file]}]
              (fs/writeFileSync file "{:kind :observation}\n" "utf8")
              passed-result)
            #"does not preserve the committed ledger" true]
           [(fn [{:keys [segments]}]
              (fs/writeFileSync (path/join segments "2026-10-13T000001000Z-forged.edn")
                                "{:kind :observation :origin :test :owner \"t\" :ts \"2026-10-13T00:00:01.000Z\" :dod \"d\" :pi \"p\" :host \"h\" :manifest {} :refs []}\n"
                                "utf8")
              (fs/writeFileSync (path/join segments "notes.txt") "x\n" "utf8")
              (throw (js/Error. "gate exploded after forge")))
            #"gate exploded after forge" true]
           [(fn [{:keys [segments]}]
              ;; Schema-valid forged segment plus a stray file: revalidation
              ;; throws, and quarantine must retain the lock.
              (fs/writeFileSync (path/join segments "2026-10-13T000001000Z-forged.edn")
                                "{:kind :observation :origin :test :owner \"t\" :ts \"2026-10-13T00:00:01.000Z\" :dod \"d\" :pi \"p\" :host \"h\" :manifest {} :refs []}\n"
                                "utf8")
              (fs/writeFileSync (path/join segments "notes.txt") "x\n" "utf8")
              passed-result)
            #".*" true]]]
    (with-receipt-fixture
      (fn [{:keys [segments file] :as context}]
        (fs/writeFileSync file "" "utf8")
        (is (thrown-with-msg?
             js/Error message
             (run-one-gate! {:file file
                             :repositories {"repo" {}}
                             :run-gate (fn [& _] (run-gate context))})))
        (if expect-lock?
          (is (seq (lock-files segments))
              "ledger change while gate ran must quarantine")
          (do (is (= [] (segment-names segments)))
              (is (= [] (lock-files segments)))))))))

(deftest receipt-verification-keeps-the-segment-directory
  (with-historical-ledger-fixture true
    (fn [{:keys [base base-text segments commit!]}]
      (fs/unlinkSync (path/join segments "README.md"))
      (is (thrown-with-msg?
           js/Error #"segment directory must remain"
           (cli/verify-receipts! {:base base :at (commit! base-text)}))))))

(deftest evidence-append-rejects-a-duplicate-candidate
  (with-historical-ledger-fixture true
    (fn [{:keys [current segments]}]
      (with-redefs [cli/validated-evidence-receipt! (fn [_] current)]
        (is (= current (cli/append-evidence-receipt! passed-result)))
        (is (thrown-with-msg?
             js/Error #"repeats a record already in the ledger"
             (cli/append-evidence-receipt! passed-result)))
        (is (= 1 (count (segment-names segments))))
        (is (= [] (lock-files segments)))))))

(deftest receipt-verification-reconciles-a-diverged-fork-and-org
  ;; The org and a fork share history H, then each records its own receipt.
  ;; Before segmentation both sides appended to the same file, so no ordering
  ;; could be a byte prefix of both. Here the fork moves its fork-only legacy
  ;; row into a segment and the legacy file returns to the org bytes; the
  ;; result verifies against BOTH bases, and later syncs merge cleanly.
  (with-historical-ledger-fixture true
    (fn [{:keys [base base-text current commit!]}]
      (let [org-row (assoc current :ts "2026-10-02T00:00:00Z" :host "org")
            fork-row (assoc current :ts "2026-10-02T00:00:01Z" :host "fork")
            org-text (str base-text (pr-str org-row) "\n")
            ;; Legacy history from before the freeze; tolerated at base only.
            org-main (commit! org-text)
            fork-main (commit! (str base-text (pr-str fork-row) "\n"
                                    (pr-str org-row) "\n"))
            reconciled (commit! org-text
                                {"2026-10-02T000001Z-fork.edn"
                                 (str (pr-str fork-row) "\n")})
            org-segment (str (pr-str (assoc current :ts "2026-10-14T00:00:00Z"
                                            :host "org-later"))
                             "\n")
            synced (commit! org-text
                            {"2026-10-02T000001Z-fork.edn"
                             (str (pr-str fork-row) "\n")
                             "2026-10-14T000000Z-org.edn" org-segment})]
        (is (zero? (cli/verify-receipts! {:base org-main :at reconciled})))
        (is (zero? (cli/verify-receipts! {:base fork-main :at reconciled})))
        (is (zero? (cli/verify-receipts! {:base reconciled :at synced})))
        (is (not= (get-in (cli/read-immutable-merged-ledger! reconciled)
                          [:ledger/identity :ledger/sha256])
                  (get-in (cli/read-immutable-merged-ledger! synced)
                          [:ledger/identity :ledger/sha256])))
        (let [records (:ledger/records
                       (cli/read-immutable-merged-ledger! synced))]
          (is (= ["2026-08-29T17:22:40Z" "2026-10-02T00:00:00Z"
                  "2026-10-02T00:00:01Z" "2026-10-14T00:00:00Z"]
                 (mapv :ts records))))
        ;; Dropping the fork row, or keeping it in both places, is rejected.
        (is (thrown-with-msg?
             js/Error #"drops 1 base record"
             (cli/verify-receipts!
              {:base fork-main :at (commit! org-text)})))
        (is (thrown-with-msg?
             js/Error #"repeats a record"
             (cli/verify-receipts!
              {:base fork-main
               :at (commit! (str base-text (pr-str fork-row) "\n"
                                 (pr-str org-row) "\n")
                            {"2026-10-02T000001Z-fork.edn"
                             (str (pr-str fork-row) "\n")})})))))))

(deftest receipt-verification-rejects-invalid-segments-after-history
  (with-historical-ledger-fixture true
    (fn [{:keys [base base-text historical current commit!]}]
      (doseq [[segment message]
              [[(str (pr-str historical) "\n") #"repeats a record"]
               ["42\n" #"invalid receipt envelopes"]
               [(str (pr-str (dissoc current :origin)) "\n")
                #"invalid receipt envelopes"]
               [(str (pr-str (assoc-in current [:evidence/result :result/exit] 1))
                     "\n") #"invalid evidence receipts"]
               ["{:kind\n" #"Invalid Receipt River EDN"]
               [(str (pr-str current) " 42\n") #"exactly one EDN form"]
               [(pr-str current) #"must end with a newline"]
               ["" #"must not be empty"]
               [(str (pr-str current) "\n"
                     (pr-str (assoc current :host "second")) "\n")
                #"exactly one record"]]]
        (let [head (commit! base-text {"2026-08-29T172240Z-a.edn" segment})]
          (is (thrown-with-msg?
               js/Error message
               (cli/verify-receipts! {:base base :at head}))))))))

(deftest receipt-verification-migrates-an-unterminated-final-legacy-row
  ;; A valid legacy ledger may lack a terminal newline. Its final row can only
  ;; be preserved as a segment by adding that one terminator, since every
  ;; segment must end with a newline.
  (with-historical-ledger-fixture true
    (fn [{:keys [historical commit!]}]
      (let [second (assoc historical :owner "second")
            first-line (str (pr-str historical) "\n")
            unterminated (str first-line (pr-str second))
            with-two (commit! unterminated)]
        (is (some? (cli/verify-receipts!
                    {:base with-two
                     :at (commit! first-line
                                  {"2026-08-29T172240Z-b.edn"
                                   (str (pr-str second) "\n")})}))
            "the final row moves into a newline-terminated segment")
        (commit! unterminated)
        (let [only (commit! (pr-str second))]
          (is (some? (cli/verify-receipts!
                      {:base only
                       :at (commit! ""
                                    {"2026-08-29T172240Z-b.edn"
                                     (str (pr-str second) "\n")})}))
              "a one-row unterminated legacy file can empty into a segment"))
        (commit! unterminated)
        ;; The terminator is the only allowed difference: a reserialized row
        ;; is still rejected.
        (is (thrown-with-msg?
             js/Error #"exact line is preserved as a segment"
             (cli/verify-receipts!
              {:base with-two
               :at (commit! first-line
                            {"2026-08-29T172240Z-b.edn"
                             (str (str/replace (pr-str second) "{" "{ ")
                                  "\n")})})))))))

(deftest receipt-verification-rejects-committed-operational-files
  ;; `.ημ/.receipt-river.quarantine` and the writer lock are working-tree
  ;; operational state. A reviewed revision that carries either one would
  ;; close every checkout's working ledger, so immutable review rejects it.
  (doseq [operational [".receipt-river.quarantine"
                       ".receipt-river.writer.lock"]]
    (with-historical-ledger-fixture true
      (fn [{:keys [directory base base-text commit!]}]
        (let [marker (path/join directory operational)]
          (fs/writeFileSync marker "quarantine pid=1\n")
          (let [head (commit! base-text)]
            (is (thrown-with-msg?
                 js/Error #"operational file must not be committed"
                 (cli/verify-receipts! {:base base :at head}))
                operational)
            (is (thrown-with-msg?
                 js/Error #"operational file must not be committed"
                 (cli/read-immutable-merged-ledger! head))
                operational)))))))

(deftest receipt-verification-rejects-historical-rewrite-and-truncation
  (with-historical-ledger-fixture true
    (fn [{:keys [base base-text historical current commit!]}]
      (doseq [head-text [""
                        (str (pr-str (assoc historical :owner "rewrite")) "\n")]]
        (let [head (commit! head-text)]
          (is (thrown-with-msg?
               js/Error #"drops 1 base record"
               (cli/verify-receipts! {:base base :at head})))))
      ;; Reordering or reserializing legacy lines is rejected even when every
      ;; record survives.
      (let [two (str base-text (pr-str (assoc historical :owner "second")) "\n")
            with-two (commit! two)]
        (is (thrown-with-msg?
             js/Error #"may only lose whole lines"
             (cli/verify-receipts!
              {:base with-two
               :at (commit! (str (pr-str (assoc historical :owner "second")) "\n"
                                 base-text))})))
        (is (thrown-with-msg?
             js/Error #"may only lose whole lines"
             (cli/verify-receipts!
              {:base with-two
               :at (commit! (str/replace two ", " " "))}))))
      (commit! base-text)
      ;; Stripping the final newline is a rewrite, not a whole-line loss.
      (let [with-newline (commit! base-text)]
        (is (thrown-with-msg?
             js/Error #"may only lose whole lines"
             (cli/verify-receipts!
              {:base with-newline
               :at (commit! (subs base-text 0 (dec (count base-text))))}))))
      (commit! base-text)
      ;; Moving a legacy line into a segment with a different serialization of
      ;; the same EDN value is rejected: IDs match via canonical form, but the
      ;; removed line's exact bytes must be the segment body.
      (let [second (assoc historical :owner "second")
            line (str (pr-str second) "\n")
            with-two (commit! (str base-text line))
            reserialized (str (str/replace (pr-str second) "{" "{ ") "\n")
            moved (commit! base-text
                           {"2026-08-29T172240Z-reserialize.edn" reserialized})]
        (is (not= line reserialized))
        (is (thrown-with-msg?
             js/Error #"exact line is preserved as a segment"
             (cli/verify-receipts!
              {:base with-two :at moved}))))
      (commit! base-text)
      ;; An unterminated base must still preserve terminators of retained lines.
      ;; Base "A\\nB" with head "A" and B moved into a segment deleted the
      ;; newline that ended retained record A — reject that as a rewrite.
      (let [second (assoc historical :owner "second")
            unterminated (str (pr-str historical) "\n" (pr-str second))
            with-two (commit! unterminated)
            moved (commit! (pr-str historical)
                           {"2026-08-29T172240Z-b.edn"
                            (str (pr-str second) "\n")})]
        (is (thrown-with-msg?
             js/Error #"may only lose whole lines"
             (cli/verify-receipts!
              {:base with-two :at moved}))))
      (commit! base-text)
      ;; The legacy file is frozen: appending to it is rejected.
      (let [head (commit! (str base-text (pr-str current) "\n"))]
        (is (thrown-with-msg?
             js/Error #"is frozen"
             (cli/verify-receipts! {:base base :at head}))))
      ;; Committed segments are immutable: no edits, no deletes.
      (let [segment (str (pr-str current) "\n")
            with-segment (commit! base-text {"2026-08-29T172240Z-a.edn" segment})
            edited (str (pr-str (assoc current :host "edited")) "\n")]
        (is (thrown-with-msg?
             js/Error #"segment was edited"
             (cli/verify-receipts!
              {:base with-segment
               :at (commit! base-text {"2026-08-29T172240Z-a.edn" edited})})))
        (is (thrown-with-msg?
             js/Error #"segment was removed"
             (cli/verify-receipts!
              {:base with-segment
               :at (commit! (str base-text segment))})))))))

(deftest evidence-append-preserves-history-and-validates-uncommitted-suffix
  (doseq [terminal-newline? [true false]]
    (with-historical-ledger-fixture terminal-newline?
      (fn [{:keys [base base-text file segments]}]
        ;; A second writer must accept the first writer's uncommitted segment.
        (let [first-receipt (cli/append-evidence-receipt! passed-result)
              second-receipt (cli/append-evidence-receipt!
                              (assoc passed-result :result/command ["second"]))]
          (is (= base-text (fs/readFileSync file "utf8")))
          (is (= 2 (count (segment-names segments))))
          (is (= #{first-receipt second-receipt}
                 (set (segment-records segments))))
          (is (= [] (lock-files segments))))))))

(deftest selected-gates-reject-invalid-extensions-of-historical-ledger
  (with-historical-ledger-fixture true
    (fn [{:keys [base-text historical current file segments]}]
      (doseq [[held-text segment message]
              [["" nil #"does not preserve the committed ledger"]
               [(str (pr-str (assoc historical :owner "rewrite")) "\n") nil
                #"does not preserve the committed ledger"]
               [(str base-text (pr-str current) "\n") nil #"is frozen"]
               [base-text (str (pr-str historical) "\n") #"repeats a record"]
               [base-text
                (str (pr-str (assoc-in current [:evidence/result :result/exit] 1))
                     "\n") #"invalid evidence receipts"]
               [base-text (pr-str current) #"must end with a newline"]
               [base-text "{:kind\n" #"Invalid Receipt River EDN"]]]
        (fs/writeFileSync file held-text "utf8")
        (doseq [name (segment-names segments)]
          (fs/unlinkSync (path/join segments name)))
        (when segment
          (fs/writeFileSync (path/join segments "2026-08-29T172240Z-a.edn")
                            segment "utf8"))
        (let [gate-ran? (atom false)
              catalog {:catalog/repositories
                       {"repo" {:repository/path "repo"
                                :repository/gates [local-gate]}}}]
          (with-redefs [cli/require-repositories! (fn [& _] {"repo" {}})
                        cli/run-gate! (fn [& _]
                                        (reset! gate-ran? true)
                                        passed-result)]
            (is (thrown-with-msg?
                 js/Error message
                 (cli/run-selected-gates!
                  catalog test-catalog-identity
                  {:only #{"repo"} :kinds #{:unit}}))))
          (is (false? @gate-ran?))
          (is (= held-text (fs/readFileSync file "utf8")))
          (is (= (if segment ["2026-08-29T172240Z-a.edn"] [])
                 (segment-names segments)))
          (is (= [] (lock-files segments))))))))

(deftest promotion-preserves-history-without-promoting-legacy-evidence
  (with-historical-ledger-fixture true
    (fn [{:keys [base base-text current commit!]}]
      (let [catalog {:catalog/version 1
                     :catalog/repositories
                     {"repo" {:repository/path "repo"
                              :repository/gates [local-gate]}}}]
        (with-redefs [cli/read-immutable-catalog-bundle!
                      (fn [_] {:catalog catalog
                               :catalog-identity test-catalog-identity})
                      cli/validate-catalog! identity
                      cli/gitlink-target! (fn [& _] child-revision)]
          (is (false? (cli/promotion-ready-at!
                       child-revision #{:repo/unit} [passed-result] base base)))
          (let [head (commit! base-text {"2026-08-29T172240Z-a.edn"
                                         (str (pr-str current) "\n")})]
            (is (cli/promotion-ready-at!
                 child-revision #{:repo/unit} [passed-result] base head)))
          (let [head (commit!
                      base-text
                      {"2026-08-29T172240Z-a.edn" (str (pr-str current) "\n")
                       "2026-08-29T172241Z-b.edn"
                       (str (pr-str (assoc-in current
                                              [:evidence/result :result/exit] 1))
                            "\n")})]
            (is (thrown-with-msg?
                 js/Error #"invalid evidence receipts"
                 (cli/promotion-ready-at!
                  child-revision #{:repo/unit} [passed-result] base head)))))))))

(deftest promotion-authority-binds-current-head-catalog-ledger-and-gitlink
  (let [revision reviewed-root-revision
        ledger {:ledger/identity
                {:ledger/path law/receipt-ledger-path
                 :ledger/revision revision
                 :ledger/sha256 (apply str (repeat 64 "d"))}
                :ledger/bytes (js/Buffer.alloc 0)
                :ledger/records
                [(cli/evidence-receipt
                  passed-result "2026-08-29T17:22:40Z" "test"
                  "nbb/node@test")]}
        reads (atom [])
        catalog {:catalog/version 1
                 :catalog/repositories
                 {"repo" {:repository/path "repo"
                          :repository/gates [local-gate]}}}]
    (with-redefs [cli/current-root-state!
                  (fn []
                    (swap! reads conj :root-state)
                    {:root/revision revision :root/status ""})
                  cli/read-immutable-catalog-bundle!
                  (fn [at]
                    (swap! reads conj [:catalog at])
                    {:catalog catalog
                     :catalog-identity test-catalog-identity})
                  cli/validate-catalog! identity
                  cli/read-immutable-receipt-ledger!
                  (fn [at]
                    (swap! reads conj [:ledger at])
                    ledger)
                  cli/read-immutable-receipt-segment-entries! (fn [_] [])
                  cli/ledger-extension-records!
                  (fn [& _]
                    (swap! reads conj :append-history)
                    [])
                  cli/require-result-gitlinks!
                  (fn [actual-catalog gate-ids results at]
                    (swap! reads conj [:gitlinks actual-catalog gate-ids
                                       results at])
                    true)]
      (is (cli/promotion-ready-at!
           child-revision #{:repo/unit} [passed-result]
           trusted-base-revision revision))
      (is (= [:root-state
              [:catalog revision]
              [:ledger trusted-base-revision]
              [:ledger revision]
              :append-history
              [:gitlinks catalog #{:repo/unit} [passed-result] revision]
              :root-state]
             @reads)))))

(deftest promotion-authority-rejects-noncurrent-or-dirty-root
  (with-redefs [cli/current-root-state!
                (fn [] {:root/revision (apply str (repeat 40 "d"))
                        :root/status ""})]
    (is (thrown-with-msg?
         js/Error #"not the current HEAD"
         (cli/promotion-ready-at!
          child-revision #{:repo/unit} [passed-result]
          trusted-base-revision
          reviewed-root-revision))))
  (with-redefs [cli/current-root-state!
                (fn [] {:root/revision reviewed-root-revision
                        :root/status " M config/quality-gates.edn\n"})]
    (is (thrown-with-msg?
         js/Error #"tracked or submodule changes"
         (cli/promotion-ready-at!
          child-revision #{:repo/unit} [passed-result]
          trusted-base-revision
          reviewed-root-revision)))))

(deftest promotion-authority-rechecks-root-after-immutable-reads
  (let [states (atom [{:root/revision reviewed-root-revision
                       :root/status ""}
                      {:root/revision reviewed-root-revision
                       :root/status " M repo\n"}])
        catalog {:catalog/version 1
                 :catalog/repositories
                 {"repo" {:repository/path "repo"
                          :repository/gates [local-gate]}}}
        ledger {:ledger/identity
                {:ledger/path law/receipt-ledger-path
                 :ledger/revision reviewed-root-revision
                 :ledger/sha256 (apply str (repeat 64 "d"))}
                :ledger/bytes (js/Buffer.alloc 0)
                :ledger/records
                [(cli/evidence-receipt
                  passed-result "2026-08-29T17:22:40Z" "test"
                  "nbb/node@test")]}]
    (with-redefs [cli/current-root-state!
                  (fn []
                    (let [state (first @states)]
                      (swap! states subvec 1)
                      state))
                  cli/read-immutable-catalog-bundle!
                  (fn [_] {:catalog catalog
                           :catalog-identity test-catalog-identity})
                  cli/validate-catalog! identity
                  cli/read-immutable-receipt-ledger! (fn [_] ledger)
                  cli/read-immutable-receipt-segment-entries! (fn [_] [])
                  cli/ledger-extension-records! (fn [& _] [])
                  cli/require-result-gitlinks! (fn [& _] true)]
      (is (thrown-with-msg?
           js/Error #"tracked or submodule changes"
           (cli/promotion-ready-at!
            child-revision #{:repo/unit} [passed-result]
            trusted-base-revision
            reviewed-root-revision)))
      (is (empty? @states)))))

(deftest gitlink-target-is-exact-and-unambiguous
  (with-redefs [cli/git-capture!
                (fn [args]
                  (is (= ["ls-tree" "-z" reviewed-root-revision "--" "repo"]
                         args))
                  (str "160000 commit " child-revision "\trepo\u0000"))]
    (is (= child-revision
           (cli/gitlink-target! reviewed-root-revision "repo"))))
  (with-redefs [cli/git-capture!
                (fn [_]
                  (str "160000 commit " sha256-child-revision
                       "\trepo\u0000"))]
    (is (= sha256-child-revision
           (cli/gitlink-target! reviewed-root-revision "repo"))))
  (with-redefs [cli/git-capture!
                (fn [_]
                  (str "100644 blob " child-revision "\trepo\u0000"))]
    (is (thrown-with-msg?
         js/Error #"not the expected gitlink"
         (cli/gitlink-target! reviewed-root-revision "repo")))))

(deftest validates-the-checked-in-catalog
  (let [{:keys [catalog catalog-identity]} (cli/read-catalog-bundle)]
    (is (law/valid-catalog? catalog))
    (is (identical? catalog (cli/validate-catalog! catalog)))
    (is (contains? (cli/actionable-submodule-paths) "knoxx"))
    (is (not (contains? (cli/actionable-submodule-paths) ".agents")))
    (is (= "config/quality-gates.edn" (:catalog/path catalog-identity)))
    (is (re-matches #"[0-9a-f]{64}" (:catalog/sha256 catalog-identity)))
    (is (thrown-with-msg?
         js/Error
         #"catalog/repository-not-actionable-submodule"
         (cli/validate-catalog!
          (assoc-in catalog
                    [:catalog/repositories "typo"]
                    {:repository/path "typo" :repository/gates []}))))
    (is (thrown-with-msg?
         js/Error
         #"catalog/repositories"
         (cli/validate-catalog!
          (assoc catalog :catalog/repositories 42))))
    (is (thrown-with-msg? js/Error #"Repositories have no mapped gates"
                          (cli/list-gates! catalog
                                           {:only #{"missing"}
                                            :kinds law/gate-kinds})))
    (is (thrown-with-msg? js/Error #"No mapped gates match"
                          (cli/list-gates! catalog
                                           {:only #{"katamorph"}
                                            :kinds #{:security}})))))

(deftest knoxx-gates-remain-under-knoxx-ownership
  (let [gates (get-in (cli/read-catalog)
                      [:catalog/repositories "knoxx" :repository/gates])]
    (is (seq gates))
    (is (every? #(= :workflow-only (:gate/execution %)) gates))
    (is (every? #(not (contains? % :gate/command)) gates))))

(deftest local-process-results-do-not-hide-failures
  (with-redefs [cli/spawn-result (fn [& _] #js {:status 0})]
    (is (= :passed
           (:result/outcome
            (cli/local-result "/repo" {:gate/id :repo/unit
                                        :gate/command ["test"]})))))
  (with-redefs [cli/spawn-result (fn [& _] #js {:status 7})]
    (is (= {:gate/id :repo/unit
            :result/outcome :failed
            :result/exit 7}
           (cli/local-result "/repo" {:gate/id :repo/unit
                                      :gate/command ["test"]}))))
  (with-redefs [cli/spawn-result
                (fn [& _] #js {:status nil
                               :error (js/Error. "spawn ENOENT")})]
    (is (= {:gate/id :repo/unit
            :result/outcome :unavailable
            :result/reason "spawn ENOENT"}
           (cli/local-result "/repo" {:gate/id :repo/unit
                                      :gate/command ["test"]})))))

(deftest dirty-or-moving-checkouts-cannot-produce-revision-evidence
  (let [catalog {:catalog/repositories {"repo" {:repository/gates []}}}]
    (with-redefs [workspace/inventory
                  (fn [] [{:path "repo"
                           :actionable true
                           :exists true
                           :initialized true
                           :dirty true
                           :git-errors {}
                           :head "abc123"}])
                  workspace/execution-paths!
                  (fn [repositories]
                    (is (empty? repositories))
                    [])]
      (is (= {:path "repo"
              :unavailable-reason "Repository checkout is dirty"
              :revision "abc123"}
             (get (cli/require-repositories! catalog #{"repo"}) "repo")))))
  (let [spawned? (atom false)]
    (with-redefs [cli/execution-path-unavailable-reason (constantly nil)
                  workspace/git-state
                  (fn [_] {:initialized true
                           :head "revision-b"
                           :dirty false
                           :git-errors {}})
                  cli/spawn-result
                  (fn [& _]
                    (reset! spawned? true)
                    #js {:status 0})]
      (is (= :unavailable
             (:result/outcome
              (cli/run-gate! {:path "repo"
                              :absolute "/repo"
                              :revision "revision-a"}
                             local-gate
                             test-catalog-identity))))
      (is (false? @spawned?)))))

(deftest execution-path-identity-is-rechecked
  (let [inventory {:path "repo"
                   :actionable true
                   :source-type "git-submodule"
                   :ownership "independent-repository"
                   :device 7
                   :inode 11}
        repository {:absolute "/repo" :inventory inventory}]
    (with-redefs [workspace/execution-paths!
                  (fn [repositories]
                    (is (= [inventory] repositories))
                    [{:repo inventory :absolute "/repo"}])]
      (is (nil? (cli/execution-path-unavailable-reason repository))))
    (with-redefs [workspace/execution-paths!
                  (fn [_] [{:repo inventory :absolute "/replacement"}])]
      (is (= "Repository execution path changed after inventory"
             (cli/execution-path-unavailable-reason repository))))
    (with-redefs [workspace/execution-paths!
                  (fn [_]
                    (throw (js/Error. "Executable source identity changed")))]
      (is (= (str "Repository execution path could not be reverified: "
                  "Executable source identity changed")
             (cli/execution-path-unavailable-reason repository))))))

(deftest local-gates-recheck-execution-path-around-every-spawn
  (let [path-results (atom [nil "Repository execution path changed after inventory"])
        git-rechecks (atom 0)
        spawn-count (atom 0)]
    (with-redefs [cli/execution-path-unavailable-reason
                  (fn [_]
                    (let [result (first @path-results)]
                      (swap! path-results subvec 1)
                      result))
                  workspace/git-state
                  (fn [_]
                    (swap! git-rechecks inc)
                    {:initialized true
                     :head "revision-a"
                     :dirty false
                     :git-errors {}})
                  cli/spawn-result
                  (fn [& _]
                    (swap! spawn-count inc)
                    #js {:status 0})]
      (let [result (cli/run-gate! {:path "repo"
                                   :absolute "/repo"
                                   :revision "revision-a"}
                                  local-gate
                                  test-catalog-identity)]
        (is (empty? @path-results))
        (is (= 1 @git-rechecks))
        (is (= 1 @spawn-count))
        (is (= :unavailable (:result/outcome result)))
        (is (= :passed (:result/attempt-outcome result)))
        (is (= 0 (:result/attempt-exit result)))
        (is (= (str "Evidence rejected after gate execution: "
                    "Repository execution path changed after inventory")
               (:result/reason result)))
        (is (nil? (:result/revision result)))))))

(deftest classifies-every-local-checkout-recheck-branch
  (let [repository {:revision "revision-a"}]
    (is (= "Repository checkout became uninitialized"
           (cli/local-unavailable-reason
            repository
            {:initialized false :head "revision-a" :dirty false :git-errors {}})))
    (is (= "Repository Git state could not be reverified"
           (cli/local-unavailable-reason
            repository
            {:initialized true :head "revision-a" :dirty false
             :git-errors {:head "failed"}})))
    (is (= "Repository checkout became dirty"
           (cli/local-unavailable-reason
            repository
            {:initialized true :head "revision-a" :dirty true :git-errors {}})))
    (is (= "Repository checkout cleanliness could not be reverified"
           (cli/local-unavailable-reason
            repository
            {:initialized true :head "revision-a" :dirty nil :git-errors {}})))
    (is (= "Repository revision changed after inventory"
           (cli/local-unavailable-reason
            repository
            {:initialized true :head "revision-b" :dirty false :git-errors {}})))
    (is (nil? (cli/local-unavailable-reason
               repository
               {:initialized true :head "revision-a" :dirty false :git-errors {}})))))

(deftest local-gates-recheck-after-spawn-and-retain-provenance
  (let [states (atom [{:initialized true
                       :head "revision-a"
                       :dirty false
                       :git-errors {}}
                      {:initialized true
                       :head "revision-b"
                       :dirty false
                       :git-errors {}}])
        spawn-count (atom 0)]
    (with-redefs [cli/execution-path-unavailable-reason (constantly nil)
                  workspace/git-state
                  (fn [_]
                    (let [state (first @states)]
                      (swap! states subvec 1)
                      state))
                  cli/spawn-result
                  (fn [cwd command]
                    (is (= "/repo" cwd))
                    (is (= ["test" "--exact"] command))
                    (swap! spawn-count inc)
                    #js {:status 0})]
      (let [result (cli/run-gate! {:path "repo"
                                   :absolute "/repo"
                                   :revision "revision-a"}
                                  local-gate
                                  test-catalog-identity)]
        (is (= 1 @spawn-count))
        (is (= :unavailable (:result/outcome result)))
        (is (= :passed (:result/attempt-outcome result)))
        (is (= "revision-b" (:result/observed-revision result)))
        (is (nil? (:result/revision result)))
        (is (= ["test" "--exact"] (:result/command result)))
        (is (= test-catalog-identity (:result/catalog result)))
        (is (= {:source/path "repo/package.json"
                :source/repository "repo"
                :source/revision "revision-a"}
               (:result/source result)))))))

(deftest stable-local-gates-bind-the-verified-revision
  (with-redefs [cli/execution-path-unavailable-reason (constantly nil)
                workspace/git-state
                (fn [_] {:initialized true
                         :head "revision-a"
                         :dirty false
                         :git-errors {}})
                cli/spawn-result (fn [& _] #js {:status 0})]
    (let [result (cli/run-gate! {:path "repo"
                                 :absolute "/repo"
                                 :revision "revision-a"}
                                local-gate
                                test-catalog-identity)]
      (is (= :passed (:result/outcome result)))
      (is (= "revision-a" (:result/revision result)))
      (is (= ["test" "--exact"] (:result/command result))))))

(deftest coverage-zero-exit-remains-unavailable-without-report-attestation
  (let [coverage-gate (assoc local-gate
                             :gate/id :repo/coverage
                             :gate/kind :coverage)]
    (with-redefs [cli/execution-path-unavailable-reason (constantly nil)
                  workspace/git-state
                  (fn [_] {:initialized true
                           :head child-revision
                           :dirty false
                           :git-errors {}})
                  cli/spawn-result (fn [& _] #js {:status 0})]
      (let [result (cli/run-gate! {:path "repo"
                                   :absolute "/repo"
                                   :revision child-revision}
                                  coverage-gate
                                  test-catalog-identity)]
        (is (= :unavailable (:result/outcome result)))
        (is (= cli/coverage-artifact-unavailable-reason
               (:result/reason result)))
        (is (= :passed (:result/attempt-outcome result)))
        (is (= 0 (:result/attempt-exit result)))
        (is (not (contains? result :result/exit)))
        (is (law/valid-result? result))
        (is (= 3 (cli/result-exit result)))))))

(deftest missing-checkouts-and-external-hosts-remain-non-passes
  (is (= :unavailable
         (:result/outcome
          (cli/run-gate! {:path "repo"
                          :unavailable-reason "not initialized"}
                         {:gate/id :repo/unit
                          :gate/kind :unit
                          :gate/execution :local
                          :gate/command ["test"]
                          :gate/source "repo/package.json"}
                         test-catalog-identity))))
  (is (= :blocked
         (:result/outcome
          (cli/run-gate! {:path "repo" :revision "revision-a"}
                         {:gate/id :repo/live
                          :gate/kind :live-smoke
                          :gate/execution :external
                          :gate/source "repo/.github/workflows/live.yml"
                          :gate/reason "needs host"}
                         test-catalog-identity)))))

(deftest process-exit-contract-keeps-non-passes-nonzero
  (is (zero? (cli/result-exit {:result/outcome :passed})))
  (is (= 1 (cli/result-exit {:result/outcome :failed})))
  (is (= 3 (cli/result-exit {:result/outcome :unavailable})))
  (is (= 4 (cli/result-exit {:result/outcome :blocked})))
  (is (zero? (cli/result-exit {:result/outcome :not-applicable}))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (if (test/successful? summary) 0 1)))

(test/run-tests 'evidence-cli-test)
