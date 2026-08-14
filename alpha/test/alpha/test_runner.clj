(ns alpha.test-runner
  (:require [alpha.law.artifact-test]
            [alpha.law.markdown-document-test]
            [alpha.law.markdown.facet-test]
            [alpha.law.markdown.frontmatter-decode-evidence-test]
            [alpha.law.markdown.profile-registry-test]
            [alpha.law.markdown.profile-test]
            [alpha.law.markdown.resolver-test]
            [alpha.law.markdown.selection-test]
            [alpha.law.reaction-test]
            [clojure.test :as test]))

(defn -main [& _]
  (let [result (test/run-tests 'alpha.law.artifact-test
                               'alpha.law.markdown-document-test
                               'alpha.law.markdown.profile-test
                               'alpha.law.markdown.facet-test
                               'alpha.law.markdown.selection-test
                               'alpha.law.markdown.profile-registry-test
                               'alpha.law.markdown.resolver-test
                               'alpha.law.markdown.frontmatter-decode-evidence-test
                               'alpha.law.reaction-test)]
    (when (pos? (+ (:fail result) (:error result)))
      (throw (ex-info "Alpha tests failed" result)))))
