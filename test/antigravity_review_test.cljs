;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns antigravity-review-test
  (:require [cljs.test :as test :refer [deftest is testing]]
            [antigravity-rollout :as rollout]
            [clojure.string :as str]
            [foresight.antigravity-review :as review]
            [foresight.project :as project]
            ["child_process" :as child-process]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]))

(def revision (apply str (repeat 40 "a")))
(def previous-revision (apply str (repeat 40 "b")))
(def same-repository-event
  {:repository {:full_name "open-hax/foresight"}
   :pull_request {:draft false :head {:repo {:full_name "open-hax/foresight"}}}})

(deftest selects-only-root-and-actionable-independent-members
  (let [targets (review/targets project/sources)
        paths (set (map :path targets))]
    (is (= 23 (count targets)))
    (is (contains? paths "."))
    (is (every? paths ["eta-mu" "Truth" "clio" "axxium"]))
    (is (not-any? paths [".agents" "eta" "clobber"]))
    (is (= [{:path "." :repository "open-hax/foresight"}]
           (review/targets [(assoc (first project/sources)
                                   :source/actionable? true
                                   :source/consolidation? false)])))
    (doseq [change [{:source/actionable? false}
                    {:source/consolidation? true}
                    {:source/ownership :workspace-root}
                    {:source/type :native-component}]]
      (is (= [{:path "." :repository "open-hax/foresight"}]
             (review/targets [(merge (second project/sources) change)]))))))

(deftest requires-an-immutable-revision
  (is (review/revision? revision))
  (doseq [value [nil "" "main" "v1.0.0" "abc123" (str revision "a")
                 (str/upper-case revision)]]
    (is (not (review/revision? value)) (pr-str value))
    (is (thrown? js/Error (review/render-workflow value)))))

(deftest rendered-caller-pins-read-only-review-contract
  (let [text (review/render-workflow revision)]
    (doseq [required [(str "antigravity-code-review.yml@" revision)
                      (str "review_runtime_revision: " revision)
                      "pr_head_sha: ${{ github.event.pull_request.head.sha }}"
                      "pr_base_sha: ${{ github.event.pull_request.base.sha }}"
                      "model: gemini-3.8-flash-high"
                      "types: [opened, synchronize, reopened, ready_for_review, converted_to_draft, edited]"
                      "github.event.pull_request.head.repo.full_name == github.repository"
                      "github.event.action != 'edited' || github.event.changes.base.ref.from != ''"
                      "cancel-in-progress: true"
                      "contents: read" "pull-requests: read"]]
      (is (str/includes? text required) required))
    (doseq [forbidden ["secrets:" "write" "pull_request_target" "checkout@"
                       "npm" "pnpm" "run:"]]
      (is (not (str/includes? text forbidden)) forbidden))))

(deftest retarget-events-review-the-new-base-without-replaying-title-or-body-edits
  (let [event same-repository-event]
    (doseq [action ["opened" "synchronize" "reopened" "ready_for_review"]]
      (is (review/review-event? (assoc event :action action)) action))
    (is (review/review-event? (assoc event :action "edited"
                                    :changes {:base {:ref {:from "main"}}})))
    (doseq [changes [nil {} {:title {:from "Old title"}} {:body {:from "Old body"}}
                     {:base {:sha {:from revision}}} {:base {:ref {}}}
                     {:base {:ref {:from nil}}} {:base {:ref {:from ""}}}]]
      (is (not (review/review-event? (assoc event :action "edited" :changes changes)))
          (pr-str changes)))
    (is (review/review-event? (assoc event :action "edited"
                                    :changes {:base {:ref {:from "release/previous"}}
                                              :title {:from "Old title"}})))
    (is (not (review/review-event? {:action "edited" :pull_request {:draft true}
                                   :changes {:base {:ref {:from "main"}}}})))
    (doseq [action ["closed" "labeled" "assigned" nil]]
      (is (not (review/review-event? (assoc event :action action))) (pr-str action)))))

(deftest previous-generated-trigger-format-remains-managed
  (doseq [repository ["open-hax/eta-mu" "octave-commons/muse"]]
    (let [prior (-> (review/render-workflow previous-revision revision repository)
                    (str/replace "ready_for_review, converted_to_draft, edited" "ready_for_review, edited")
                    (str/replace "github.event.pull_request.head.repo.full_name == github.repository && " ""))
          retarget (str/replace prior
                                "-${{ (github.event.action != 'edited' || github.event.changes.base.ref.from != '') && 'review' || github.run_id }}"
                                "")
          old (-> retarget
                  (str/replace "types: [opened, synchronize, reopened, ready_for_review, edited]"
                               "types: [opened, synchronize, reopened, ready_for_review]")
                  (str/replace "${{ github.event.pull_request.draft == false && (github.event.action != 'edited' || github.event.changes.base.ref.from != '') }}"
                               "${{ github.event.pull_request.draft == false }}"))]
      (is (= :revision-mismatch
             (review/workflow-status prior previous-revision revision repository)))
      (is (= :revision-mismatch
             (review/workflow-status old previous-revision revision repository)))
      (is (= :revision-mismatch
             (review/workflow-status retarget previous-revision revision repository)))
      (is (= :unmanaged
             (review/workflow-status (str old "# unrelated edit\n")
                                     previous-revision revision repository))))))

(defn evaluate-generated-expression [expression event run-id]
  (let [normalized (assoc-in event [:changes :base :ref :from]
                             (or (get-in event [:changes :base :ref :from]) ""))
        context {:workflow "Antigravity evidence review" :run_id run-id
                 :repository (get-in event [:repository :full_name])
                 :event (assoc-in normalized [:pull_request :number] 42)}]
    ((js/Function. "github" (str "return (" expression ");")) (clj->js context))))

(deftest actual-generated-concurrency-isolates-ineligible-edits-before-job-admission
  (let [workflow (review/render-workflow revision)
        group-template (second (re-find #"  group: ([^\n]+)" workflow))
        condition (second (re-find #"    if: \$\{\{\s*(.*?)\s*\}\}" workflow))
        group (fn [event run-id]
                (str/replace group-template #"\$\{\{\s*(.*?)\s*\}\}"
                             (fn [[_ expression]]
                               (str (evaluate-generated-expression expression event run-id)))))
        event (assoc same-repository-event :action "opened")
        review-group (group event "100")
        cases [["opened" nil true] ["synchronize" nil true]
               ["reopened" nil true] ["ready_for_review" nil true]
               ["edited" {:base {:ref {:from "main"}}} true]
               ["edited" {:title {:from "old"}} false]
               ["edited" {:body {:from "old"}} false]
               ["edited" {:base {:sha {:from revision}}} false]
               ["edited" {:base {:ref {:from ""}}} false]
               ["edited" nil false]]]
    (is (str/ends-with? review-group "-review"))
    (doseq [[action changes eligible?] cases]
      (let [input (assoc event :action action :changes changes)
            first-group (group input "100")
            second-group (group input "101")]
        (is (= eligible? (evaluate-generated-expression condition input "100")) (pr-str input))
        (if eligible?
          (is (= review-group first-group second-group) (pr-str input))
          (do
            (is (= 3 (count (set [review-group first-group second-group]))) (pr-str input))
            (is (str/ends-with? first-group "-100"))
            (is (str/ends-with? second-group "-101"))))))))

(deftest unsupported-heads-skip-before-calling-the-shared-reviewer
  (let [event (assoc same-repository-event :action "opened")
        fork (assoc-in event [:pull_request :head :repo :full_name] "contributor/foresight")
        condition (second (re-find #"    if: \$\{\{\s*(.*?)\s*\}\}"
                                   (review/render-workflow revision)))]
    (is (review/review-event? event))
    (doseq [input [fork (update-in event [:pull_request :head] dissoc :repo)
                   (dissoc event :repository)]]
      (is (false? (review/review-event? input))))
    (is (false? (evaluate-generated-expression condition fork "100")))))

(deftest draft-conversion-cancels-the-pr-group-without-admitting-another-review
  (let [workflow (review/render-workflow revision)
        group-template (second (re-find #"  group: ([^\n]+)" workflow))
        group (fn [event]
                (str/replace group-template #"\$\{\{\s*(.*?)\s*\}\}"
                             (fn [[_ expression]]
                               (str (evaluate-generated-expression expression event "100")))))
        opened (assoc same-repository-event :action "opened")
        drafted (-> same-repository-event
                    (assoc :action "converted_to_draft")
                    (assoc-in [:pull_request :draft] true))
        condition (second (re-find #"    if: \$\{\{\s*(.*?)\s*\}\}" workflow))]
    (is (some #{"converted_to_draft"} review/pull-request-actions))
    (is (= (group opened) (group drafted)))
    (is (false? (review/review-event? drafted)))
    (is (false? (evaluate-generated-expression condition drafted "100")))))

(deftest runtime-pin-can-precede-the-workflow-pin
  (let [text (review/render-workflow revision previous-revision)]
    (is (str/includes? text (str "antigravity-code-review.yml@" revision)))
    (is (str/includes? text (str "review_runtime_revision: " previous-revision)))
    (is (= :matching (review/workflow-status text revision previous-revision)))
    (is (= :revision-mismatch (review/workflow-status text revision revision)))
    (is (thrown? js/Error (review/render-workflow revision "main")))
    (is (= previous-revision
           (:runtime-revision
             (review/parse-options ["plan" "--only" "." "--revision" revision
                                    "--runtime-revision" previous-revision]))))
    (is (thrown? js/Error
                 (review/parse-options ["plan" "--revision" revision
                                        "--runtime-revision" "main"])))))

(deftest workflow-routing-follows-repository-identity
  (let [routed (review/route-targets (review/targets project/sources)
                                    {:revision revision :octave-revision previous-revision})
        octave (filter #(= "octave-commons/muse" (:workflow-repo %)) routed)
        canonical (filter #(= "open-hax/eta-mu" (:workflow-repo %)) routed)]
    (is (= 6 (count octave)))
    (is (= 17 (count canonical)))
    (is (= #{"Truth" "bitch-tracker" "calliope" "epiphany" "muse" "shx"}
           (set (map :path octave))))
    (is (every? #(= previous-revision (:workflow-revision %)) octave))
    (is (every? #(= revision (:workflow-revision %)) canonical))
    (is (= "octave-commons/muse"
           (:workflow-repo (first (review/route-targets
                                   [{:path "clio" :repository "octave-commons/example"}]
                                   {:revision revision :octave-revision previous-revision})))))
    (is (thrown? js/Error (review/route-targets (review/targets project/sources)
                                              {:revision revision})))
    (is (thrown? js/Error (review/route-targets [{:path "muse" :repository "unknown/repo"}]
                                              {:revision revision}))))
  (let [mirror (review/render-workflow previous-revision revision "octave-commons/muse")]
    (is (str/includes? mirror (str "uses: octave-commons/muse/.github/workflows/antigravity-code-review.yml@"
                                                   previous-revision)))
    (is (str/includes? mirror (str "review_runtime_revision: " revision)))
    (is (= :matching (review/workflow-status mirror previous-revision revision "octave-commons/muse")))
    (is (= :revision-mismatch (review/workflow-status mirror revision)))
    (is (= :revision-mismatch (review/workflow-status (review/render-workflow revision)
                                                      previous-revision revision "octave-commons/muse")))
    (is (thrown? js/Error (review/render-workflow revision revision "attacker/muse")))
    (is (= :unmanaged (review/workflow-status
                       (str/replace mirror "octave-commons/muse" "attacker/muse") revision))))
  (is (thrown? js/Error (review/parse-options ["plan" "--revision" revision])))
  (is (thrown? js/Error (review/parse-options ["plan" "--revision" revision
                                             "--octave-revision" "main"])))
  (is (= previous-revision
         (:octave-revision (review/parse-options ["plan" "--revision" revision
                                                  "--octave-revision" previous-revision])))))

(deftest file-ownership-and-revision-drift-are-distinct
  (let [expected (review/render-workflow revision)
        previous (review/render-workflow previous-revision)]
    (is (= :absent (review/workflow-status nil revision)))
    (is (= :matching (review/workflow-status expected revision)))
    (is (= :revision-mismatch (review/workflow-status previous revision)))
    (is (= :unmanaged (review/workflow-status "name: Existing reviewer\n" revision)))
    (is (= :unmanaged (review/workflow-status (str previous "# unexpected edit\n") revision)))
    (is (= :unmanaged (review/workflow-status
                       (str/replace previous "contents: read" "contents: write")
                       revision)))))

(deftest selection-and-cli-options-fail-closed
  (let [targets (review/targets project/sources)]
    (is (= 23 (count (review/select-targets targets {}))))
    (is (= ["." "clio"]
           (mapv :path (review/select-targets targets {:only ["clio" "."]}))))
    (doseq [options [{:only ["../clio"]} {:only [".agents"]}
                     {:only ["eta"]} {:only ["clio"] :all? true}]]
      (is (thrown? js/Error (review/select-targets targets options))))
    (is (thrown? js/Error (review/parse-options ["apply" "--revision" revision])))
    (is (thrown? js/Error (review/parse-options ["apply" "--all"])))
    (is (thrown? js/Error (review/parse-options ["plan" "--revision" "main"])))
    (is (thrown? js/Error (review/parse-options ["apply" "--all" "--revision"])))
    (is (thrown? js/Error (review/parse-options ["plan" "--revision" revision "--unknown"])))
    (testing "checkout overrides cannot widen the selected declaration"
      (is (thrown? js/Error
                   (review/parse-options ["apply" "--revision" revision "--only" "clio"
                                          "--checkout" "knoxx=/tmp/knoxx"]))))
    (is (= {:command "apply" :revision revision :only ["clio"]
            :checkouts {"clio" "/tmp/clio"}}
           (review/parse-options ["apply" "--revision" revision "--only" "clio"
                                  "--checkout" "clio=/tmp/clio"])))
    (doseq [args [["plan" "--revision" revision "--only" ""]
                  ["plan" "--revision" revision "--checkout" "../escape=/tmp/repo"]
                  ["plan" "--revision" revision "--checkout" "clio=relative"]
                  ["plan" "--revision" revision "--revision" previous-revision]]]
      (is (thrown? js/Error (review/parse-options args)) (pr-str args)))))

(defn fixture-repository! [directory repository]
  (fs/mkdirSync directory #js {:recursive true})
  (child-process/execFileSync "git" #js ["init" "--quiet" directory])
  (child-process/execFileSync "git"
                             #js ["-C" directory "remote" "add" "origin"
                                  (str "https://github.com/" repository ".git")]))

(deftest adapter-renders-independent-organization-pins
  (let [temporary (fs/mkdtempSync (path/join (os/tmpdir) "antigravity-routes-test-"))
        root (path/join temporary "foresight")
        shx (path/join temporary "shx-worktree")
        options {:root root :only ["." "shx"] :checkouts {"shx" shx}
                 :revision revision :octave-revision previous-revision}]
    (try
      (fixture-repository! root "open-hax/foresight")
      (fixture-repository! shx "octave-commons/shx")
      (let [plan (rollout/plan options)]
        (is (= ["open-hax/eta-mu" "octave-commons/muse"]
               (mapv :workflow-repo (:targets plan))))
        (is (= [revision previous-revision] (mapv :workflow-revision (:targets plan))))
        (is (= revision (:runtime-revision plan)))
        (rollout/apply-plan! plan)
        (is (= (review/render-workflow revision)
               (fs/readFileSync (path/join root review/workflow-path) "utf8")))
        (is (= (review/render-workflow previous-revision revision "octave-commons/muse")
               (fs/readFileSync (path/join shx review/workflow-path) "utf8")))
        (is (every? #(= :matching (:workflow-status %)) (:targets (rollout/plan options)))))
      (finally (fs/rmSync temporary #js {:recursive true :force true})))))

(deftest adapter-scopes-writes-and-exposes-unavailable-targets
  (let [temporary (fs/mkdtempSync (path/join (os/tmpdir) "antigravity-rollout-test-"))
        root (path/join temporary "foresight")
        clio (path/join temporary "clio-worktree")
        options {:command "apply" :root root :only ["." "clio"]
                 :checkouts {"clio" clio} :revision revision
                 :runtime-revision previous-revision}
        workflow (path/join root review/workflow-path)
        clio-workflow (path/join clio review/workflow-path)]
    (try
      (fixture-repository! root "open-hax/foresight")
      (fixture-repository! clio "open-hax/clio")
      (testing "plan reports missing direct checkout and creates no file"
        (is (= :absent (get-in (rollout/plan (assoc options :only ["sol"] :checkouts {}))
                               [:targets 0 :checkout-status])))
        (is (not (fs/existsSync workflow))))
      (testing "preflight refuses wrong repository identity before any selected write"
        (let [wrong (path/join temporary "wrong")]
          (fixture-repository! wrong "open-hax/wrong")
          (let [plan (rollout/plan (assoc options :checkouts {"clio" wrong}))]
            (is (= :repository-mismatch (get-in plan [:targets 1 :checkout-status])))
            (is (thrown? js/Error (rollout/apply-plan! plan)))
            (is (not (fs/existsSync workflow))))))
      (testing "unknown member workflow blocks the entire selection"
        (fs/mkdirSync (path/dirname clio-workflow) #js {:recursive true})
        (fs/writeFileSync clio-workflow "name: Existing caller\n")
        (is (thrown? js/Error (rollout/apply-plan! (rollout/plan options))))
        (is (not (fs/existsSync workflow)))
        (is (= "name: Existing caller\n" (fs/readFileSync clio-workflow "utf8")))
        (fs/unlinkSync clio-workflow))
      (testing "successful apply is identical across selected worktrees and idempotent"
        (is (= [:created :created]
               (mapv :action (:targets (rollout/apply-plan! (rollout/plan options))))))
        (is (= (review/render-workflow revision previous-revision)
               (fs/readFileSync workflow "utf8")
               (fs/readFileSync clio-workflow "utf8")))
        (is (= [:unchanged :unchanged]
               (mapv :action (:targets (rollout/apply-plan! (rollout/plan options))))))
        (is (= [:updated :updated]
               (mapv :action (:targets (rollout/apply-plan!
                                         (rollout/plan (assoc options :revision previous-revision))))))))
      (testing "workflow-path symlinks cannot escape the checkout"
        (fs/unlinkSync clio-workflow)
        (let [outside (path/join temporary "outside.yml")]
          (fs/writeFileSync outside "outside\n")
          (fs/symlinkSync outside clio-workflow)
          (is (= :unsafe-path (get-in (rollout/plan options) [:targets 1 :workflow-status])))
          (is (thrown? js/Error (rollout/apply-plan! (rollout/plan options))))
          (is (= "outside\n" (fs/readFileSync outside "utf8")))))
      (finally (fs/rmSync temporary #js {:recursive true :force true})))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (if (test/successful? summary) 0 1)))

(test/run-tests 'antigravity-review-test)
