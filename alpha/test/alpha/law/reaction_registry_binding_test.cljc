(ns alpha.law.reaction-registry-binding-test
  #?(:clj (:require [alpha.law.reaction-registry :as registry]
                    [clojure.test :refer [deftest is]])
     :cljs (:require [alpha.law.reaction-registry :as registry]
                     [cljs.test :refer-macros [deftest is]])))

(def event
  {:event/id :event/1
   :event/type :artifact/changed
   :event/subject {:ref/type :artifact :ref/id :doc/1}})

(def subject
  {:artifact/id :doc/1 :artifact/kind :document :artifact/status :review})

(def review
  {:reaction/id :review/open
   :reaction/on {:event/type :artifact/changed :artifact/kind :document}
   :reaction/when {:condition/op :eq
                   :condition/path [:artifact :artifact/status]
                   :condition/value :review}
   :reaction/do {:operation/id :evaluation/open-case}})

(def publish
  {:reaction/id :publish/reconcile
   :reaction/on {:event/type :artifact/changed}
   :reaction/do {:operation/id :publication/reconcile}})

(def orphan
  {:reaction/id :clock/orphan
   :reaction/on {:event/type :clock/tick}
   :reaction/do {:operation/id :operation/missing}})

(deftest any-unresolved-operation-blocks-registry-use
  (let [result (registry/select {:evaluation/open-case {}
                                 :publication/reconcile {}}
                                {:review/open review
                                 :clock/orphan orphan}
                                event subject)]
    (is (= :unbound-reaction-registry (:reason result)))
    (is (= :operation/missing (-> result :errors first :operation/id)))))

(deftest bound-selection-is-deterministically-ordered
  (let [result (registry/select {:evaluation/open-case {}
                                 :publication/reconcile {}}
                                {:review/open review
                                 :publish/reconcile publish}
                                event subject)]
    (is (:ok result))
    (is (= [:publish/reconcile :review/open]
           (mapv :reaction/id (:reactions result))))))
