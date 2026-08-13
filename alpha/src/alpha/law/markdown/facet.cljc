(ns alpha.law.markdown.facet
  (:require [alpha.law.markdown.profile :as profile]
            [katamorph.schema.core :as schema]))

(defn- facet-value [target document projected]
  (case target
    :artifact projected
    :document document
    :frontmatter (:document/frontmatter document)))

(defn validate-facets
  "Validate all profile facet assertions against caller-supplied schemas."
  [registry markdown-profile document projected]
  (let [results
        (mapv
         (fn [{:facet/keys [schema target]}]
           (assoc (schema/validate registry
                                   schema
                                   (facet-value target document projected))
                  :facet/schema schema
                  :facet/target target))
         (:profile/facets markdown-profile []))]
    {:ok (every? :ok results)
     :results results}))

(defn validate-profiled-document
  "Project a Markdown document, then validate every asserted facet."
  [registry markdown-profile document]
  (let [projection (profile/project-artifact markdown-profile document)]
    (if-not (:ok projection)
      projection
      (let [facets (validate-facets registry
                                    markdown-profile
                                    document
                                    (:artifact projection))]
        (assoc projection :facets facets :ok (:ok facets))))))
