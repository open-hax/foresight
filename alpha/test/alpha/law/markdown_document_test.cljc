(ns alpha.law.markdown-document-test
  (:require [alpha.law.artifact :as law]
            [alpha.law.markdown.profile :as profile]
            #?(:clj [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is]])))

(def lossless-document
  {:document/format :markdown
   :document/source-path "docs/finding.md"
   :document/frontmatter-present? true
   :document/frontmatter-raw "uuid: finding/lossless\nkind: finding\nstatus: provisional"
   :document/frontmatter-data {:uuid "finding/lossless"
                               :kind "finding"
                               :status "provisional"}
   :document/body "# Finding"})

(deftest lossless-markdown-documents-are-lawful
  (is (law/valid-shape? :alpha/markdown-document lossless-document)))

(deftest lossless-documents-project-through-profiles
  (let [declared {:profile/id :finding
                  :profile/id-path [:uuid]
                  :profile/kind-path [:kind]
                  :profile/status-path [:status]}
        result (profile/project-artifact declared lossless-document)]
    (is (:ok result))
    (is (= "finding/lossless" (get-in result [:artifact :artifact/id])))
    (is (= :finding (get-in result [:artifact :artifact/kind])))
    (is (= :provisional (get-in result [:artifact :artifact/status])))
    (is (= (:document/frontmatter-data lossless-document)
           (get-in result [:artifact :artifact/data])))))

(deftest legacy-markdown-shape-remains-compatible
  (is (law/valid-shape?
       :alpha/markdown-document
       {:document/path "docs/legacy.md"
        :document/frontmatter {:kind :finding}
        :document/body "# Legacy"})))
