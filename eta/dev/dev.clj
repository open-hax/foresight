(ns dev
  "Dev REPL helpers."
  (:require [eta.provider :as p]
            [eta.tools    :as t]
            [eta.agent    :as a]))

(defn test-provider
  "Quick smoke test of the provider connection."
  []
  (let [config (p/provider-config)]
    (println "Provider:" (:base-url config))
    (println "Model:" (:model config))
    (println "Models available:" (count (p/list-models config)))))

(defn test-tool [tool-name args]
  "Test a single tool. (test-tool \"bash\" {:command \"echo hi\"})"
  (let [handler (get t/tool-handlers tool-name)]
    (if handler
      (handler args)
      (println "Unknown tool:" tool-name))))

(defn quick-agent
  "Run a single agent turn and return the final state."
  [prompt]
  (a/run (a/make-state) prompt))
