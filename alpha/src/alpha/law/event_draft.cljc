(ns alpha.law.event-draft
  (:require [alpha.law.artifact :as artifact]
            [malli.core :as m]))

(def EventData
  [:map-of [:or keyword? string?] artifact/PortableData])

(def EventDraft
  [:map {:closed true}
   [:event/type :keyword]
   [:event/source {:optional true} artifact/Ref]
   [:event/subject {:optional true} artifact/Ref]
   [:event/data {:optional true} EventData]
   [:event/causes {:optional true} [:vector artifact/Ref]]])

(defn valid? [value]
  (m/validate EventDraft value))
