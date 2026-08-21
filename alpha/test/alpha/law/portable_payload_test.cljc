(ns alpha.law.portable-payload-test
  (:require [alpha.law.artifact :as law]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(def portable-payload
  {:policy {:mode :append
            :retries 2
            :labels #{:durable :portable}}
   :history [{:kind :observation :confidence 0.84}
             {:kind :review :accepted true}]
   :note "cross-runtime"
   :missing nil})

(deftest recursively-portable-semantic-maps-remain-lawful
  (is (law/valid-shape? :alpha/portable-map portable-payload))
  (is (law/artifact?
       {:artifact/id :artifact/portable
        :artifact/kind :finding
        :artifact/data portable-payload}))
  (is (law/event?
       {:event/id :event/portable
        :event/type :artifact/changed
        :event/data portable-payload})))

(deftest artifact-data-cannot-carry-runtime-code
  (is (false?
       (law/artifact?
        {:artifact/id :artifact/runtime-leak
         :artifact/kind :finding
         :artifact/data {:callback (fn [] :runtime)}}))))

(deftest event-data-cannot-carry-runtime-code
  (is (false?
       (law/event?
        {:event/id :event/runtime-leak
         :event/type :artifact/changed
         :event/data {:callback (fn [] :runtime)}}))))

(deftest markdown-frontmatter-cannot-carry-runtime-code
  (testing "legacy frontmatter"
    (is (false?
         (law/valid-shape?
          :alpha/markdown-document
          {:document/path "legacy.md"
           :document/frontmatter {:callback (fn [] :runtime)}
           :document/body "# Legacy"}))))
  (testing "lossless decoded frontmatter"
    (is (false?
         (law/valid-shape?
          :alpha/markdown-document
          {:document/format :markdown
           :document/source-path "lossless.md"
           :document/frontmatter-present? true
           :document/frontmatter/raw "callback: opaque"
           :document/frontmatter/data {:callback (fn [] :runtime)}
           :document/body "# Lossless"})))))

(deftest host-object-shaped-values-do-not-become-portable-by-nesting
  #?(:cljs
     (is (false?
          (law/valid-shape?
           :alpha/portable-map
           {:runtime #js {:host true}})))
     :clj
     (is (false?
          (law/valid-shape?
           :alpha/portable-map
           {:runtime (Object.)})))))
