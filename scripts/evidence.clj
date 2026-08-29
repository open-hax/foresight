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

(def max-receipt-line-bytes (* 1024 1024))

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
                #js {:cwd root :encoding "utf8" :shell false})
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
                #js {:cwd root :shell false})
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

(defn read-receipt-records! [contents]
  (->> (str/split-lines contents)
       (remove str/blank?)
       (map-indexed
        (fn [index line]
          (try
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
        base-ledger (read-immutable-receipt-ledger! trusted-base-revision)
        ledger (read-immutable-receipt-ledger! reviewed-root-revision)]
    (appended-receipt-records! (:ledger/bytes base-ledger)
                               (:ledger/bytes ledger))
    (require-valid-receipt-records! (:ledger/records ledger))
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

(defn spawn-result [cwd command]
  (child-process/spawnSync
   (first command)
   (clj->js (rest command))
   #js {:cwd cwd :encoding "utf8" :stdio "inherit" :shell false}))

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

(defn open-parent-directory! [directory]
  (let [initial-stats (lstat-if-present! directory "Receipt River parent directory")]
    (when-not initial-stats
      (append-error! "Receipt River parent directory does not exist"))
    (require-directory-stats! initial-stats "Receipt River parent directory")
    (let [fd (fs/openSync
              directory
              (bit-or (fs-constant! "O_RDONLY")
                      (fs-constant! "O_DIRECTORY")
                      (fs-constant! "O_NOFOLLOW")))]
      (try
        (let [fd-stats (fs/fstatSync fd bigint-stat-options)
              proc-path (str "/proc/self/fd/" fd)]
          (require-directory-stats! fd-stats "Receipt River parent descriptor")
          (require-path-identity! directory fd-stats
                                  "Receipt River parent directory"
                                  require-directory-stats!)
          (let [proc-stats (fs/statSync proc-path bigint-stat-options)]
            (when-not (and (.isDirectory proc-stats)
                           (same-file? proc-stats fd-stats))
              (append-error! "/proc/self/fd did not resolve to the held parent descriptor")))
          {:directory directory
           :fd fd
           :stats fd-stats
           :proc-path proc-path})
        (catch :default error
          (close-after-open-error! fd error))))))

(defn require-parent-identity! [{:keys [directory stats]}]
  (require-path-identity! directory stats
                          "Receipt River parent directory"
                          require-directory-stats!))

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

(defn acquire-append-lock! [parent basename]
  (require-parent-identity! parent)
  (let [lock-path (append-lock-path parent basename)
        flags (bit-or (fs-constant! "O_WRONLY")
                      (fs-constant! "O_CREAT")
                      (fs-constant! "O_EXCL")
                      (fs-constant! "O_NOFOLLOW"))
        fd (try
             (fs/openSync lock-path flags 384)
             (catch :default error
               (if (= "EEXIST" (.-code error))
                 (append-error!
                  "another writer holds the append lock, or a stale/unsafe lock exists")
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
        (close-after-open-error! fd error)))))

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

(defn require-safe-existing-target! [target-path]
  (when-let [stats (lstat-if-present! target-path "Receipt River file")]
    (require-regular-stats! stats "Receipt River file")))

(defn open-append-target! [parent target-path]
  (require-parent-identity! parent)
  (require-safe-existing-target! target-path)
  (let [flags (bit-or (fs-constant! "O_RDWR")
                      (fs-constant! "O_APPEND")
                      (fs-constant! "O_CREAT")
                      (fs-constant! "O_NOFOLLOW")
                      (fs-constant! "O_NONBLOCK"))
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
        (close-after-open-error! fd error)))))

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
    (let [parsed (try
                   (reader/read-string line)
                   (catch :default error
                     (append-error! (str "receipt is not readable EDN: "
                                         (.-message error)))))]
      (when-not (= record parsed)
        (append-error! "receipt does not round-trip through EDN")))
    (let [bytes (.encode (js/TextEncoder.) line)]
      (when (> (.-byteLength bytes) max-receipt-line-bytes)
        (append-error! (str "receipt exceeds " max-receipt-line-bytes
                            " UTF-8 bytes")))
      line)))

(defn append-edn-line! [file record]
  (require-secure-append-support!)
  (let [absolute-file (path/resolve file)
        directory (path/dirname absolute-file)
        basename (path/basename absolute-file)
        line (encode-edn-line! record)
        parent (open-parent-directory! directory)]
    (try
      (let [lock (acquire-append-lock! parent basename)
            write-started? (atom false)
            write-verified? (atom false)]
          (try
            (require-append-lock-identity! lock)
            (let [target-path (path/join (:proc-path parent) basename)
                  target (open-append-target! parent target-path)
                  result
                  (try
                    (*secure-append-phase-hook*
                     :before-write
                     {:requested-file absolute-file
                      :directory directory
                      :target-path target-path})
                    (require-parent-identity! parent)
                    (require-append-lock-identity! lock)
                    (let [before (require-target-identity! target)
                          before-size (safe-file-size! before)
                          separator (if (terminal-newline? (:fd target) before-size)
                                      ""
                                      "\n")
                          payload (.encode (js/TextEncoder.)
                                           (str separator line "\n"))]
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
                      (require-parent-identity! parent)
                      (*secure-append-phase-hook*
                       :after-write
                       {:requested-file absolute-file
                        :directory directory
                        :target-path target-path})
                      (require-parent-identity! parent)
                      (require-append-lock-identity! lock)
                      (require-target-identity! target)
                      record)
                    (finally
                      (fs/closeSync (:fd target))))]
              (reset! write-verified? true)
              result)
            (finally
              (if (and @write-started? (not @write-verified?))
                ;; A short write, fsync failure, or post-write identity change may
                ;; have left a partial ledger line. Retain the lock as a durable
                ;; quarantine marker so a later writer cannot normalize the damage.
                (fs/closeSync (:fd lock))
                (release-append-lock! parent lock)))))
      (finally
        (fs/closeSync (:fd parent))))))

(defn append-evidence-receipt! [result]
  (let [receipt (evidence-receipt result
                                  (.toISOString (js/Date.))
                                  (os/hostname)
                                  (runtime-adapter))]
    (when-not (law/evidence-receipt? receipt)
      (throw (js/Error.
              (str "Gate result cannot be recorded as an evidence receipt: "
                   (pr-str (law/result-errors result))))))
    (append-edn-line! receipt-file receipt)))

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

(defn run-selected-gates! [catalog catalog-identity {:keys [only kinds]}]
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
                            (let [result (run-gate! (get paths repository-path)
                                                    gate
                                                    catalog-identity)]
                              (append-evidence-receipt! result)
                              result)))
                        gates)
          summary (law/summarize-results results)]
      (println "SUMMARY" (pr-str summary))
      (reduce max 0 (map result-exit results)))))

(defn buffer-prefix? [prefix value]
  (and (<= (.-length prefix) (.-length value))
       (.equals prefix (.subarray value 0 (.-length prefix)))))

(defn appended-receipt-records! [base-bytes head-bytes]
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
  (let [base-ledger (read-immutable-receipt-ledger! base)
        ledger (read-immutable-receipt-ledger! at)
        records (:ledger/records ledger)
        appended-records (appended-receipt-records!
                          (:ledger/bytes base-ledger)
                          (:ledger/bytes ledger))
        candidates (filter #(= law/evidence-receipt-origin (:origin %)) records)
        evidence-count (count candidates)
        counts (require-valid-receipt-records! records)]
    (println "PASS"
             (pr-str (assoc (:ledger/identity ledger)
                            :ledger/base-revision base
                            :ledger/total-receipts (count records)
                            :ledger/appended-receipts (count appended-records)
                            :ledger/legacy-evidence-receipts
                            (:receipt/legacy-evidence counts)
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
