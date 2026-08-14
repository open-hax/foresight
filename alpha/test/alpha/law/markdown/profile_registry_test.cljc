(ns alpha.law.markdown.profile-registry-test
  (:require [alpha.law.markdown.profile-registry :as registry]
            #?(:clj [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is]])))

(defn profile [id field value]
  {:profile/id id
   :profile/when {:condition/op :eq
                  :condition/path [:frontmatter field]
                  :condition/value value}
   :profile/id-path [:uuid]
   :profile/kind-path [:kind]})

(def epiphany
  (profile :epiphany :kind "finding"))

(def rheos
  (profile :rheos :type "story"))

(deftest empty-composition-has-an-empty-registry-identity
  (is (= {:ok true :registry {} :conflicts []}
         (registry/compose))))

(deftest independent-registries-compose
  (let [result (registry/compose {:epiphany epiphany}
                                 {:rheos rheos})]
    (is (:ok result))
    (is (= #{:epiphany :rheos}
           (set (keys (:registry result)))))))

(deftest duplicate-profile-ids-fail-closed
  (let [result (registry/compose {:epiphany epiphany}
                                 {:epiphany epiphany})]
    (is (= :profile-id-conflict (:reason result)))
    (is (= [:epiphany] (:conflicts result)))))

(deftest registry-key-must-match-profile-id
  (let [result (registry/compose {:wrong epiphany})]
    (is (= :invalid-registry (:reason result)))
    (is (= :profile/registry-id-mismatch
           (-> result :errors first :law/id)))))

(deftest composed-registry-can-select-a-profile
  (let [composed (:registry (registry/compose {:epiphany epiphany}
                                               {:rheos rheos}))
        document {:document/format :markdown
                  :document/frontmatter-present? true
                  :document/frontmatter/raw "uuid: f/1\nkind: finding"
                  :document/frontmatter/data {:uuid "f/1" :kind "finding"}
                  :document/body "# Finding"}
        result (registry/select composed document)]
    (is (:ok result))
    (is (= :epiphany (get-in result [:profile :profile/id])))))
