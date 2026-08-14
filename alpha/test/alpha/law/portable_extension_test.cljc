(ns alpha.law.portable-extension-test
  (:require [alpha.law.artifact :as law]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(deftest portable-project-extensions-remain-lawful
  (is (law/artifact?
       {:artifact/id :finding/id-1
        :artifact/kind :finding
        :project/custom {:confidence 0.84
                         :labels #{:research :portable}}}))
  (is (law/event?
       {:event/id :event/id-1
        :event/type :artifact/changed
        :project/context {:actor :agent/reviewer}}))
  (is (law/valid-shape?
       :alpha/ref
       {:ref/type :artifact
        :ref/id :finding/id-1
        :project/provenance {:source "research.md"}})))

(deftest runtime-values-cannot-hide-in-open-artifact-fields
  (is (false?
       (law/artifact?
        {:artifact/id :finding/runtime-leak
         :artifact/kind :finding
         :project/callback (fn [] :runtime)}))))

(deftest runtime-values-cannot-hide-in-open-event-or-ref-fields
  (is (false?
       (law/event?
        {:event/id :event/runtime-leak
         :event/type :artifact/changed
         :runtime/handle (fn [] :runtime)})))
  (is (false?
       (law/valid-shape?
        :alpha/ref
        {:ref/type :artifact
         :ref/id :finding/id-1
         :runtime/handle (fn [] :runtime)}))))

(deftest runtime-values-cannot-hide-in-open-reaction-fields
  (let [reaction {:reaction/id :review/open
                  :reaction/on {:event/type :artifact/changed}
                  :reaction/do {:operation/id :evaluation/open-case}
                  :project/runtime (fn [] :runtime)}]
    (is (false? (law/reaction-shape? reaction)))))

(deftest untyped-document-structure-is-still-portability-bounded
  (testing "portable AST-like values remain allowed while the detailed AST contract is undecided"
    (is (law/valid-shape?
         :alpha/markdown-document
         {:document/path "docs/example.md"
          :document/frontmatter {:kind :finding}
          :document/body "# Finding"
          :document/structure [{:node/type :heading
                                :node/level 1
                                :node/text "Finding"}]})))
  (testing "host values cannot use the untyped structure field as an escape hatch"
    (is (false?
         (law/valid-shape?
          :alpha/markdown-document
          {:document/path "docs/example.md"
           :document/frontmatter {:kind :finding}
           :document/body "# Finding"
           :document/structure [{:node/runtime (fn [] :runtime)}]})))))

(deftest host-objects-fail-even-in-unknown-extension-fields
  #?(:cljs
     (is (false?
          (law/artifact?
           {:artifact/id :artifact/host
            :artifact/kind :finding
            :project/host #js {:connected true}})))
     :clj
     (is (false?
          (law/artifact?
           {:artifact/id :artifact/host
            :artifact/kind :finding
            :project/host (Object.)})))))
