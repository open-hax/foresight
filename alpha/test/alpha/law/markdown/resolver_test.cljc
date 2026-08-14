(ns alpha.law.markdown.resolver-test
  (:require [alpha.law.markdown.resolver :as resolver]
            [alpha.law.markdown.schema :as markdown]
            [katamorph.schema.core :as schema]
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])))

(def rheos-profile
  {:profile/id :rheos/card
   :profile/id-path [:uuid]
   :profile/kind-path [:type]
   :profile/status-path [:status]
   :profile/when {:condition/op :exists
                  :condition/path [:frontmatter :type]}
   :profile/facets [{:facet/schema :rheos/story
                     :facet/target :frontmatter}]})

(def epiphany-profile
  {:profile/id :epiphany/finding
   :profile/id-path [:uuid]
   :profile/kind-path [:kind]
   :profile/status-path [:status]
   :profile/when {:condition/op :exists
                  :condition/path [:frontmatter :kind]}})

(def profile-registry
  {:rheos/card rheos-profile
   :epiphany/finding epiphany-profile})

(def project-schemas
  (:registry
   (schema/compose-registries
    markdown/all-schemas
    {:rheos/story
     [:map {:closed false}
      [:uuid :string]
      [:type [:= :story]]
      [:status :keyword]
      [:points :int]]})))

(defn lossless-story [points]
  {:document/format :markdown
   :document/source-path "kanban/story.md"
   :document/frontmatter-present? true
   :document/frontmatter/raw "uuid: story-1\ntype: story\nstatus: ready\npoints: 5"
   :document/frontmatter/data {:uuid "story-1"
                               :type :story
                               :status :ready
                               :points points}
   :document/body "# Story"})

(deftest lossless-document-resolves-through-one-declared-profile
  (let [result (resolver/resolve-artifact project-schemas
                                          profile-registry
                                          (lossless-story 5))]
    (is (:ok result))
    (is (= :rheos/card (get-in result [:profile :profile/id])))
    (is (= "story-1" (get-in result [:artifact :artifact/id])))
    (is (= :story (get-in result [:artifact :artifact/kind])))
    (is (= :ready (get-in result [:artifact :artifact/status])))
    (is (= true (get-in result [:facets :ok])))))

(deftest facet-failure-remains-distinct-from-profile-selection
  (let [result (resolver/resolve-artifact project-schemas
                                          profile-registry
                                          (lossless-story "five"))]
    (is (false? (:ok result)))
    (is (= :facet-validation (:stage result)))
    (is (= :rheos/card (get-in result [:profile :profile/id])))
    (is (= "story-1" (get-in result [:artifact :artifact/id])))))

(deftest zero-and-multiple-profile-matches-fail-closed
  (testing "no match"
    (let [document (assoc-in (lossless-story 5)
                             [:document/frontmatter/data]
                             {:uuid "plain"})
          result (resolver/resolve-artifact project-schemas
                                            profile-registry
                                            document)]
      (is (false? (:ok result)))
      (is (= :profile-selection (:stage result)))
      (is (= :no-profile-match (:reason result)))))
  (testing "ambiguous match"
    (let [also-rheos (assoc epiphany-profile
                            :profile/id :another/card
                            :profile/kind-path [:type]
                            :profile/when (:profile/when rheos-profile))
          result (resolver/resolve-artifact
                  project-schemas
                  {:rheos/card rheos-profile
                   :another/card also-rheos}
                  (lossless-story 5))]
      (is (false? (:ok result)))
      (is (= :profile-selection (:stage result)))
      (is (= :ambiguous-profile (:reason result))))))
