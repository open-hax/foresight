(ns alpha.law.diagram-artifact
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.diagram :as diagram]))

(defn project
  "Project one decoded Mermaid diagram into a lawful Alpha Artifact.

   The source text remains present alongside the structured graph so parsing is
   a derived representation rather than replacement authority."
  [decoded]
  (let [{:diagram/keys [id language source graph]} decoded]
    (cond
      (nil? id)
      {:ok false :stage :diagram :reason :missing-id}

      (not= :mermaid language)
      {:ok false :stage :diagram :reason :unsupported-language}

      (not (string? source))
      {:ok false :stage :diagram :reason :missing-source}

      (not (:ok (diagram/validate graph)))
      {:ok false
       :stage :graph
       :errors (:errors (diagram/validate graph))}

      :else
      (let [value {:artifact/id id
                   :artifact/kind :diagram
                   :artifact/form :mermaid
                   :artifact/data {:diagram/language language
                                   :diagram/source source
                                   :diagram/graph graph}}
            validation (artifact/validate-artifact value)]
        (if (:valid? validation)
          {:ok true :artifact value :diagram decoded}
          {:ok false :stage :artifact :errors validation})))))
