(ns alpha.law.reaction
  (:require [alpha.law.artifact :as artifact]
            [katamorph.condition :as condition]))

(defn trigger-matches? [trigger event subject-artifact]
  (and (= (:event/type trigger) (:event/type event))
       (or (nil? (:artifact/kind trigger))
           (= (:artifact/kind trigger) (:artifact/kind subject-artifact)))
       (or (nil? (:subject/type trigger))
           (= (:subject/type trigger) (get-in event [:event/subject :ref/type])))))

(defn condition-context [event subject-artifact]
  {:event event :artifact subject-artifact})

(defn- bound-artifact? [event subject-artifact]
  (or (nil? subject-artifact)
      (and (artifact/artifact? subject-artifact)
           (= :artifact (get-in event [:event/subject :ref/type]))
           (= (:artifact/id subject-artifact)
              (get-in event [:event/subject :ref/id])))))

(defn matches? [operation-registry reaction event subject-artifact]
  (and (artifact/reaction? operation-registry reaction)
       (artifact/event? event)
       (bound-artifact? event subject-artifact)
       (not= false (:reaction/enabled? reaction))
       (trigger-matches? (:reaction/on reaction) event subject-artifact)
       (if-let [predicate (:reaction/when reaction)]
         (condition/match? (condition-context event subject-artifact) predicate)
         true)))

(defn select [operation-registry reactions event subject-artifact]
  (filterv #(matches? operation-registry % event subject-artifact) reactions))
