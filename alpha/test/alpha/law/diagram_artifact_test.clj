(ns alpha.law.diagram-artifact-test
  (:require [alpha.law.artifact :as artifact]
            [alpha.law.diagram-artifact :as diagram-artifact]
            [alpha.shape.mermaid :as mermaid]
            [clojure.test :refer [deftest is]]))

(def source
  (str "flowchart LR\n"
       "  A[Source] --> B[Lawful graph]\n"
       "  B --> C[Artifact]\n"))

(deftest decoded-diagrams-project-as-lawful-artifacts
  (let [decoded (mermaid/parse :workflow/example source)
        result (diagram-artifact/project (:diagram decoded))
        value (:artifact result)]
    (is (:ok decoded))
    (is (:ok result))
    (is (artifact/artifact? value))
    (is (= :workflow/example (:artifact/id value)))
    (is (= :diagram (:artifact/kind value)))
    (is (= :mermaid (:artifact/form value)))
    (is (= source (get-in value [:artifact/data :diagram/source])))
    (is (= (get-in decoded [:diagram :diagram/graph])
           (get-in value [:artifact/data :diagram/graph])))))

(deftest source-remains-required
  (let [decoded (:diagram (mermaid/parse :workflow/example source))]
    (is (= :missing-source
           (:reason (diagram-artifact/project
                     (dissoc decoded :diagram/source)))))))
