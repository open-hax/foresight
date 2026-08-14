(ns alpha.shape.mermaid-test
  (:require [alpha.shape.mermaid :as mermaid]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(defn- repo-root []
  (let [cwd (io/file (System/getProperty "user.dir"))]
    (if (= "alpha" (.getName cwd))
      (.getParentFile cwd)
      cwd)))

(defn- workflow-files []
  (let [dir (io/file (repo-root) "docs" "architecture" "workflows")]
    (->> (.listFiles dir)
         (filter #(.isFile %))
         (filter #(str/ends-with? (.getName %) ".mmd"))
         (sort-by #(.getName %)))))

(deftest every-canonical-diagram-is-readable-code
  (let [files (workflow-files)]
    (is (seq files))
    (doseq [file files]
      (testing (.getName file)
        (let [result (mermaid/parse (keyword "workflow" (.getName file))
                                    (slurp file))]
          (is (:ok result))
          (is (seq (get-in result [:graph :graph/nodes])))
          (is (seq (get-in result [:graph :graph/edges]))))))))
