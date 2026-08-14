(ns alpha.law.portable-identity-test
  (:require [alpha.law.artifact :as law]
            [katamorph.schema.condition :as condition]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(deftest alpha-id-keeps-portable-identity-forms
  (doseq [id [:artifact/example
              "artifact/example"
              #uuid "00000000-0000-0000-0000-000000000001"
              42
              condition/max-safe-integer
              (- condition/max-safe-integer)]]
    (is (law/valid-shape? :alpha/id id) (pr-str id))))

(deftest unsafe-and-noninteger-numeric-ids-fail-closed
  (doseq [id [(inc condition/max-safe-integer)
              (dec (- condition/max-safe-integer))
              1.0
              1.5]]
    (is (false? (law/valid-shape? :alpha/id id)) (pr-str id))))

(deftest artifact-event-and-ref-identities-share-the-same-law
  (testing "safe numeric ids remain usable"
    (is (law/artifact? {:artifact/id 42 :artifact/kind :finding}))
    (is (law/event? {:event/id 42 :event/type :artifact/changed}))
    (is (law/valid-shape? :alpha/ref {:ref/type :github/pr :ref/id 243})))
  (testing "unsafe numeric ids cannot enter through another shape"
    (let [unsafe (inc condition/max-safe-integer)]
      (is (false? (law/artifact? {:artifact/id unsafe :artifact/kind :finding})))
      (is (false? (law/event? {:event/id unsafe :event/type :artifact/changed})))
      (is (false? (law/valid-shape? :alpha/ref {:ref/type :external :ref/id unsafe}))))))

(deftest relation-revisions-use-portable-identities-too
  (let [safe {:ref/type :artifact
              :ref/id :doc/id-1
              :ref/revision condition/max-safe-integer}
        unsafe (assoc safe :ref/revision (inc condition/max-safe-integer))]
    (is (law/valid-shape? :alpha/ref safe))
    (is (false? (law/valid-shape? :alpha/ref unsafe)))))
