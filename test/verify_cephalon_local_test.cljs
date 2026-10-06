(ns verify-cephalon-local-test
  "Exercise diagnostic admission and exit behavior without a deployment or APIs."
  (:require [cljs.test :as test :refer [deftest is testing]]
            [verify-cephalon-local :as diagnostic]
            ["node:crypto" :as crypto]
            ["node:fs" :as fs]
            ["node:os" :as os]
            ["node:path" :as path]))

(def contract-bytes "fixture contract, not a deployed resource")
(def contract-hash
  (.digest (.update (.createHash crypto "sha256") contract-bytes) "hex"))
(def manifest
  {:runtimeImageId "sha256:fixture-image"
   :files [{:path "fixture.edn" :sha256 contract-hash}]
   :operationalGaps []})

(def observed
  {:unauth 401
   :selfControlIdentity true
   :runs [{:status "completed"
           :created_at (.toISOString (js/Date.))
           :settings {:agentSpec {:eventType "schedule/ussyverse-social-creative"
                                  :scheduleId "creative"}}}]
   :schedules [{:rule "*/15 * * * *"}]
   :triggers [{:enabled true :events ["creative-request"]}]
   :headTools ["discord.send" "discord.read" "discord.react" "agents.spawn"]
   :makerTools ["bash" "write" "music.generate" "bluesky.publish" "discord.send"]
   :synthesisScript true
   :publicationReadOk true
   :publications [{:uri "at://fixture/image" :images 1}]
   :active []})

(defn inspect-fixture
  "Use real manifest bytes and stub only the external command boundary."
  [snapshot runtime & arguments]
  (let [directory (.mkdtempSync fs (path/join (.tmpdir os) "cephalon-verifier-"))
        commands (atom [])
        exit (atom nil)]
    (try
      (.mkdirSync fs (path/join directory "contracts"))
      (.writeFileSync fs (path/join directory "contracts" "fixture.edn") contract-bytes)
      (.writeFileSync fs (path/join directory "contracts-manifest.json")
                      (js/JSON.stringify (clj->js snapshot)))
      (let [output (with-redefs [diagnostic/deployment directory
                                diagnostic/failures (atom 0)
                                diagnostic/command!
                                (fn [executable command input]
                                  (swap! commands conj [executable command])
                                  (case (first command)
                                    "inspect" "sha256:fixture-image\n"
                                    "exec" (do
                                             (is (string? input))
                                             (js/JSON.stringify (clj->js runtime)))
                                    (throw (ex-info "Unexpected fixture command" {}))))]
                     (with-out-str
                       (reset! exit (apply diagnostic/-main arguments))))]
        {:exit @exit :commands @commands :output output})
      (finally
        (.rmSync fs directory #js {:recursive true :force true})))))

(defn inspect [snapshot runtime]
  (inspect-fixture snapshot runtime "--only" "knoxx"))

(deftest explicit-selection-before-inspection
  (let [result (inspect-fixture manifest observed)]
    (is (= 1 (:exit result)))
    (is (empty? (:commands result)))))

(deftest reject-wrong-image-before-runtime
  (let [result (inspect (assoc manifest :runtimeImageId "sha256:wrong") observed)]
    (is (= 1 (:exit result)))
    (is (= ["inspect"] (mapv #(first (second %)) (:commands result))))))

(deftest reject-missing-or-empty-manifest-before-runtime
  (doseq [snapshot [(dissoc manifest :files)
                    (assoc manifest :files [])
                    (assoc manifest :files {})]]
    (let [result (inspect snapshot observed)]
      (is (= 1 (:exit result)))
      (is (= ["inspect"] (mapv #(first (second %)) (:commands result)))))))

(deftest reject-contract-hash-drift-before-runtime
  (let [result (inspect (assoc-in manifest [:files 0 :sha256] "wrong") observed)]
    (is (= 1 (:exit result)))
    (is (= ["inspect"] (mapv #(first (second %)) (:commands result))))))

(deftest successful-inspection-and-text-only-window
  (testing "mixed media proves a sampled image observation"
    (let [result (inspect manifest observed)]
      (is (= 0 (:exit result)))
      (is (= ["inspect" "exec"] (mapv #(first (second %)) (:commands result))))))
  (testing "a bounded feed window cannot require image frequency"
    (doseq [publications [[{:uri "at://fixture/music-1" :images 0}
                          {:uri "at://fixture/music-2" :images 0}
                          {:uri "at://fixture/music-3" :images 0}]
                         []]]
      (let [result (inspect manifest (assoc observed :publications publications))]
        (is (= 0 (:exit result)))
        (is (re-find #"WARN No image post observed in the sampled Bluesky feed" (:output result)))))))

(deftest unavailable-publication-read-remains-failure
  (let [result (inspect manifest (assoc observed :publicationReadOk false))]
    (is (= 1 (:exit result)))
    (is (re-find #"FAIL public Bluesky feed read succeeded with a valid feed" (:output result)))))

(deftest malformed-publication-feed-remains-failure
  (let [result (inspect manifest (assoc observed :publications nil))]
    (is (= 1 (:exit result)))))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (when (pos? (+ (:fail summary) (:error summary)))
    (set! (.-exitCode js/process) 1)))

(test/run-tests 'verify-cephalon-local-test)
