(ns alpha.law.diagram
  (:require [malli.core :as m]))

(def Direction [:enum :lr :rl :td :tb :bt])
(def Node [:map {:closed true}
           [:node/id :string]
           [:node/label :string]
           [:node/shape [:enum :box :decision :round :implicit]]])
(def Edge [:map {:closed true}
           [:edge/from :string]
           [:edge/to :string]
           [:edge/style [:enum :solid :dotted]]
           [:edge/label {:optional true} :string]])
(def Graph [:map {:closed true}
            [:graph/direction Direction]
            [:graph/nodes [:map-of :string Node]]
            [:graph/edges [:vector Edge]]])

(defn shape-valid? [graph]
  (m/validate Graph graph))

(defn node-id-errors [graph]
  (->> (:graph/nodes graph)
       (keep (fn [[map-id node]]
               (when (not= map-id (:node/id node))
                 {:law/id :diagram/node-map-id-mismatch
                  :map-id map-id
                  :node/id (:node/id node)})))
       vec))

(defn endpoint-errors [graph]
  (let [nodes (:graph/nodes graph)]
    (->> (:graph/edges graph)
         (map-indexed
          (fn [idx edge]
            (cond-> []
              (not (contains? nodes (:edge/from edge)))
              (conj {:law/id :diagram/unknown-edge-source
                     :path [:graph/edges idx :edge/from]
                     :node/id (:edge/from edge)})
              (not (contains? nodes (:edge/to edge)))
              (conj {:law/id :diagram/unknown-edge-target
                     :path [:graph/edges idx :edge/to]
                     :node/id (:edge/to edge)}))))
         (apply concat)
         vec)))

(defn validate [graph]
  (if-not (shape-valid? graph)
    {:ok false :stage :shape}
    (let [errors (vec (concat (node-id-errors graph)
                              (endpoint-errors graph)))]
      {:ok (empty? errors) :graph graph :errors errors})))
