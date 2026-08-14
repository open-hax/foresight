(ns alpha.law.markdown.frontmatter-decode-evidence-test
  (:require [alpha.law.markdown.resolver :as resolver]
            [alpha.law.markdown.resolver-test :as fixtures]
            #?(:clj [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is]])))

(def partial
  {:decoder/id :rheos/flat-frontmatter-v1
   :decode/status :partial
   :decode/capabilities #{:top-level-string-scalars}})

(def failed
  {:decoder/id :yaml/full-v1
   :decode/status :failed
   :decode/capabilities #{:yaml}})

(deftest partial-decoding-remains-usable-and-visible
  (let [document (assoc (fixtures/lossless-story 5)
                        :document/frontmatter/decoding partial)
        result (resolver/resolve-artifact fixtures/project-schemas
                                          fixtures/profile-registry
                                          document)]
    (is (:ok result))
    (is (= partial (:document/frontmatter/decoding result)))))

(deftest partial-decoding-evidence-survives-a-no-match-finding
  (let [document (-> (fixtures/lossless-story 5)
                     (assoc :document/frontmatter/decoding partial)
                     (assoc :document/frontmatter/data {:uuid "plain"}))
        result (resolver/resolve-artifact fixtures/project-schemas
                                          fixtures/profile-registry
                                          document)]
    (is (false? (:ok result)))
    (is (= :profile-selection (:stage result)))
    (is (= :no-profile-match (:reason result)))
    (is (= partial (:document/frontmatter/decoding result)))))

(deftest failed-decoder-data-is-never-used-for-selection
  (let [document (assoc (fixtures/lossless-story 5)
                        :document/frontmatter/decoding failed)
        result (resolver/resolve-artifact fixtures/project-schemas
                                          fixtures/profile-registry
                                          document)]
    (is (false? (:ok result)))
    (is (= :frontmatter-decoding (:stage result)))
    (is (= :frontmatter-decode-failed (:reason result)))
    (is (= failed (:document/frontmatter/decoding result)))))

(deftest malformed-decode-evidence-is-an-invalid-document
  (let [document (assoc (fixtures/lossless-story 5)
                        :document/frontmatter/decoding
                        {:decoder/id :broken
                         :decode/status :unknown
                         :decode/capabilities #{}})
        result (resolver/resolve-artifact fixtures/project-schemas
                                          fixtures/profile-registry
                                          document)]
    (is (false? (:ok result)))
    (is (= :document-validation (:stage result)))
    (is (= :invalid-document (:reason result)))))
