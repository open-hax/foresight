(ns alpha.law.artifact
  "Portable laws for things that can participate in Foresight workflows."
  (:require [malli.core :as m]
            [malli.error :as me]))

(def Id [:or :uuid :keyword :string :int])
(def PathSegment [:or :keyword :string :int])
(def EpistemicTier [:enum :observed :derived :provisional :accepted])

(def Ref
  [:map {:closed false}
   [:ref/type :keyword]
   [:ref/id Id]
   [:ref/revision {:optional true} Id]])

(def ArtifactRef
  [:map {:closed false}
   [:ref/type [:= :artifact]]
   [:ref/id Id]
   [:artifact/kind {:optional true} :keyword]
   [:ref/revision {:optional true} Id]])

(def Relation
  [:map {:closed false}
   [:relation/type :keyword]
   [:relation/source Ref]
   [:relation/target Ref]
   [:relation/epistemic-tier {:optional true} EpistemicTier]
   [:relation/basis {:optional true} [:vector Ref]]])

(def MarkdownDocument
  [:map {:closed false}
   [:document/path :string]
   [:document/frontmatter :map]
   [:document/body :string]
   [:document/structure {:optional true} [:vector :any]]])

(def DiagramSource
  [:map {:closed false}
   [:diagram/id Id]
   [:diagram/language [:= :mermaid]]
   [:diagram/source :string]])

(def Artifact
  [:map {:closed false}
   [:artifact/id Id]
   [:artifact/kind :keyword]
   [:artifact/form {:optional true} :keyword]
   [:artifact/status {:optional true} :keyword]
   [:artifact/epistemic-tier {:optional true} EpistemicTier]
   [:artifact/data {:optional true} :map]
   [:artifact/source {:optional true} Ref]
   [:artifact/relations {:optional true} [:vector Relation]]])

(def Condition
  [:schema
   {:registry
    {::condition
     [:multi {:dispatch :condition/op}
      [:eq [:map {:closed true}
            [:condition/op [:= :eq]]
            [:condition/path [:vector PathSegment]]
            [:condition/value :any]]]
      [:not-eq [:map {:closed true}
                [:condition/op [:= :not-eq]]
                [:condition/path [:vector PathSegment]]
                [:condition/value :any]]]
      [:exists [:map {:closed true}
               [:condition/op [:= :exists]]
               [:condition/path [:vector PathSegment]]]]
      [:in [:map {:closed true}
           [:condition/op [:= :in]]
           [:condition/path [:vector PathSegment]]
           [:condition/values [:vector :any]]]]
      [:and [:map {:closed true}
            [:condition/op [:= :and]]
            [:condition/clauses [:vector {:min 1} [:ref ::condition]]]]]
      [:or [:map {:closed true}
           [:condition/op [:= :or]]
           [:condition/clauses [:vector {:min 1} [:ref ::condition]]]]]
      [:not [:map {:closed true}
            [:condition/op [:= :not]]
            [:condition/clause [:ref ::condition]]]]]}}
   ::condition])

(def Event
  [:map {:closed false}
   [:event/id Id]
   [:event/type :keyword]
   [:event/at {:optional true} :string]
   [:event/source {:optional true} Ref]
   [:event/subject {:optional true} Ref]
   [:event/data {:optional true} :map]
   [:event/causes {:optional true} [:vector Ref]]])

(def PortableData
  "Recursive EDN-like data allowed to cross the Alpha operation boundary.
   Runtime values such as functions, host objects, and implementation handles
   are deliberately excluded."
  [:schema
   {:registry
    {::portable-data
     [:or nil?
      boolean?
      number?
      string?
      keyword?
      uuid?
      [:vector [:ref ::portable-data]]
      [:set [:ref ::portable-data]]
      [:map-of [:or keyword? string?] [:ref ::portable-data]]]}}
   [:ref ::portable-data]])

(def OperationRef
  [:map {:closed true}
   [:operation/id Id]
   [:operation/with {:optional true}
    [:map-of [:or keyword? string?] PortableData]]
   [:operation/in {:optional true}
    [:map-of [:or keyword? string?] PortableData]]])

(def ReactionTrigger
  [:map {:closed false}
   [:event/type :keyword]
   [:artifact/kind {:optional true} :keyword]
   [:subject/type {:optional true} :keyword]])

(def Reaction
  [:map {:closed false}
   [:reaction/id Id]
   [:reaction/on ReactionTrigger]
   [:reaction/when {:optional true} Condition]
   [:reaction/do OperationRef]
   [:reaction/enabled? {:optional true} :boolean]])

(def schemas
  {:alpha/ref Ref
   :alpha/artifact-ref ArtifactRef
   :alpha/relation Relation
   :alpha/markdown-document MarkdownDocument
   :alpha/diagram-source DiagramSource
   :alpha/artifact Artifact
   :alpha/condition Condition
   :alpha/event Event
   :alpha/portable-data PortableData
   :alpha/operation-ref OperationRef
   :alpha/reaction Reaction})

(defn schema [schema-id]
  (or (get schemas schema-id)
      (throw (ex-info "Unknown Alpha schema" {:schema/id schema-id}))))

(defn shape-errors [schema-id value]
  (some-> (m/explain (schema schema-id) value) me/humanize))

(defn validate-shape [schema-id value]
  (if-let [errors (shape-errors schema-id value)]
    {:valid? false :schema/id schema-id :errors errors}
    {:valid? true :schema/id schema-id :value value}))

(defn valid-shape? [schema-id value]
  (:valid? (validate-shape schema-id value)))

(defn assert-shape! [schema-id value]
  (let [result (validate-shape schema-id value)]
    (if (:valid? result)
      value
      (throw (ex-info "Alpha shape validation failed" result)))))

(defn- embedded-relation-errors [artifact]
  (->> (:artifact/relations artifact)
       (keep-indexed
        (fn [idx relation]
          (let [source (:relation/source relation)]
            (when-not (and (= :artifact (:ref/type source))
                           (= (:artifact/id artifact) (:ref/id source)))
              {:law/id :alpha/artifact-owns-embedded-relation-source
               :path [:artifact/relations idx :relation/source]
               :expected {:ref/type :artifact :ref/id (:artifact/id artifact)}
               :actual source}))))
       vec))

(defn validate-artifact [artifact]
  (let [shape (validate-shape :alpha/artifact artifact)]
    (if-not (:valid? shape)
      shape
      (let [law-errors (embedded-relation-errors artifact)]
        (if (seq law-errors)
          {:valid? false :schema/id :alpha/artifact :law-errors law-errors}
          shape)))))

(defn validate-reaction
  "Validate a reaction against both its portable shape and the operation ids
   registered by the caller. The registry may be a map or set keyed by operation id."
  [operation-registry reaction]
  (let [shape (validate-shape :alpha/reaction reaction)]
    (if-not (:valid? shape)
      shape
      (let [operation-id (get-in reaction [:reaction/do :operation/id])]
        (if (contains? operation-registry operation-id)
          shape
          {:valid? false
           :schema/id :alpha/reaction
           :law-errors
           [{:law/id :alpha/reaction-operation-registered
             :path [:reaction/do :operation/id]
             :actual operation-id}]})))))

(defn artifact? [value] (:valid? (validate-artifact value)))
(defn relation? [value] (valid-shape? :alpha/relation value))
(defn event? [value] (valid-shape? :alpha/event value))
(defn reaction-shape? [value] (valid-shape? :alpha/reaction value))
(defn reaction? [operation-registry value]
  (:valid? (validate-reaction operation-registry value)))
