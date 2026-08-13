(ns alpha.law.markdown.facet-test
  (:require [alpha.law.markdown.facet :as facet]
            [alpha.law.markdown.profile-test :as fixtures]
            [alpha.law.markdown.schema :as markdown]
            [katamorph.schema.core :as schema]
            #?(:clj [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is]])))

(def project-schemas
  (:registry
   (schema/compose-registries
    markdown/all-schemas
    {:epiphany/finding
     [:map {:closed false}
      [:kind [:= :finding]]
      [:confidence number?]]

     :rheos/story
     [:map {:closed false}
      [:type [:= :story]]
      [:status keyword?]
      [:points int?]]

     :calliope/review
     [:map {:closed false}
      [:kind [:= :media-review]]
      [:ratings [:map {:closed false}]]]})))

(deftest facets-validate-the-target-the-profile-names
  (doseq [[declared facet-schema document]
          [[(assoc fixtures/epiphany-profile
                   :profile/facets [{:facet/schema :epiphany/finding
                                     :facet/target :frontmatter}])
            :epiphany/finding
            {:document/path "finding.md"
             :document/frontmatter {:uuid "finding/1"
                                    :kind :finding
                                    :status :provisional
                                    :confidence 0.84}
             :document/body "# Finding"}]

           [(assoc fixtures/rheos-profile
                   :profile/facets [{:facet/schema :rheos/story
                                     :facet/target :frontmatter}])
            :rheos/story
            {:document/path "story.md"
             :document/frontmatter {:uuid "story-1"
                                    :type :story
                                    :status :ready
                                    :points 5}
             :document/body "# Story"}]

           [(assoc fixtures/calliope-profile
                   :profile/facets [{:facet/schema :calliope/review
                                     :facet/target :frontmatter}])
            :calliope/review
            {:document/path "review.md"
             :document/frontmatter {:subject {:id "render-17"}
                                    :kind :media-review
                                    :decision {:status :accepted}
                                    :ratings {:enjoyment 4}}
             :document/body "# Review"}]]]
    (let [result (facet/validate-profiled-document project-schemas declared document)]
      (is (:ok result))
      (is (= facet-schema
             (-> result :facets :results first :facet/schema))))))

(deftest unknown-facet-schema-is-not-treated-as-success
  (let [declared (assoc fixtures/epiphany-profile
                        :profile/facets [{:facet/schema :missing/facet
                                          :facet/target :frontmatter}])
        result (facet/validate-profiled-document
                project-schemas
                declared
                {:document/path "finding.md"
                 :document/frontmatter {:uuid "finding/2"
                                        :kind :finding
                                        :status :provisional}
                 :document/body "# Finding"})]
    (is (false? (:ok result)))
    (is (= "unknown schema kind"
           (-> result :facets :results first :errors first :message)))))
