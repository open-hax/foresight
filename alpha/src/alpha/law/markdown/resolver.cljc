(ns alpha.law.markdown.resolver
  (:require [alpha.law.markdown.facet :as facet]
            [alpha.law.markdown.profile-registry :as profiles]))

(defn resolve-artifact
  "Select exactly one Markdown profile, project an Artifact, then validate its facets.

   Selection, projection, and facet failures remain distinct so callers can
   surface structural findings without collapsing them into one generic error."
  ([schema-registry profile-registry document]
   (resolve-artifact schema-registry profile-registry document nil))
  ([schema-registry profile-registry document caller-context]
   (let [selected (profiles/select profile-registry document caller-context)]
     (if-not (:ok selected)
       (assoc selected :stage :profile-selection)
       (let [profile (:profile selected)
             resolved (facet/validate-profiled-document
                       schema-registry profile document)]
         (cond
           (:ok resolved)
           (assoc resolved :profile profile)

           (:stage resolved)
           (assoc resolved :profile profile)

           :else
           (assoc resolved :stage :facet-validation :profile profile)))))))
