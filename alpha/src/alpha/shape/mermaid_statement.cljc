(ns alpha.shape.mermaid-statement
  (:require [alpha.shape.mermaid-declaration :as declaration]
            [alpha.shape.mermaid-edge :as edge]))

(def empty-state {:nodes {} :edges [] :errors []})

(defn- add-node [state line-number node]
  (if (contains? (:nodes state) (:node/id node))
    (update state :errors conj
            {:line line-number :reason :duplicate-node :node/id (:node/id node)})
    (assoc-in state [:nodes (:node/id node)] node)))

(defn consume [state line-number source]
  (cond
    (declaration/node source)
    (add-node state line-number (declaration/node source))

    (edge/dotted source)
    (update state :edges conj (edge/dotted source))

    (edge/solid source)
    (update state :edges into (edge/solid source))

    :else
    (update state :errors conj
            {:line line-number :reason :unsupported-statement :source source})))
