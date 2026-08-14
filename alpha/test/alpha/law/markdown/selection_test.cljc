(ns alpha.law.markdown.selection-test
  (:require [alpha.law.markdown.selection :as selection]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(defn profile [id predicate]
  (cond-> {:profile/id id
           :profile/id-path [:uuid]
           :profile/kind-path [:kind]}
    predicate (assoc :profile/when predicate)))

(def profiles
  [(profile :epiphany
            {:condition/op :in
             :condition/path [:frontmatter :kind]
             :condition/values ["finding" "design" "decision"]})
   (profile :rheos
            {:condition/op :in
             :condition/path [:frontmatter :type]
             :condition/values ["story" "epic"]})
   (profile :calliope-review
            {:condition/op :eq
             :condition/path [:frontmatter :kind]
             :condition/value "media-review"})])

(def finding
  {:document/format :markdown
   :document/frontmatter-present? true
   :document/frontmatter/raw "uuid: finding/1\nkind: finding"
   :document/frontmatter/data {:uuid "finding/1" :kind "finding"}
   :document/body "# Finding"})

(deftest selects-one-profile-by-declarative-condition
  (let [result (selection/select profiles finding)]
    (is (:ok result))
    (is (= :epiphany (get-in result [:profile :profile/id])))))

(deftest zero-matches-fail-closed
  (is (= :no-profile-match
         (:reason (selection/select profiles
                                    (assoc-in finding
                                              [:document/frontmatter/data :kind]
                                              "unknown"))))))

(deftest ambiguity-fails-closed-instead-of-using-order
  (let [result (selection/select [(profile :zeta nil)
                                  (profile :alpha nil)]
                                 finding)]
    (is (= :ambiguous-profile (:reason result)))
    (is (= [:alpha :zeta] (:profile/ids result)))))

(deftest malformed-selectors-fail-before-matching
  (let [invalid (assoc (profile :invalid nil)
                       :profile/when {:condition/op :eq
                                      :condition/value "finding"})
        result (selection/select [invalid] finding)]
    (is (= :invalid-profile (:reason result)))
    (is (= [:invalid] (:profile/ids result)))))

(deftest caller-context-can-participate-without-hardcoded-project-logic
  (let [scoped (profile :curation
                        {:condition/op :eq
                         :condition/path [:context :mode]
                         :condition/value :curation})]
    (is (:ok (selection/select [scoped] finding {:mode :curation})))
    (is (= :no-profile-match
           (:reason (selection/select [scoped] finding {:mode :research}))))))
