(ns alpha.law.reaction-plan-test
  (:require [alpha.law.reaction-plan :as plan]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(def actions
  {:evaluation/open-case {:contract/kind :action
                          :contract/id :evaluation/open-case
                          :action/category :evaluation}
   :publication/reconcile {:contract/kind :action
                           :contract/id :publication/reconcile
                           :action/category :orchestration}})

(def event
  {:event/id :event/1
   :event/type :artifact/changed
   :event/subject {:ref/type :artifact :ref/id :doc/1}})

(def subject
  {:artifact/id :doc/1
   :artifact/kind :document
   :artifact/status :review})

(def open-review
  {:reaction/id :review/open
   :reaction/on {:event/type :artifact/changed :artifact/kind :document}
   :reaction/when {:condition/op :eq
                   :condition/path [:artifact :artifact/status]
                   :condition/value :review}
   :reaction/do {:operation/id :evaluation/open-case
                 :operation/in {:subject [:event :subject]}}})

(def reconcile
  {:reaction/id :publication/reconcile
   :reaction/on {:event/type :artifact/changed :artifact/kind :document}
   :reaction/do {:operation/id :publication/reconcile}})

(deftest selected-reactions-become-deterministic-invocation-plans
  (let [result (plan/plan actions
                          {:review/open open-review
                           :publication/reconcile reconcile}
                          event subject)]
    (is (:ok result))
    (is (= [:publication/reconcile :review/open]
           (mapv :plan/reaction-id (:plans result))))
    (is (every? plan/entry? (:plans result)))
    (is (= :evaluation/open-case
           (get-in result [:plans 1 :plan/invocation :operation/id])))))

(deftest no-matching-reaction-is-successful-no-work
  (let [result (plan/plan actions
                          {:review/open open-review}
                          event
                          (assoc subject :artifact/status :accepted))]
    (is (:ok result))
    (is (= [] (:plans result)))))

(deftest registry-failures-propagate-before-planning
  (testing "invalid action registry"
    (let [result (plan/plan {:evaluation/open-case {}}
                            {:review/open open-review}
                            event subject)]
      (is (false? (:ok result)))
      (is (= :invalid-action-registry (:reason result)))))
  (testing "unbound reaction registry"
    (let [orphan (assoc-in open-review
                           [:reaction/do :operation/id]
                           :operation/missing)
          result (plan/plan actions
                            {:review/open orphan}
                            event subject)]
      (is (false? (:ok result)))
      (is (= :unbound-reaction-registry (:reason result))))))
