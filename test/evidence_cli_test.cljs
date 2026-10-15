;; SPDX-License-Identifier: GPL-3.0-or-later
(ns evidence-cli-test
  (:require [cljs.test :as test :refer [deftest is]]
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
        file (path/join directory "receipts.edn")]
    (fs/mkdirSync directory)
    (try
      (run! {:fixture fixture :directory directory :file file})
      (finally
        (fs/rmSync fixture #js {:recursive true :force true})))))

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
        (is (re-find #"[.]ημ/receipts[.]edn$" (:file @captured)))))))

(deftest evidence-append-refuses-to-recreate-a-missing-ledger
  (with-receipt-fixture
    (fn [{:keys [file]}]
      (with-redefs [cli/receipt-file file]
        (is (thrown-with-msg?
             js/Error #"could not be securely opened"
             (cli/append-evidence-receipt! passed-result)))
        (is (not (fs/existsSync file)))))))

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
    (fn [{:keys [directory file]}]
      (let [gate-ran? (atom false)
            lock-file (path/join directory ".receipts.edn.append.lock")
            catalog {:catalog/repositories
                     {"repo" {:repository/path "repo"
                              :repository/gates [local-gate]}}}]
        ;; Semantic evidence appends require an explicitly initialized ledger.
        (fs/writeFileSync file "" "utf8")
        (with-redefs [cli/receipt-file file
                      cli/git-capture!
                      (fn [args]
                        (is (= ["rev-parse" "HEAD"] args))
                        reviewed-root-revision)
                      cli/read-immutable-receipt-ledger!
                      (fn [revision]
                        (is (= reviewed-root-revision revision))
                        {:ledger/bytes (js/Buffer.alloc 0)})
                      cli/require-repositories!
                      (fn [& _]
                        {"repo" {:path "repo"
                                 :absolute "/repo"
                                 :revision child-revision}})
                      cli/run-gate!
                      (fn [& _]
                        (reset! gate-ran? true)
                        (is (fs/existsSync lock-file))
                        (is (fs/existsSync file))
                        passed-result)]
          (is (zero? (cli/run-selected-gates!
                      catalog test-catalog-identity
                      {:only #{"repo"} :kinds #{:unit}})))
          (is @gate-ran?)
          (is (not (fs/existsSync lock-file)))
          (let [records (cli/read-receipt-records!
                         (fs/readFileSync file "utf8"))]
            (is (= 1 (count records)))
            (is (= passed-result (:evidence/result (first records))))))))))

(deftest selected-gates-reject-an-unsafe-receipt-target-before-execution
  (with-receipt-fixture
    (fn [{:keys [directory file]}]
      (let [gate-ran? (atom false)
            lock-file (path/join directory ".receipts.edn.append.lock")
            catalog {:catalog/repositories
                     {"repo" {:repository/path "repo"
                              :repository/gates [local-gate]}}}]
        (fs/writeFileSync lock-file "stale-or-held\n"
                          #js {:encoding "utf8" :flag "wx"})
        (with-redefs [cli/receipt-file file
                      cli/require-repositories!
                      (fn [& _]
                        {"repo" {:path "repo"
                                 :absolute "/repo"
                                 :revision child-revision}})
                      cli/run-gate!
                      (fn [& _]
                        (reset! gate-ran? true)
                        passed-result)]
          (is (thrown-with-msg?
               js/Error #"another writer holds the append lock"
               (cli/run-selected-gates!
                catalog test-catalog-identity
                {:only #{"repo"} :kinds #{:unit}})))
          (is (false? @gate-ran?))
          (is (not (fs/existsSync file)))
          (is (= "stale-or-held\n"
                 (fs/readFileSync lock-file "utf8"))))))))

(deftest selected-gates-refuse-to-recreate-a-missing-ledger
  (with-receipt-fixture
    (fn [{:keys [directory file]}]
      (let [gate-ran? (atom false)
            lock-file (path/join directory ".receipts.edn.append.lock")
            catalog {:catalog/repositories
                     {"repo" {:repository/path "repo"
                              :repository/gates [local-gate]}}}]
        (with-redefs [cli/receipt-file file
                      cli/require-repositories!
                      (fn [& _]
                        {"repo" {:path "repo"
                                 :absolute "/repo"
                                 :revision child-revision}})
                      cli/run-gate!
                      (fn [& _]
                        (reset! gate-ran? true)
                        passed-result)]
          (is (thrown-with-msg?
               js/Error #"could not be securely opened"
               (cli/run-selected-gates!
                catalog test-catalog-identity
                {:only #{"repo"} :kinds #{:unit}})))
          (is (false? @gate-ran?))
          (is (not (fs/existsSync file)))
          (is (not (fs/existsSync lock-file))))))))

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
        committed-text (str first-line (pr-str second-record) "\n")
        committed-bytes (js/Buffer.from committed-text "utf8")
        catalog {:catalog/repositories
                 {"repo" {:repository/path "repo"
                          :repository/gates [local-gate]}}}]
    (is (law/receipt-envelope? first-record))
    (is (law/receipt-envelope? second-record))
    (doseq [held-text ["" first-line]]
      (with-receipt-fixture
        (fn [{:keys [directory file]}]
          (let [gate-ran? (atom false)
                lock-file (path/join directory
                                     ".receipts.edn.append.lock")]
            (fs/writeFileSync file held-text "utf8")
            (with-redefs [cli/receipt-file file
                          cli/git-capture!
                          (fn [args]
                            (is (= ["rev-parse" "HEAD"] args))
                            reviewed-root-revision)
                          cli/read-immutable-receipt-ledger!
                          (fn [revision]
                            (is (= reviewed-root-revision revision))
                            {:ledger/bytes committed-bytes})
                          cli/require-repositories!
                          (fn [& _]
                            {"repo" {:path "repo"
                                     :absolute "/repo"
                                     :revision child-revision}})
                          cli/run-gate!
                          (fn [& _]
                            (reset! gate-ran? true)
                            passed-result)]
              (is (thrown-with-msg?
                   js/Error #"does not preserve the committed ledger"
                   (cli/run-selected-gates!
                    catalog test-catalog-identity
                    {:only #{"repo"} :kinds #{:unit}})))
              (is (false? @gate-ran?))
              (is (= held-text (fs/readFileSync file "utf8")))
              (is (not (fs/existsSync lock-file))))))))))

(deftest selected-gates-reject-an-invalid-held-ledger-before-execution
  (with-receipt-fixture
    (fn [{:keys [directory file]}]
      (let [gate-ran? (atom false)
            invalid-ledger "{:kind :observation}\n"
            lock-file (path/join directory ".receipts.edn.append.lock")
            catalog {:catalog/repositories
                     {"repo" {:repository/path "repo"
                              :repository/gates [local-gate]}}}]
        (fs/writeFileSync file invalid-ledger "utf8")
        (with-redefs [cli/receipt-file file
                      cli/git-capture!
                      (fn [_] reviewed-root-revision)
                      cli/read-immutable-receipt-ledger!
                      (fn [_]
                        {:ledger/bytes (js/Buffer.alloc 0)})
                      cli/require-repositories!
                      (fn [& _]
                        {"repo" {:path "repo"
                                 :absolute "/repo"
                                 :revision child-revision}})
                      cli/run-gate!
                      (fn [& _]
                        (reset! gate-ran? true)
                        passed-result)]
          (is (thrown-with-msg?
               js/Error #"invalid receipt envelopes"
               (cli/run-selected-gates!
                catalog test-catalog-identity
                {:only #{"repo"} :kinds #{:unit}})))
          (is (false? @gate-ran?))
          (is (= invalid-ledger (fs/readFileSync file "utf8")))
          (is (not (fs/existsSync lock-file))))))))

(deftest selected-gates-reject-held-ledger-mutation-after-execution
  (with-receipt-fixture
    (fn [{:keys [directory file]}]
      (let [gate-ran? (atom false)
            changed-ledger "{:kind :observation}\n"
            lock-file (path/join directory ".receipts.edn.append.lock")
            catalog {:catalog/repositories
                     {"repo" {:repository/path "repo"
                              :repository/gates [local-gate]}}}]
        (fs/writeFileSync file "" "utf8")
        (with-redefs [cli/receipt-file file
                      cli/git-capture!
                      (fn [_] reviewed-root-revision)
                      cli/read-immutable-receipt-ledger!
                      (fn [_]
                        {:ledger/bytes (js/Buffer.alloc 0)})
                      cli/require-repositories!
                      (fn [& _]
                        {"repo" {:path "repo"
                                 :absolute "/repo"
                                 :revision child-revision}})
                      cli/run-gate!
                      (fn [& _]
                        (reset! gate-ran? true)
                        ;; Model a non-cooperating writer that ignores the held lock.
                        (fs/writeFileSync file changed-ledger "utf8")
                        passed-result)]
          (is (thrown-with-msg?
               js/Error #"changed after held ledger validation"
               (cli/run-selected-gates!
                catalog test-catalog-identity
                {:only #{"repo"} :kinds #{:unit}})))
          (is @gate-ran?)
          (is (= changed-ledger (fs/readFileSync file "utf8")))
          (is (not (fs/existsSync lock-file))))))))

(deftest receipt-lines-require-one-complete-edn-form
  (is (= [{:a 1}] (cli/read-receipt-records! "{:a 1}\n")))
  (is (thrown-with-msg?
       js/Error #"exactly one EDN form"
       (cli/read-receipt-records! "{:a 1} {:b 2}\n")))
  (is (thrown-with-msg?
       js/Error #"exactly one EDN form"
       (cli/read-receipt-records! "{:a 1} trailing\n"))))

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
        head-text (str base-text (pr-str invalid-record) "\n")
        ledger (fn [revision text records]
                 {:ledger/identity
                  {:ledger/path law/receipt-ledger-path
                   :ledger/revision revision
                   :ledger/sha256 (cli/sha256
                                   (js/Buffer.from text "utf8"))}
                  :ledger/bytes (js/Buffer.from text "utf8")
                  :ledger/records records})]
    (with-redefs [cli/read-immutable-receipt-ledger!
                  (fn [revision]
                    (if (= revision child-revision)
                      (ledger revision base-text [base-record])
                      (ledger revision head-text
                              [base-record invalid-record])))]
      (is (thrown-with-msg?
           js/Error #"invalid receipt envelopes"
           (cli/verify-receipts!
            {:base child-revision :at reviewed-root-revision})))))
  (let [base (js/Buffer.from "" "utf8")
        head (js/Buffer.from "42\n" "utf8")]
    (is (= [42] (cli/appended-receipt-records! base head)))))

(defn with-historical-ledger-fixture [terminal-newline? run!]
  (with-receipt-fixture
    (fn [{:keys [fixture file] :as context}]
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
              commit! (fn [contents]
                        (fs/writeFileSync file contents)
                        (cli/git-capture! ["add" "--" law/receipt-ledger-path])
                        (cli/git-capture!
                         ["-c" "user.name=Receipt Test"
                          "-c" "user.email=receipt-test@example.invalid"
                          "-c" "commit.gpgsign=false"
                          "commit" "--quiet" "--allow-empty" "-m" "fixture"])
                        (str/trim
                         (cli/git-capture! ["rev-parse" "HEAD"])))
              base (commit! base-text)]
          (run! (assoc context
                       :current current :historical historical
                       :base-text base-text :base base :commit! commit!)))))))

(deftest receipt-verification-trusts-exact-historical-prefix-only
  (doseq [terminal-newline? [true false]]
    (with-historical-ledger-fixture terminal-newline?
      (fn [{:keys [base base-text historical current commit!]}]
        (is (not (law/receipt-envelope? historical)))
        (is (zero? (cli/verify-receipts! {:base base :at base})))
        (let [head-text (str base-text (when-not terminal-newline? "\n")
                             (pr-str current) "\n")
              head (commit! head-text)
              output (with-out-str
                       (is (zero? (cli/verify-receipts!
                                   {:base base :at head}))))]
          (is (str/includes? output ":total-receipts 2"))
          (is (str/includes? output ":appended-receipts 1"))
          (is (str/includes? output ":appended-evidence-receipts 1"))
          (is (str/includes? output ":legacy-evidence-receipts 1"))
          (is (.equals (js/Buffer.from base-text "utf8")
                       (:ledger/bytes
                        (cli/read-immutable-receipt-ledger! base)))))))))

(deftest receipt-verification-rejects-invalid-suffixes-after-history
  (with-historical-ledger-fixture true
    (fn [{:keys [base base-text historical current commit!]}]
      (doseq [[suffix message]
              [[(str (pr-str historical) "\n") #"invalid receipt envelopes"]
               ["42\n" #"invalid receipt envelopes"]
               [(str (pr-str (dissoc current :origin)) "\n")
                #"invalid receipt envelopes"]
               [(str (pr-str (assoc-in current [:evidence/result :result/exit] 1))
                     "\n") #"invalid evidence receipts"]
               ["{:kind\n" #"Invalid Receipt River EDN"]
               [(str (pr-str current) " 42\n") #"exactly one EDN form"]
               [(pr-str current) #"must end with a newline"]]]
        (let [head (commit! (str base-text suffix))]
          (is (thrown-with-msg?
               js/Error message
               (cli/verify-receipts! {:base base :at head}))))))))

(deftest receipt-verification-rejects-historical-rewrite-and-truncation
  (with-historical-ledger-fixture true
    (fn [{:keys [base base-text historical commit!]}]
      (doseq [head-text [""
                        (subs base-text 0 (dec (count base-text)))
                        (str (pr-str (assoc historical :owner "rewrite")) "\n")]]
        (let [head (commit! head-text)]
          (is (thrown-with-msg?
               js/Error #"does not preserve the base bytes as a prefix"
               (cli/verify-receipts! {:base base :at head}))))))))

(defn canonical-river-observation []
  {:event/id "a6d845a8-94da-4d83-b407-a73ee7b48aaf"
   :event/type :receipt-river/receipt-recorded
   :event/recorded-at "2026-10-14T06:12:17.427Z"
   :event/schema {:id :eta-mu.receipt-river/receipt-recorded :version 1}
   :event/producer {:package/name "@eta-mu/receipt-river"
                    :package/version "0.1.0"
                    :command "Receipt River package API"
                    :actor "test"
                    :eta-mu/version "0.1.0"
                    :source/commit child-revision}
   :event/subject {:repo "open-hax/foresight"}
   :event/payload {:ts "2026-10-14T06:12:17.427Z"
                   :kind :observation
                   :repo "open-hax/foresight"
                   :origin "Canonical receipt consumer regression"
                   :owner "test"
                   :dod "Preserve immutable historical receipt bytes"
                   :pi "eta-mu"
                   :host "test"
                   :manifest "source-manifest.json sha256:retained"
                   :refs "retained source and test references"}})

(deftest canonical-river-events-admit-without-changing-legacy-evidence
  (let [event (canonical-river-observation)
        legacy (cli/evidence-receipt passed-result
                                     "2026-10-14T06:12:17.427Z"
                                     "test" "nbb/node@test")]
    (is (not (law/receipt-envelope? event)))
    (is (not (law/receipt-envelope? (:event/payload event))))
    (is (not (law/evidence-receipt? event)))
    (is (not (law/receipt-attests-result? event passed-result)))
    (is (= {:receipt/total 2 :receipt/evidence 1 :receipt/legacy-evidence 0}
           (cli/require-valid-receipt-records! [legacy event])))
    (doseq [value [nil 42 {} []]]
      (is (thrown-with-msg? js/Error #"invalid receipt envelopes"
                           (cli/require-valid-receipt-records! [value]))))))

(deftest actual-canonical-337d-events-are-admitted-without-rewrite
  ;; Exact lines 197–198 of 337d2eafe21b8093053a1449956ed38d326c7b04.
  ;; A retained fixture keeps this regression runnable in a shallow CI checkout.
  (let [bytes (fs/readFileSync
               (path/join cli/root "test" "fixtures"
                          "canonical-receipt-river-events.edn"))
        appended (cli/read-receipt-records!
                  (cli/decode-utf8! bytes "canonical regression fixture"))]
    (is (= ["8fa78a8b-badb-4750-814a-6f7fb31b14a9"
            "3d4cd40f-d37c-4c00-b533-f22cc3e1ff62"]
           (mapv :event/id appended)))
    (is (= {:receipt/total 2 :receipt/evidence 0 :receipt/legacy-evidence 0}
           (cli/require-valid-receipt-records! appended)))))

(deftest canonical-observations-pass-immutable-suffix-and-secure-append-boundaries
  (doseq [terminal-newline? [true false]]
    (with-historical-ledger-fixture terminal-newline?
      (fn [{:keys [base base-text file commit!]}]
        (let [event (canonical-river-observation)
              text (str base-text (when-not terminal-newline? "\n")
                        (pr-str event) "\n")]
          ;; Leave the canonical event uncommitted, so secure append must
          ;; validate it as a new suffix rather than trust it as history.
          (fs/writeFileSync file text "utf8")
          (let [receipt (cli/append-evidence-receipt! passed-result)
                bytes (fs/readFileSync file)
                head (commit! (.toString bytes "utf8"))
                output (with-out-str
                         (is (zero? (cli/verify-receipts!
                                     {:base base :at head}))))]
            (is (str/includes? output ":appended-receipts 2"))
            (is (str/includes? output ":appended-evidence-receipts 1"))
            (is (cli/buffer-prefix? (js/Buffer.from text "utf8") bytes))
            (is (= [receipt]
                   (cli/appended-receipt-records!
                    (js/Buffer.from text "utf8") bytes)))))))))

(deftest canonical-river-events-reject-malformed-contract-boundaries
  (let [event (canonical-river-observation)]
    (doseq [key (keys event)]
      (is (thrown-with-msg?
           js/Error #"invalid receipt envelopes"
           (cli/require-valid-receipt-records! [(dissoc event key)]))))
    (doseq [invalid [(assoc event :event/id 42)
                     (assoc event :event/id "not-a-uuid")
                     (assoc event :event/type :other/event)
                     (assoc event :event/schema [])
                     (assoc-in event [:event/schema :id] :other/schema)
                     (assoc-in event [:event/schema :version] "1")
                     (assoc-in event [:event/schema :version] 2)
                     (assoc event :event/recorded-at 42)
                     (assoc event :event/recorded-at "2026-02-30T00:00:00Z")
                     (assoc event :event/recorded-at "2026-10-14T24:00:00Z")
                     (assoc event :event/producer [])
                     (assoc-in event [:event/producer :package/name] "other")
                     (assoc-in event [:event/producer :package/version] 1)
                     (assoc-in event [:event/producer :package/version] "999.0.0")
                     (assoc event :event/subject "open-hax/foresight")
                     (assoc event :event/payload [])
                     (assoc-in event [:event/payload :ts] "2026-99-99T00:00:00Z")
                     (assoc-in event [:event/payload :kind] "observation")
                     (assoc-in event [:event/payload :kind] :unknown)
                     (assoc-in event [:event/payload :kind] :test-run)
                     (assoc-in event [:event/payload :manifest] 42)
                     (assoc-in event [:event/payload :manifest] ["source"])
                     (assoc-in event [:event/payload :refs] ["source"])
                     (assoc-in event [:event/payload :refs] [42])]]
      (is (thrown-with-msg? js/Error #"invalid receipt envelopes"
                           (cli/require-valid-receipt-records! [invalid]))))
    (doseq [key [:repo :origin :owner :dod :pi :host :manifest :refs]]
      (doseq [value [nil 42 ""]]
        (is (thrown-with-msg?
             js/Error #"invalid receipt envelopes"
             (cli/require-valid-receipt-records!
              [(assoc-in event [:event/payload key] value)])))))))

(deftest unadmitted-producer-versions-refuse-suffix-and-secure-append
  (doseq [terminal-newline? [true false]]
    (with-historical-ledger-fixture terminal-newline?
      (fn [{:keys [base base-text file directory commit!]}]
        (let [event (assoc-in (canonical-river-observation)
                              [:event/producer :package/version] "999.0.0")
              text (str base-text (when-not terminal-newline? "\n")
                        (pr-str event) "\n")]
          ;; The unsupported record is a new working-tree suffix, not trusted
          ;; history, when the evidence writer checks admission before append.
          (fs/writeFileSync file text "utf8")
          (is (thrown-with-msg?
               js/Error #"invalid receipt envelopes"
               (cli/append-evidence-receipt! passed-result)))
          (is (.equals (js/Buffer.from text "utf8")
                       (fs/readFileSync file)))
          (is (not (fs/existsSync
                    (path/join directory ".receipts.edn.append.lock"))))
          ;; Restore the exact neighbor after a RED writer may have appended,
          ;; then independently exercise immutable base-relative admission.
          (let [head (commit! text)]
            (is (thrown-with-msg?
                 js/Error #"invalid receipt envelopes"
                 (cli/verify-receipts! {:base base :at head})))
            (is (.equals (js/Buffer.from base-text "utf8")
                         (:ledger/bytes
                          (cli/read-immutable-receipt-ledger! base))))))))))

(deftest incomplete-producer-custody-refuses-new-suffix-before-append
  (let [event (canonical-river-observation)
        missing (mapv #(update event :event/producer dissoc %)
                      [:actor :command :eta-mu/version :source/commit])
        malformed [(assoc-in event [:event/producer :actor] " ")
                   (assoc-in event [:event/producer :command] 42)
                   (assoc-in event [:event/producer :eta-mu/version] [])
                   (assoc-in event [:event/producer :source/commit] "main")]]
    (doseq [terminal-newline? [true false]
            invalid (concat missing malformed)]
      (with-historical-ledger-fixture terminal-newline?
        (fn [{:keys [base base-text file directory commit!]}]
          (let [text (str base-text (when-not terminal-newline? "\n")
                          (pr-str invalid) "\n")
                phases (atom [])]
            (fs/writeFileSync file text "utf8")
            (binding [cli/*secure-append-phase-hook*
                      (fn [phase _] (swap! phases conj phase))]
              (is (thrown-with-msg?
                   js/Error #"invalid receipt envelopes"
                   (cli/append-evidence-receipt! passed-result))))
            (is (= [] @phases))
            (is (.equals (js/Buffer.from text "utf8")
                         (fs/readFileSync file)))
            (is (not (fs/existsSync
                      (path/join directory ".receipts.edn.append.lock"))))
            ;; A failing predecessor may have appended. Restore the exact
            ;; requested suffix before independent immutable verification.
            (let [head (commit! text)]
              (is (thrown-with-msg?
                   js/Error #"invalid receipt envelopes"
                   (cli/verify-receipts! {:base base :at head})))
              (is (.equals (js/Buffer.from base-text "utf8")
                           (:ledger/bytes
                            (cli/read-immutable-receipt-ledger! base)))))))))))

(deftest producer-custody-admission-preserves-trusted-history
  (with-historical-ledger-fixture true
    (fn [{:keys [base-text file commit!]}]
      (let [historical (update (canonical-river-observation) :event/producer
                               #(apply dissoc % [:actor :command :eta-mu/version
                                                 :source/commit]))
            trusted-text (str base-text (pr-str historical) "\n")
            base (commit! trusted-text)
            fresh (assoc (canonical-river-observation)
                         :event/id "d1e845a8-94da-4d83-b407-a73ee7b48aaf")
            head (commit! (str trusted-text (pr-str fresh) "\n"))]
        (is (zero? (cli/verify-receipts! {:base base :at base})))
        (is (zero? (cli/verify-receipts! {:base base :at head})))
        (is (.equals (js/Buffer.from trusted-text "utf8")
                     (:ledger/bytes (cli/read-immutable-receipt-ledger! base))))
        (let [receipt (cli/append-evidence-receipt! passed-result)
              bytes (fs/readFileSync file)]
          (is (cli/buffer-prefix? (js/Buffer.from trusted-text "utf8") bytes))
          (is (= [fresh receipt]
                 (cli/appended-receipt-records!
                  (:ledger/bytes (cli/read-immutable-receipt-ledger! base))
                  bytes))))))))

(deftest canonical-envelope-cannot-smuggle-legacy-evidence-or-a-fallback
  (let [event (canonical-river-observation)
        legacy (cli/evidence-receipt passed-result
                                     "2026-10-14T06:12:17.427Z"
                                     "test" "nbb/node@test")]
    (doseq [invalid [(merge legacy event)
                     (assoc legacy :event/schema nil)
                     (assoc legacy :event/type :receipt-river/receipt-recorded)
                     (assoc-in event [:event/payload :origin]
                               law/evidence-receipt-origin)
                     (assoc event :event/payload legacy)
                     (assoc event :event/payload
                            (assoc-in legacy [:evidence/result :result/exit] 1))]]
      (is (thrown-with-msg?
           js/Error #"invalid receipt envelopes"
           (cli/require-valid-receipt-records! [invalid]))))))

(deftest canonical-suffix-records-cannot-repeat-event-identities
  (let [event (canonical-river-observation)]
    (doseq [repeated [event (assoc-in event [:event/payload :note] "conflict")]]
      (is (thrown-with-msg?
           js/Error #"duplicate canonical event IDs"
           (cli/require-valid-receipt-records! [event repeated]))))))

(defn with-canonical-identity-fixture [collision-in-base? run!]
  (with-historical-ledger-fixture true
    (fn [{:keys [base base-text commit!] :as context}]
      (let [event (canonical-river-observation)
            trusted-text (str base-text
                              (when collision-in-base?
                                (str (pr-str event) "\n")))
            trusted-base (if collision-in-base? (commit! trusted-text) base)]
        (run! (assoc context
                     :base trusted-base :base-text trusted-text
                     :event event :collision-in-base? collision-in-base?))))))

(defn duplicate-canonical-suffix [{:keys [event collision-in-base?]} repeated]
  (str (when-not collision-in-base? (str (pr-str event) "\n"))
       (pr-str repeated) "\n"))

(deftest immutable-receipt-history-rejects-canonical-replay-and-conflicting-ids
  (doseq [collision-in-base? [false true]]
    (with-canonical-identity-fixture collision-in-base?
      (fn [{:keys [base base-text event commit!] :as context}]
        (doseq [repeated [event
                         (assoc-in event [:event/payload :note] "conflict")]]
          (let [head (commit! (str base-text
                                  (duplicate-canonical-suffix context repeated)))]
            (is (thrown-with-msg?
                 js/Error #"duplicate canonical event IDs"
                 (cli/verify-receipts! {:base base :at head})))
            (is (.equals (js/Buffer.from base-text "utf8")
                         (:ledger/bytes
                          (cli/read-immutable-receipt-ledger! base))))))))))

(deftest secure-evidence-append-rejects-new-canonical-identity-collisions
  (doseq [collision-in-base? [false true]]
    (with-canonical-identity-fixture collision-in-base?
      (fn [{:keys [base-text event file directory] :as context}]
        (doseq [repeated [event
                         (assoc-in event [:event/payload :note] "conflict")]]
          (let [held-text (str base-text
                               (duplicate-canonical-suffix context repeated))]
            (fs/writeFileSync file held-text "utf8")
            (is (thrown-with-msg?
                 js/Error #"duplicate canonical event IDs"
                 (cli/append-evidence-receipt! passed-result)))
            (is (= held-text (fs/readFileSync file "utf8")))
            (is (not (fs/existsSync
                      (path/join directory ".receipts.edn.append.lock"))))))))))

(deftest promotion-refuses-new-canonical-identity-collisions
  (doseq [collision-in-base? [false true]]
    (with-canonical-identity-fixture collision-in-base?
      (fn [{:keys [base base-text event current commit!] :as context}]
        (let [catalog {:catalog/version 1
                       :catalog/repositories
                       {"repo" {:repository/path "repo"
                                :repository/gates [local-gate]}}}]
          (with-redefs [cli/read-immutable-catalog-bundle!
                        (fn [_] {:catalog catalog
                                 :catalog-identity test-catalog-identity})
                        cli/validate-catalog! identity
                        cli/gitlink-target! (fn [& _] child-revision)]
            (doseq [repeated [event
                             (assoc-in event [:event/payload :note] "conflict")]]
              (let [head (commit!
                          (str base-text
                               (duplicate-canonical-suffix context repeated)
                               (pr-str current) "\n"))]
                (is (thrown-with-msg?
                     js/Error #"duplicate canonical event IDs"
                     (cli/promotion-ready-at!
                      child-revision #{:repo/unit} [passed-result]
                      base head)))))))))))

(deftest canonical-identity-check-grandfathers-unchanged-history-only
  (with-historical-ledger-fixture true
    (fn [{:keys [base-text commit!]}]
      (let [event (canonical-river-observation)
            trusted-text (str base-text (pr-str event) "\n"
                              (pr-str event) "\n")
            base (commit! trusted-text)
            fresh (assoc event :event/id "d1e845a8-94da-4d83-b407-a73ee7b48aaf")
            head (commit! (str trusted-text (pr-str fresh) "\n"))
            output (with-out-str
                     (is (zero? (cli/verify-receipts! {:base base :at base})))
                     (is (zero? (cli/verify-receipts! {:base base :at head}))))]
        (is (str/includes? output ":total-receipts 4"))
        (is (str/includes? output ":appended-receipts 1"))
        (is (str/includes? output ":appended-evidence-receipts 0"))
        (is (str/includes? output ":legacy-evidence-receipts 1"))
        (is (.equals (js/Buffer.from trusted-text "utf8")
                     (:ledger/bytes (cli/read-immutable-receipt-ledger! base))))))))

(deftest evidence-append-preserves-history-and-validates-uncommitted-suffix
  (doseq [terminal-newline? [true false]]
    (with-historical-ledger-fixture terminal-newline?
      (fn [{:keys [base base-text file]}]
        ;; A second writer must accept the first writer's canonical suffix.
        (let [first-receipt (cli/append-evidence-receipt! passed-result)
              second-receipt (cli/append-evidence-receipt! passed-result)
              bytes (fs/readFileSync file)]
          (is (cli/buffer-prefix? (js/Buffer.from base-text "utf8") bytes))
          (is (= [first-receipt second-receipt]
                 (cli/appended-receipt-records!
                  (:ledger/bytes (cli/read-immutable-receipt-ledger! base))
                  bytes))))))))

(deftest selected-gates-reject-invalid-extensions-of-historical-ledger
  (with-historical-ledger-fixture true
    (fn [{:keys [base-text historical current file directory]}]
      (doseq [[held-text message]
              [["" #"does not preserve the committed ledger"]
               [(str (pr-str (assoc historical :owner "rewrite")) "\n")
                #"does not preserve the committed ledger"]
               [(str base-text (pr-str historical) "\n")
                #"invalid receipt envelopes"]
               [(str base-text
                     (pr-str (assoc-in current [:evidence/result :result/exit] 1))
                     "\n") #"invalid evidence receipts"]
               [(str base-text (pr-str current)) #"must end with a newline"]
               [(str base-text "{:kind\n") #"Invalid Receipt River EDN"]]]
        (fs/writeFileSync file held-text "utf8")
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
          (is (not (fs/existsSync
                    (path/join directory ".receipts.edn.append.lock")))))))))

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
          (let [head (commit! (str base-text (pr-str current) "\n"))]
            (is (cli/promotion-ready-at!
                 child-revision #{:repo/unit} [passed-result] base head)))
          (let [head (commit!
                      (str base-text (pr-str current) "\n"
                           (pr-str (assoc-in current
                                             [:evidence/result :result/exit] 1))
                           "\n"))]
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
                  cli/appended-receipt-records!
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
                  cli/appended-receipt-records! (fn [& _] [])
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
