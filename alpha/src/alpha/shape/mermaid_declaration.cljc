(ns alpha.shape.mermaid-declaration
  (:require [clojure.string :as str]))

(defn header [line]
  (when-let [[_ direction]
             (re-matches #"^(?:flowchart|graph)\s+(LR|RL|TD|TB|BT)\s*$"
                         (str/trim line))]
    (keyword (str/lower-case direction))))

(defn- clean-label [label]
  (let [value (str/trim label)]
    (if (and (>= (count value) 2)
             (= \" (first value))
             (= \" (last value)))
      (subs value 1 (dec (count value)))
      value)))

(defn- parse-node [line pattern shape]
  (when-let [[_ id label] (re-matches pattern (str/trim line))]
    {:node/id id :node/label (clean-label label) :node/shape shape}))

(defn node [line]
  (or (parse-node line #"^([A-Za-z][A-Za-z0-9_.:-]*)\[(.*)\]$" :box)
      (parse-node line #"^([A-Za-z][A-Za-z0-9_.:-]*)\{(.*)\}$" :decision)
      (parse-node line #"^([A-Za-z][A-Za-z0-9_.:-]*)\((.*)\)$" :round)))
