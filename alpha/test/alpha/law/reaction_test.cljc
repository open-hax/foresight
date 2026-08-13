(ns alpha.law.reaction-test
  (:require [alpha.law.reaction :as reaction]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(def operations
  {:evaluation/open-case {:operation/category :evaluation}
   :publication/reconcile {:operation/category :orchestration}})

(def event
  {:event/id :event/review-requested
   :event/type :artifact/changed
   :event/subject {:ref/type :artifact :ref/id :translation/42}
   :event/data {:actor :agent/reviewer}})

(def subject
  {:artifact/id :translation/42
   :artifact/kind :translation
   :artifact/status :review})

(def matching-reaction
  {:reaction/id :translation/open-review
   :reaction/on {:event/type :artifact/changed
                 :artifact/kind :translation
                 :subject/type :artifact}
   :reaction/when {:condition/op :eq
                   :condition/path [:artifact :artifact/status]
                   :condition/value :review}
   :reaction/do {:operation/id :evaluation/open-case}})

(deftest selects-valid-matching-reactions
  (is (= [matching-reaction]
         (reaction/select operations [matching-reaction] event subject))))

(deftest trigger-and-condition-are-both-required
  (testing "wrong artifact kind"
    (is (empty?
         (reaction/select operations
                          [(assoc-in matching-reaction
                                     [:reaction/on :artifact/kind]
                                     :audio-render)]
                          event
                          subject))))
  (testing "condition does not hold"
    (is (empty?
         (reaction/select operations
                          [matching-reaction]
                          event
                          (assoc subject :artifact/status :accepted))))))

(deftest artifact-context-must-be-lawful-and-bound-to-event
  (testing "an unrelated valid artifact cannot influence an event"
    (is (empty?
         (reaction/select operations
                          [matching-reaction]
                          event
                          (assoc subject :artifact/id :translation/other)))))
  (testing "a malformed artifact cannot provide trigger or condition context"
    (is (empty?
         (reaction/select operations
                          [matching-reaction]
                          event
                          {:artifact/id :translation/42
                           :artifact/status :review})))))

(deftest disabled-and-unregistered-reactions-fail-closed
  (is (empty?
       (reaction/select operations
                        [(assoc matching-reaction :reaction/enabled? false)]
                        event
                        subject)))
  (is (empty?
       (reaction/select operations
                        [(assoc-in matching-reaction
                                   [:reaction/do :operation/id]
                                   :operation/not-registered)]
                        event
                        subject))))

(deftest external-events-do-not-require-an-artifact
  (let [external-event {:event/id :clock/morning
                        :event/type :clock/tick
                        :event/subject {:ref/type :clock :ref/id :local}}
        external-reaction {:reaction/id :daily/reconcile
                           :reaction/on {:event/type :clock/tick
                                         :subject/type :clock}
                           :reaction/do {:operation/id :publication/reconcile}}]
    (is (= [external-reaction]
           (reaction/select operations
                            [external-reaction]
                            external-event
                            nil)))))
