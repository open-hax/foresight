(ns alpha.law.document-test
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.document :as document]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(def markdown-value
  {:document/format :markdown
   :document/source-path "docs/design.md"
   :document/frontmatter-present? true
   :document/frontmatter-raw "title: Design\ncustom:\n  owner: team"
   :document/frontmatter-data {:title "Design" :custom {:owner "team"}}
   :document/body "# Design\n\n```yaml\n---\n```\n"})

(def markdown-observation
  {:observation/schema-version 1
   :observation/id "read-1"
   :observation/source {:ref/type :workspace/file
                        :ref/id "workspace/design"
                        :ref/revision "working-content-1"}
   :observation/observed-at "2026-10-03T12:00:00Z"
   :observation/coverage :full
   :observation/retention :source-owned
   :observation/capabilities #{:read :refresh :write}
   :observation/value "---\ntitle: Design\ncustom:\n  owner: team\n---\n# Design\n\n```yaml\n---\n```\n"
   :observation/content-digest {:digest/algorithm :sha-256
                                :digest/value "declared-content-digest"}})

(def markdown-document
  {:document/schema-version 1
   :document/source (:observation/source markdown-observation)
   :document/observation-id (:observation/id markdown-observation)
   :document/assembly {:assembly/id :markdown/read
                       :assembly/version "1"
                       :assembly/input-schema :input/markdown-v1
                       :assembly/output-schema :alpha/markdown-document}
   :document/value markdown-value})

(def api-observation
  {:observation/schema-version 1
   :observation/id "query-read-1"
   :observation/source {:ref/type :github/query :ref/id "repo-open-issues"}
   :observation/observed-at "2026-10-03T12:00:00Z"
   :observation/coverage :partial
   :observation/retention :ephemeral
   :observation/capabilities #{:read :refresh}
   :observation/context {:query "repo:example/project is:open"
                         :page 1 :has-more? true}
   :observation/value {:issues [{:number 17 :title "Read documents"}]}})

(def api-document
  {:document/schema-version 1
   :document/source (:observation/source api-observation)
   :document/observation-id (:observation/id api-observation)
   :document/assembly {:assembly/id :github/issue-list
                       :assembly/version "1"
                       :assembly/input-schema :input/issue-query-v1
                       :assembly/output-schema :document/issue-list-v1}
   :document/value {:title "Open issues"
                    :items [{:number 17 :title "Read documents"}]}})

(def registry
  (assoc artifact/schemas
         :input/markdown-v1 :string
         :input/issue-query-v1
         [:map {:closed true}
          [:issues [:vector [:map [:number :int] [:title :string]]]]]
         :document/issue-list-v1
         [:map {:closed true}
          [:title :string]
          [:items [:vector [:map [:number :int] [:title :string]]]]]))

(deftest markdown-admission-preserves-the-supplied-maps
  (let [result (document/admit-document registry markdown-observation markdown-document)]
    (is (:ok result))
    (is (= markdown-observation (:observation result)))
    (is (= markdown-document (:document result)))
    (is (= markdown-value (get-in result [:document :document/value])))))

(deftest volatile-partial-api-data-needs-no-file-fields-or-revision
  (let [result (document/admit-document registry api-observation api-document)]
    (is (:ok result))
    (is (= :partial (get-in result [:observation :observation/coverage])))
    (is (= :ephemeral (get-in result [:observation :observation/retention])))
    (is (not (contains? (:document/value api-document) :document/source-path)))
    (is (not (contains? (:document/source api-document) :ref/revision)))
    (is (not (contains? (:observation/capabilities api-observation) :write)))))

(deftest malformed-context-and-runtime-values-are-refused
  (doseq [bad [(dissoc api-observation :observation/id)
               (assoc api-observation :observation/schema-version 2)
               (assoc api-observation :observation/coverage :unknown)
               (assoc api-observation :observation/retention :automatic)
               (assoc api-observation :observation/observed-at "")
               (assoc api-observation :observation/capabilities #{:unqualified-action})
               (assoc-in api-observation [:observation/value :callback] (fn [] :runtime))
               (assoc-in api-observation [:observation/source :host] (fn [] :runtime))]]
    (is (= :observation
           (:stage (document/admit-document registry bad api-document)))))
  (testing "assembled content and provenance also reject embedded executable values"
    (doseq [bad [(assoc-in api-document [:document/value :callback] (fn [] :runtime))
                 (assoc-in api-document [:document/assembly :assembly/version] "")
                 (assoc-in api-document [:document/assembly :execute] (fn [] :runtime))]]
      (is (= :document
             (:stage (document/admit-document registry api-observation bad)))))))

(deftest admission-checks-both-declared-schema-boundaries
  (testing "unknown schemas do not fall back to a generic map"
    (is (= :input
           (:stage (document/admit-document {} api-observation api-document))))
    (is (= :output
           (:stage (document/admit-document
                    (dissoc registry :document/issue-list-v1)
                    api-observation api-document)))))
  (testing "input validity does not establish output validity, or conversely"
    (is (= :input
           (:stage (document/admit-document
                    registry (assoc api-observation :observation/value {:issues "bad"})
                    api-document))))
    (is (= :output
           (:stage (document/admit-document
                    registry api-observation
                    (assoc api-document :document/value {:title "Open issues"})))))))

(deftest selected-observation-and-source-context-must-match
  (testing "an older document cannot silently bind to another supplied observation"
    (let [result (document/admit-document
                  registry (assoc api-observation :observation/id "query-read-2")
                  api-document)]
      (is (= :context (:stage result)))
      (is (= :alpha/document-selected-observation-matches
             (-> result :errors first :law/id)))))
  (testing "another entity or revision is a different source context"
    (doseq [source [(assoc (:document/source markdown-document) :ref/id "another-file")
                    (assoc (:document/source markdown-document) :ref/revision "working-content-2")]]
      (let [result (document/admit-document
                    registry markdown-observation
                    (assoc markdown-document :document/source source))]
        (is (= :context (:stage result)))
        (is (= :alpha/document-observed-source-matches
               (-> result :errors first :law/id)))))))
