(ns alpha.law.reaction-registry-composition-test
  #?(:clj (:require [alpha.law.reaction-registry :as registry]
                    [clojure.test :refer [deftest is]])
     :cljs (:require [alpha.law.reaction-registry :as registry]
                     [cljs.test :refer-macros [deftest is]])))

(def review
  {:reaction/id :review/open
   :reaction/on {:event/type :artifact/changed}
   :reaction/do {:operation/id :evaluation/open-case}})

(def publish
  {:reaction/id :publish/reconcile
   :reaction/on {:event/type :artifact/changed}
   :reaction/do {:operation/id :publication/reconcile}})

(deftest empty-registry-is-identity
  (is (= {:ok true :registry {} :conflicts []} (registry/compose))))

(deftest independent-registries-compose
  (is (:ok (registry/compose {:review/open review}
                             {:publish/reconcile publish}))))

(deftest duplicate-ids-fail-closed
  (let [result (registry/compose {:review/open review} {:review/open review})]
    (is (= :reaction-id-conflict (:reason result)))
    (is (= [:review/open] (:conflicts result)))))

(deftest registry-key-must-match-reaction-id
  (let [result (registry/compose {:wrong review})]
    (is (= :invalid-registry (:reason result)))
    (is (= :reaction/registry-id-mismatch (-> result :errors first :law/id)))))
