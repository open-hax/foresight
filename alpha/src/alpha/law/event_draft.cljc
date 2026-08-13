(ns alpha.law.event-draft
  (:require [alpha.law.artifact :as artifact]
            [malli.core :as m]))

(def EventDraft
  [:map {:closed false}
   [:event/type :keyword]
   [:event/source {:optional true} artifact/Ref]
   [:event/subject {:optional true} artifact/Ref]
   [:event/data {:optional true} :map]
   [:event/causes {:optional true} [:vector artifact/Ref]]])

(defn valid? [value]
  (m/validate EventDraft value))
