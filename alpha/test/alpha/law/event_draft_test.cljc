(ns alpha.law.event-draft-test
  (:require [alpha.law.event-draft :as draft]
            [clojure.test :refer [deftest is]]))

(deftest event-draft-shape
  (is (draft/valid? {:event/type :artifact/observed}))
  (is (false? (draft/valid? {:event/type :artifact/observed
                             :event/id :already-materialized}))))
