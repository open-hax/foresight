(ns alpha.law.shared-invocation-test
  (:require [alpha.law.artifact :as alpha]
            [katamorph.action.invocation :as invocation]
            #?(:clj [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is]])))

(def request
  {:operation/id :evaluation/open-case
   :operation/with {:rubric :translation/sme-v1}
   :operation/in {:subject [:event :subject]}})

(deftest alpha-operation-ref-is-the-katamorph-invocation-contract
  (is (invocation/valid? request))
  (is (alpha/valid-shape? :alpha/operation-ref request)))

(deftest runtime-provider-data-is-rejected-at-both-boundaries
  (let [value (assoc request :operation/provider :provider/local)]
    (is (false? (invocation/valid? value)))
    (is (false? (alpha/valid-shape? :alpha/operation-ref value)))))

(deftest action-reference-ids-are-portable-contract-ids
  (is (false?
       (alpha/valid-shape?
        :alpha/operation-ref
        {:operation/id 42}))))
