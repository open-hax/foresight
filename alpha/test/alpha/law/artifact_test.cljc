(ns alpha.law.artifact-test
  (:require [alpha.law.artifact :as law]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(def epiphany-finding
  {:artifact/id "finding/document-reactive-work"
   :artifact/kind :finding
   :artifact/form :markdown
   :artifact/status :provisional
   :artifact/epistemic-tier :derived
   :artifact/data {:confidence 0.84
                   :method :comparative-analysis
                   :anything-project-specific {:is-preserved true}}})

(def rheos-story
  {:artifact/id "story/generalize-rheos"
   :artifact/kind :story
   :artifact/form :markdown
   :artifact/status :ready
   :artifact/data {:priority :P1
                   :points 5
                   :dependency ["alpha-kernel"]}})

(def calliope-review
  {:artifact/id "review/render-17"
   :artifact/kind :media-review
   :artifact/status :accepted
   :artifact/data {:subject {:type :render :id "render-17"}
                   :ratings {:enjoyment 4 :weirdness 5}
                   :labels [:salvage/good-intro]}})

(def operation-registry
  {:verification/open-case {:operation/category :evaluation}
   :artifact/archive {:operation/category :store}})

(deftest unrelated-artifact-kinds-share-the-base-law
  (testing "Epiphany, Rheos, and Calliope specimens do not need one giant schema"
    (is (law/artifact? epiphany-finding))
    (is (law/artifact? rheos-story))
    (is (law/artifact? calliope-review))))

(deftest markdown-preserves-arbitrary-frontmatter
  (is (law/valid-shape?
       :alpha/markdown-document
       {:document/path "docs/example.md"
        :document/frontmatter {:kind :finding
                               :custom-field {:nested [1 2 3]}}
        :document/body "# Anything"
        :document/structure [{:heading 1 :text "Anything"}]})))

(deftest embedded-relations-belong-to-the-containing-artifact
  (let [good (assoc epiphany-finding
                    :artifact/relations
                    [{:relation/type :supports
                      :relation/source {:ref/type :artifact
                                        :ref/id "finding/document-reactive-work"}
                      :relation/target {:ref/type :artifact
                                        :ref/id "design/reactive-docs"}}])
        bad (assoc-in good
                      [:artifact/relations 0 :relation/source :ref/id]
                      "some-other-artifact")]
    (is (law/artifact? good))
    (is (false? (:valid? (law/validate-artifact bad))))
    (is (= :alpha/artifact-owns-embedded-relation-source
           (-> (law/validate-artifact bad) :law-errors first :law/id)))))

(deftest external-events-can-trigger-declarative-reactions
  (let [event {:event/id "github-243-merged"
               :event/type :github/pr-merged
               :event/source {:ref/type :github/repository
                              :ref/id "open-hax/knoxx"}
               :event/subject {:ref/type :github/pr
                               :ref/id 243}}
        reaction {:reaction/id :docs/open-verification
                  :reaction/on {:event/type :github/pr-merged}
                  :reaction/when {:condition/op :and
                                  :condition/clauses
                                  [{:condition/op :exists
                                    :condition/path [:event :subject]}
                                   {:condition/op :not-eq
                                    :condition/path [:event :data :draft?]
                                    :condition/value true}]}
                  :reaction/do {:operation/id :verification/open-case
                                :operation/with {:profile :publication}
                                :operation/in {:subject [:event :subject]}}}]
    (is (law/event? event))
    (is (law/reaction? operation-registry reaction))))

(deftest reactions-require-typed-operation-refs
  (is (false?
       (law/reaction-shape?
        {:reaction/id :invalid
         :reaction/on {:event/type :artifact/accepted}
         :reaction/do "untyped executable value"}))))

(deftest reactions-require-registered-operations
  (let [reaction {:reaction/id :invalid-operation
                  :reaction/on {:event/type :artifact/accepted}
                  :reaction/do {:operation/id :operation/not-registered}}
        result (law/validate-reaction operation-registry reaction)]
    (is (false? (:valid? result)))
    (is (= :alpha/reaction-operation-registered
           (-> result :law-errors first :law/id)))
    (is (= :operation/not-registered
           (-> result :law-errors first :actual)))))

(deftest operation-refs-reject-embedded-runtime-values
  (testing "implementation fields cannot ride along on an open operation map"
    (is (false?
         (law/reaction-shape?
          {:reaction/id :embedded-implementation
           :reaction/on {:event/type :artifact/accepted}
           :reaction/do {:operation/id :artifact/archive
                         :operation/implementation (fn [] :runtime)}}))))
  (testing "configuration and inputs must remain recursively portable data"
    (is (false?
         (law/reaction-shape?
          {:reaction/id :runtime-argument
           :reaction/on {:event/type :artifact/accepted}
           :reaction/do {:operation/id :artifact/archive
                         :operation/with {:callback (fn [] :runtime)}}}))))
  (testing "nested EDN-like values remain lawful"
    (is (law/reaction?
         operation-registry
         {:reaction/id :portable-arguments
          :reaction/on {:event/type :artifact/accepted}
          :reaction/do {:operation/id :artifact/archive
                        :operation/with {:policy {:mode :append
                                                 :retries 2
                                                 :labels #{:durable :portable}}}
                        :operation/in {:artifact [:step :load :artifact]}}}))))

(deftest diagram-source-is-first-class-code-data
  (is (law/valid-shape?
       :alpha/diagram-source
       {:diagram/id :workflow/alpha-eta-mu-pi
        :diagram/language :mermaid
        :diagram/source "flowchart LR\n  A --> B\n"})))
