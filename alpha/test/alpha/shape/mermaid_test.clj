(ns alpha.shape.mermaid-test
  (:require [alpha.shape.mermaid :as mermaid]
            [clojure.test :refer [deftest is]]))

(def files
  ["alpha-eta-mu-pi.mmd"
   "artifact-reactive-flow.mmd"
   "epiphany-document-flow.mmd"
   "markdown-profile-facets.mmd"])

(deftest canonical-diagrams-parse
  (doseq [filename files]
    (let [source (slurp (str "../docs/architecture/workflows/" filename))
          result (mermaid/parse (keyword filename) source)]
      (is (:ok result))
      (is (seq (get-in result [:graph :graph/nodes])))
      (is (seq (get-in result [:graph :graph/edges]))))))
