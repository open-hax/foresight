(ns alpha.shape.mermaid-edge
  (:require [clojure.string :as str]))

(def id-re #"^[A-Za-z][A-Za-z0-9_.:-]*$")

(defn- target [segment]
  (let [value (str/trim segment)]
    (if (str/starts-with? value "|")
      (when-let [close (str/index-of value "|" 1)]
        {:label (subs value 1 close)
         :id (str/trim (subs value (inc close)))})
      {:id value})))

(defn solid [line]
  (when (str/includes? line "-->")
    (let [segments (str/split (str/trim line) #"\s*-->\s*")
          source (first segments)]
      (when (and (re-matches id-re source) (> (count segments) 1))
        (loop [from source remaining (rest segments) edges []]
          (if-let [segment (first remaining)]
            (when-let [{:keys [id label]} (target segment)]
              (when (re-matches id-re id)
                (recur id
                       (rest remaining)
                       (conj edges
                             (cond-> {:edge/from from :edge/to id :edge/style :solid}
                               (seq label) (assoc :edge/label label))))))
            edges))))))

(defn dotted [line]
  (when-let [[_ from label to]
             (re-matches #"^([A-Za-z][A-Za-z0-9_.:-]*)\s+-\.\s*(.*?)\s*\.->\s*([A-Za-z][A-Za-z0-9_.:-]*)$"
                         (str/trim line))]
    (cond-> {:edge/from from :edge/to to :edge/style :dotted}
      (seq (str/trim label)) (assoc :edge/label (str/trim label)))))
