(ns alpha.test-runner
  (:require [alpha.law.artifact-test]
            [alpha.law.diagram-artifact-test]
            [alpha.law.markdown.facet-test]
            [alpha.law.markdown.profile-test]
            [alpha.shape.mermaid-test]
            [clojure.test :as test]))

(defn -main [& _]
  (let [result (test/run-tests 'alpha.law.artifact-test
                               'alpha.law.diagram-artifact-test
                               'alpha.law.markdown.profile-test
                               'alpha.law.markdown.facet-test
                               'alpha.shape.mermaid-test)]
    (when (pos? (+ (:fail result) (:error result)))
      (throw (ex-info "Alpha tests failed" result)))))
