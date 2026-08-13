(ns alpha.law.markdown-document-test
  (:require [alpha.law.artifact :as law]
            #?(:clj [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is]])))

(deftest lossless-markdown-documents-are-lawful
  (is (law/valid-shape?
       :alpha/markdown-document
       {:document/format :markdown
        :document/source-path "docs/finding.md"
        :document/frontmatter-present? true
        :document/frontmatter/raw (str "kind: finding\n"
                                       "nested:\n"
                                       "  arbitrary: true")
        :document/frontmatter/data {:kind "finding"
                                    :nested ""}
        :document/body "# Finding"})))

(deftest legacy-markdown-shape-remains-compatible
  (is (law/valid-shape?
       :alpha/markdown-document
       {:document/path "docs/legacy.md"
        :document/frontmatter {:kind :finding}
        :document/body "# Legacy"})))
