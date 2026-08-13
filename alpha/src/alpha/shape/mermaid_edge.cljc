(ns alpha.shape.mermaid-edge
  (:require [alpha.shape.mermaid-declaration :as declaration]
            [clojure.string :as str]))

(def id-re #"^[A-Za-z][A-Za-z0-9_.:-]*$")

(defn- endpoint [token]
  (let [value (str/trim token)]
    (if-let [node (declaration/node value)]
      {:id (:node/id node) :node node}
      (when (re-matches id-re value)
        {:id value}))))

(defn- target [segment]
  (let [value (str/trim segment)]
    (if (str/starts-with? value "|")
      (when-let [close (str/index-of value "|" 1)]
        (when-let [parsed (endpoint (subs value (inc close)))]
          (assoc parsed :label (subs value 1 close))))
      (endpoint value))))

(defn solid [line]
  (when (str/includes? line "-->")
    (let [segments (str/split (str/trim line) #"\s*-->\s*")]
      (when-let [{from :id source-node :node} (endpoint (first segments))]
        (when (> (count segments) 1)
          (loop [from from
                 remaining (rest segments)
                 nodes (cond-> [] source-node (conj source-node))
                 edges []]
            (if-let [segment (first remaining)]
              (when-let [{to :id target-node :node label :label} (target segment)]
                (recur to
                       (rest remaining)
                       (cond-> nodes target-node (conj target-node))
                       (conj edges
                             (cond-> {:edge/from from :edge/to to :edge/style :solid}
                               (seq label) (assoc :edge/label label)))))
              {:nodes nodes :edges edges})))))))

(defn dotted [line]
  (when-let [[_ from label to]
             (re-matches #"^([A-Za-z][A-Za-z0-9_.:-]*)\s+-\.\s*(.*?)\s*\.->\s*([A-Za-z][A-Za-z0-9_.:-]*)$"
                         (str/trim line))]
    (cond-> {:edge/from from :edge/to to :edge/style :dotted}
      (seq (str/trim label)) (assoc :edge/label (str/trim label)))))
