(ns alpha.law.markdown.resolver
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.markdown.facet :as facet]
            [alpha.law.markdown.profile-registry :as profiles]))

(defn- decoding [document]
  (:document/frontmatter/decoding document))

(defn- with-decode-evidence [result document]
  (if-let [evidence (decoding document)]
    (assoc result :document/frontmatter/decoding evidence)
    result))

(defn resolve-artifact
  "Validate Markdown, select exactly one profile, project an Artifact, then validate facets.

   A failed frontmatter decoder is never used as semantic input. Partial decode
   evidence remains usable but is attached to every result so callers can
   distinguish a fail-closed decision made from incomplete interpretation."
  ([schema-registry profile-registry document]
   (resolve-artifact schema-registry profile-registry document nil))
  ([schema-registry profile-registry document caller-context]
   (let [document-shape (artifact/validate-shape :alpha/markdown-document document)
         decode-evidence (decoding document)]
     (cond
       (not (:valid? document-shape))
       {:ok false
        :stage :document-validation
        :reason :invalid-document
        :errors (:errors document-shape)}

       (= :failed (:decode/status decode-evidence))
       (with-decode-evidence
        {:ok false
         :stage :frontmatter-decoding
         :reason :frontmatter-decode-failed}
        document)

       :else
       (let [selected (profiles/select profile-registry document caller-context)]
         (if-not (:ok selected)
           (with-decode-evidence
            (assoc selected :stage :profile-selection)
            document)
           (let [profile (:profile selected)
                 resolved (facet/validate-profiled-document
                           schema-registry profile document)
                 result (cond
                          (:ok resolved)
                          (assoc resolved :profile profile)

                          (:stage resolved)
                          (assoc resolved :profile profile)

                          :else
                          (assoc resolved
                                 :stage :facet-validation
                                 :profile profile))]
             (with-decode-evidence result document))))))))
