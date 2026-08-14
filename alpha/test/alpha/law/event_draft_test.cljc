(ns alpha.law.event-draft-test
  (:require [alpha.law.event-draft :as draft]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(deftest event-draft-shape
  (is (draft/valid? {:event/type :artifact/observed}))
  (is (false? (draft/valid? {:event/type :artifact/observed
                             :event/id :already-materialized}))))

(deftest nested-portable-event-data-remains-lawful
  (is (draft/valid?
       {:event/type :artifact/changed
        :event/data {:actor :agent/reviewer
                     :policy {:mode :strict
                              :labels #{:terminology :accuracy}}
                     :history [{:attempt 1 :accepted false}
                               {:attempt 2 :accepted true}]}})))

(deftest runtime-code-cannot-enter-event-draft-data
  (is (false?
       (draft/valid?
        {:event/type :artifact/changed
         :event/data {:callback (fn [] :runtime)}}))))

(deftest host-values-cannot-hide-inside-event-draft-data
  #?(:cljs
     (is (false?
          (draft/valid?
           {:event/type :artifact/changed
            :event/data {:runtime #js {:host true}}})))
     :clj
     (is (false?
          (draft/valid?
           {:event/type :artifact/changed
            :event/data {:runtime (Object.)}})))))
