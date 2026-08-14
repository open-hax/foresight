(ns alpha.law.markdown.selection
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.markdown.schema :as markdown]
            [katamorph.condition :as condition]
            [malli.core :as m]))

(def SelectableProfile
  [:and markdown/MarkdownProfile
   [:map {:closed false}
    [:profile/when {:optional true} artifact/Condition]]])

(defn valid-profile? [profile]
  (m/validate SelectableProfile profile))

(defn- frontmatter-data [document]
  (or (:document/frontmatter/data document)
      (:document/frontmatter document)
      {}))

(defn context
  ([document] (context document nil))
  ([document caller-context]
   {:document document
    :frontmatter (frontmatter-data document)
    :context caller-context}))

(defn matches?
  ([profile document] (matches? profile document nil))
  ([profile document caller-context]
   (and (valid-profile? profile)
        (if-let [predicate (:profile/when profile)]
          (condition/match? (context document caller-context) predicate)
          true))))

(defn select
  "Select exactly one valid profile. Zero or multiple matches fail closed."
  ([profiles document] (select profiles document nil))
  ([profiles document caller-context]
   (let [invalid (filterv (complement valid-profile?) profiles)]
     (if (seq invalid)
       {:ok false
        :reason :invalid-profile
        :profile/ids (mapv :profile/id invalid)}
       (let [matches (filterv #(matches? % document caller-context) profiles)]
         (case (count matches)
           0 {:ok false :reason :no-profile-match}
           1 {:ok true :profile (first matches)}
           {:ok false
            :reason :ambiguous-profile
            :profile/ids (->> matches (map :profile/id) (sort-by str) vec)}))))))
