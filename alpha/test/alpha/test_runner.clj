(ns alpha.test-runner
  (:require [alpha.law.artifact-event-test]
            [alpha.law.artifact-test]
            [alpha.law.event-draft-test]
            [alpha.law.event-materialization-test]
            [alpha.law.markdown.facet-test]
            [alpha.law.markdown.profile-test]
            [clojure.test :as t]))

(def suites
  ['alpha.law.artifact-test
   'alpha.law.artifact-event-test
   'alpha.law.event-draft-test
   'alpha.law.event-materialization-test
   'alpha.law.markdown.profile-test
   'alpha.law.markdown.facet-test])

(defn -main [& _]
  (let [r (apply t/run-tests suites)]
    (when (pos? (+ (:fail r) (:error r)))
      (throw (ex-info "test failure" r)))))
