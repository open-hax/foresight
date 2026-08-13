(ns eta.core
  "Main entry point for η TUI agent harness."
  (:require [eta.agent :as agent]
            [eta.tui   :as tui]
            [eta.provider :as provider]
            [clojure.string :as str])
  (:gen-class))

(defn -main
  "Bootstrap and run the TUI agent."
  [& args]
  (let [config (provider/provider-config)
        model  (:model config)
        state  (agent/make-state {:config config
                                  :model  model})]
    (tui/repl-loop state agent/run)))
