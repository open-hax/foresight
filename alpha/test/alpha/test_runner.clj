(ns alpha.test-runner
  (:require [alpha.law.artifact-test]
            [alpha.law.markdown-document-test]
            [alpha.law.markdown.facet-test]
            [alpha.law.markdown.profile-test]
            [alpha.law.portable-extension-test]
            [alpha.law.portable-identity-test]
            [alpha.law.portable-payload-test]
            [alpha.law.reaction-test]
            [clojure.test :as test]))

(defn -main [& _]
  (let [result (test/run-tests 'alpha.law.artifact-test
                               'alpha.law.markdown-document-test
                               'alpha.law.markdown.profile-test
                               'alpha.law.markdown.facet-test
                               'alpha.law.portable-payload-test
                               'alpha.law.portable-identity-test
                               'alpha.law.portable-extension-test
                               'alpha.law.reaction-test)]
    (when (pos? (+ (:fail result) (:error result)))
      (throw (ex-info "Alpha tests failed" result)))))
