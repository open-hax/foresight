(ns alpha.shape.mermaid
  (:require [alpha.law.diagram :as diagram]
            [alpha.shape.mermaid-declaration :as declaration]
            [alpha.shape.mermaid-statement :as statement]
            [clojure.string :as str]))

(defn- lines [source]
  (->> (str/split-lines source)
       (map-indexed (fn [idx value] {:line (inc idx) :source value}))
       (remove #(or (str/blank? (:source %))
                    (str/starts-with? (str/trim (:source %)) "%%")))
       vec))

(defn parse [diagram-id source]
  (let [source-lines (lines source)
        first-line (first source-lines)
        direction (some-> first-line :source declaration/header)]
    (if-not direction
      {:ok false :stage :header}
      (let [state (reduce (fn [acc {:keys [line source]}]
                            (statement/consume acc line source))
                          statement/empty-state
                          (rest source-lines))
            graph {:graph/direction direction
                   :graph/nodes (:nodes state)
                   :graph/edges (:edges state)}]
        (cond
          (seq (:errors state))
          {:ok false :stage :syntax :errors (:errors state)}

          (not (:ok (diagram/validate graph)))
          {:ok false :stage :law :errors (:errors (diagram/validate graph))}

          :else
          {:ok true
           :diagram {:diagram/id diagram-id
                     :diagram/language :mermaid
                     :diagram/source source
                     :diagram/graph graph}
           :graph graph})))))
