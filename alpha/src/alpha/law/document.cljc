(ns alpha.law.document
  "Portable admission of document maps assembled from a supplied source observation.
   These laws check data and referential context, not source truth or live freshness."
  (:require [alpha.law.artifact :as artifact]
            [katamorph.schema.core :as schema]))

(def PortableMap
  [:map-of [:or keyword? string?] artifact/PortableData])

(def SchemaId
  [:or :keyword [:string {:min 1}]])

(def ContentDigest
  [:map {:closed true}
   [:digest/algorithm :keyword]
   [:digest/value [:string {:min 1}]]])

(def ObservationV1
  [:and artifact/PortableData
   [:map {:closed true}
    [:observation/schema-version [:= 1]]
    [:observation/id artifact/Id]
    [:observation/source artifact/Ref]
    [:observation/observed-at [:string {:min 1}]]
    [:observation/coverage [:enum :full :partial]]
    [:observation/retention [:enum :ephemeral :source-owned :retained]]
    [:observation/capabilities [:set [:enum :read :refresh :write]]]
    [:observation/value artifact/PortableData]
    [:observation/content-digest {:optional true} ContentDigest]
    [:observation/context {:optional true} PortableMap]]])

(def AssemblySignatureV1
  [:and artifact/PortableData
   [:map {:closed true}
    [:assembly/id artifact/Id]
    [:assembly/version [:string {:min 1}]]
    [:assembly/input-schema SchemaId]
    [:assembly/output-schema SchemaId]]])

(def DocumentV1
  [:and artifact/PortableData
   [:map {:closed true}
    [:document/schema-version [:= 1]]
    [:document/source artifact/Ref]
    [:document/observation-id artifact/Id]
    [:document/assembly AssemblySignatureV1]
    [:document/value PortableMap]]])

(def schemas
  {:alpha/document-observation-v1 ObservationV1
   :alpha/document-assembly-signature-v1 AssemblySignatureV1
   :alpha/document-v1 DocumentV1})

(defn validate-observation [observation]
  (schema/validate schemas :alpha/document-observation-v1 observation))

(defn validate-document [document]
  (schema/validate schemas :alpha/document-v1 document))

(defn- context-errors [observation document]
  (cond-> []
    (not= (:observation/id observation) (:document/observation-id document))
    (conj {:law/id :alpha/document-selected-observation-matches
           :path [:document/observation-id]
           :expected (:observation/id observation)
           :actual (:document/observation-id document)})

    (not= (:observation/source observation) (:document/source document))
    (conj {:law/id :alpha/document-observed-source-matches
           :path [:document/source]
           :expected (:observation/source observation)
           :actual (:document/source document)})))

(defn admit-document
  "Validate a supplied observation, its document context, and the declared input
   and output schema kinds in the caller's registry. No assembly is executed.

   An admitted map does not establish truth, persistence, write authority, or
   current freshness. Adapters must qualify those capabilities separately."
  [registry observation document]
  (let [observation-result (validate-observation observation)
        document-result (validate-document document)]
    (cond
      (not (:ok observation-result))
      (assoc observation-result :stage :observation)

      (not (:ok document-result))
      (assoc document-result :stage :document)

      :else
      (let [errors (context-errors observation document)]
        (if (seq errors)
          {:ok false :stage :context :errors errors}
          (let [assembly (:document/assembly document)
                input-result (schema/validate registry
                                              (:assembly/input-schema assembly)
                                              (:observation/value observation))
                output-result (schema/validate registry
                                               (:assembly/output-schema assembly)
                                               (:document/value document))]
            (cond
              (not (:ok input-result)) (assoc input-result :stage :input)
              (not (:ok output-result)) (assoc output-result :stage :output)
              :else {:ok true :observation observation :document document})))))))
