(ns alpha.shape.mermaid-statement
  (:require [alpha.shape.mermaid-declaration :as declaration]
            [alpha.shape.mermaid-edge :as edge]))

(def empty-state {:nodes {} :edges [] :errors []})

(defn- add-node [state line-number node]
  (let [id (:node/id node)
        current (get-in state [:nodes id])]
    (cond
      (nil? current) (assoc-in state [:nodes id] node)
      (= current node) state
      :else (update state :errors conj
                    {:line line-number :reason :conflicting-node :node/id id}))))

(defn- add-nodes [state line-number nodes]
  (reduce #(add-node %1 line-number %2) state nodes))

(defn consume [state line-number source]
  (cond
    (declaration/node source)
    (add-node state line-number (declaration/node source))

    (edge/dotted source)
    (update state :edges conj (edge/dotted source))

    (edge/solid source)
    (let [{:keys [nodes edges]} (edge/solid source)]
      (-> state
          (add-nodes line-number nodes)
          (update :edges into edges)))

    :else
    (update state :errors conj
            {:line line-number :reason :unsupported-statement :source source})))
