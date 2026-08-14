(ns alpha.test-runner
  (:require [alpha.law.artifact-test]
            [alpha.law.markdown-document-test]
            [alpha.law.markdown.facet-test]
            [alpha.law.markdown.profile-test]
            [alpha.law.reaction-plan-test]
            [alpha.law.reaction-registry-binding-test]
            [alpha.law.reaction-registry-composition-test]
            [alpha.law.reaction-test]
            [clojure.test :as test]))

(defn -main [& _]
  (let [result (test/run-tests 'alpha.law.artifact-test
                               'alpha.law.markdown-document-test
                               'alpha.law.markdown.profile-test
                               'alpha.law.markdown.facet-test
                               'alpha.law.reaction-test
                               'alpha.law.reaction-registry-composition-test
                               'alpha.law.reaction-registry-binding-test
                               'alpha.law.reaction-plan-test)]
    (when (pos? (+ (:fail result) (:error result)))
      (throw (ex-info "Alpha tests failed" result)))))
