(ns alpha.law.markdown.profile
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.markdown.schema :as markdown]
            [katamorph.schema.core :as schema]))

(def ^:private missing ::missing)

(defn- frontmatter-data [document]
  (or (:document/frontmatter/data document)
      (:document/frontmatter document)
      {}))

(defn- frontmatter-value [document path]
  (get-in (frontmatter-data document) path missing))

(defn- normalize-keyword [value]
  (cond
    (keyword? value) value
    (string? value) (keyword value)
    :else value))

(defn project-artifact
  "Project a parsed Markdown document through one declarative profile."
  [profile document]
  (let [document-result (schema/validate artifact/schemas
                                         :alpha/markdown-document
                                         document)
        profile-result (schema/validate markdown/schemas
                                        :alpha/markdown-profile
                                        profile)]
    (cond
      (not (:ok document-result))
      {:ok false :stage :document :errors (:errors document-result)}

      (not (:ok profile-result))
      {:ok false :stage :profile :errors (:errors profile-result)}

      :else
      (let [artifact-id (frontmatter-value document (:profile/id-path profile))
            artifact-kind (normalize-keyword
                           (frontmatter-value document (:profile/kind-path profile)))
            status-path (:profile/status-path profile)
            artifact-status (when status-path
                              (normalize-keyword
                               (frontmatter-value document status-path)))
            missing-fields (cond-> []
                             (= missing artifact-id) (conj :artifact/id)
                             (= missing artifact-kind) (conj :artifact/kind))]
        (if (seq missing-fields)
          {:ok false
           :stage :projection
           :errors [{:path []
                     :message "profile paths did not resolve required artifact fields"
                     :missing missing-fields}]}
          (let [projected (cond->
                           {:artifact/id artifact-id
                            :artifact/kind artifact-kind
                            :artifact/form :markdown
                            :artifact/data (frontmatter-data document)}
                            (and status-path (not= missing artifact-status))
                            (assoc :artifact/status artifact-status))
                validation (artifact/validate-artifact projected)]
            (if (:valid? validation)
              {:ok true :artifact projected :document document :profile profile}
              {:ok false :stage :artifact :errors validation})))))))
