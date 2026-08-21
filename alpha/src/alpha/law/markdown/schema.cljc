(ns alpha.law.markdown.schema
  (:require [alpha.law.artifact :as artifact]
            [katamorph.schema.condition :as condition]
            [katamorph.schema.core :as schema]))

(def FrontmatterPath
  [:vector condition/PathSegment])

(def FacetTarget
  [:enum :artifact :document :frontmatter])

(def FacetAssertion
  [:map {:closed false}
   [:facet/schema :keyword]
   [:facet/target FacetTarget]])

(def MarkdownProfile
  [:map {:closed false}
   [:profile/id artifact/Id]
   [:profile/id-path FrontmatterPath]
   [:profile/kind-path FrontmatterPath]
   [:profile/status-path {:optional true} FrontmatterPath]
   [:profile/facets {:optional true} [:vector FacetAssertion]]])

(def schemas
  {:alpha/facet-assertion FacetAssertion
   :alpha/markdown-profile MarkdownProfile})

(def schema-composition
  (schema/compose-registries artifact/schemas schemas))

(def all-schemas
  (:registry schema-composition))
