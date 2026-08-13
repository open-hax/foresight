(ns alpha.shape.mermaid-declaration
  (:require [clojure.string :as str]))

(defn header [line]
  (when-let [[_ direction]
             (re-matches #"^(?:flowchart|graph)\s+(LR|RL|TD|TB|BT)\s*$"
                         (str/trim line))]
    (keyword (str/lower-case direction))))

(defn- unquote-label [label]
  (let [value (str/trim label)]
    (if (and (>= (count value) 2)
             (= \" (first value))
             (= \" (last value)))
      (subs value 1 (dec (count value)))
      value)))

(defn node [line]
  (when-let [[_ id wrapper label]
             (re-matches #"^([A-Za-z][A-Za-z0-9_.:-]*)(\[|\{|\()(.*)(?:\]|\}|\))$"
                         (str/trim line))]
    {:node/id id
     :node/label (unquote-label label)
     :node/shape (case wrapper "{" :decision "(" :round :box)}))
