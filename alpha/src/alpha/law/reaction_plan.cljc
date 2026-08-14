(ns alpha.law.reaction-plan
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.reaction-registry :as reactions]
            [malli.core :as m]))

(def InvocationPlanEntry
  [:map {:closed true}
   [:plan/reaction-id artifact/Id]
   [:plan/invocation artifact/OperationRef]])

(defn entry? [value]
  (m/validate InvocationPlanEntry value))

(defn plan
  "Select lawful reactions and describe the invocation requests they imply.

   This function does not choose providers, call handlers, assign runtime ids,
   stamp time, or perform effects. An empty plan is successful no-work."
  [action-registry reaction-registry event subject-artifact]
  (let [selected (reactions/select action-registry
                                   reaction-registry
                                   event
                                   subject-artifact)]
    (if-not (:ok selected)
      selected
      {:ok true
       :plans (mapv (fn [reaction]
                      {:plan/reaction-id (:reaction/id reaction)
                       :plan/invocation (:reaction/do reaction)})
                    (:reactions selected))})))
