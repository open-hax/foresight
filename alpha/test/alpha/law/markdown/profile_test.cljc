(ns alpha.law.markdown.profile-test
  (:require [alpha.law.markdown.profile :as profile]
            [alpha.law.markdown.schema :as markdown]
            #?(:clj [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is]])))

(def epiphany-profile
  {:profile/id :epiphany/governed-document
   :profile/id-path [:uuid]
   :profile/kind-path [:kind]
   :profile/status-path [:status]})

(def rheos-profile
  {:profile/id :rheos/card
   :profile/id-path [:uuid]
   :profile/kind-path [:type]
   :profile/status-path [:status]})

(def calliope-profile
  {:profile/id :calliope/review
   :profile/id-path [:subject :id]
   :profile/kind-path [:kind]
   :profile/status-path [:decision :status]})

(deftest alpha-and-markdown-registries-compose
  (is (:ok markdown/schema-composition))
  (is (contains? markdown/all-schemas :alpha/artifact))
  (is (contains? markdown/all-schemas :alpha/markdown-profile)))

(deftest unrelated-frontmatter-projects-through-declared-paths
  (doseq [[declared document expected-id expected-kind]
          [[epiphany-profile
            {:document/path "docs/findings/reactive.md"
             :document/frontmatter {:uuid "finding/reactive-docs"
                                    :kind :finding
                                    :status :provisional
                                    :confidence 0.84
                                    :project/custom {:kept true}}
             :document/body "# Finding"}
            "finding/reactive-docs" :finding]

           [rheos-profile
            {:document/path "docs/agile/kanban/story.md"
             :document/frontmatter {:uuid "story-generalize-rheos"
                                    :type :story
                                    :status :ready
                                    :points 5}
             :document/body "# Story"}
            "story-generalize-rheos" :story]

           [calliope-profile
            {:document/path "reviews/render-17.md"
             :document/frontmatter {:subject {:id "render-17"}
                                    :kind :media-review
                                    :decision {:status :accepted}
                                    :ratings {:enjoyment 4 :weirdness 5}}
             :document/body "# Review"}
            "render-17" :media-review]]]
    (let [result (profile/project-artifact declared document)]
      (is (:ok result))
      (is (= expected-id (get-in result [:artifact :artifact/id])))
      (is (= expected-kind (get-in result [:artifact :artifact/kind])))
      (is (= (:document/frontmatter document)
             (get-in result [:artifact :artifact/data]))))))

(deftest required-projection-paths-fail-closed
  (let [result (profile/project-artifact
                epiphany-profile
                {:document/path "missing.md"
                 :document/frontmatter {:kind :finding}
                 :document/body "# Missing identity"})]
    (is (false? (:ok result)))
    (is (= :projection (:stage result)))
    (is (= [:artifact/id]
           (-> result :errors first :missing)))))
