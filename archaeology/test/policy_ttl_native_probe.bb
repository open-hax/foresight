#!/usr/bin/env bb
;; Run from the repository root with archaeology/src and the pinned Clio/Malli
;; sources on the classpath. This validates these two resources only, not the
;; complete historical causal closure or a live MongoDB instance.
(require '[foresight.archaeology.shape :as shape]
         '[foresight.archaeology.domain :as domain]
         '[clio.domain.schema :as schema]
         '[clio.shape.edn :as wire]
         '[clojure.string :as str]
         '[babashka.fs :as fs])
(defn sha256 [text]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (apply str (map #(format "%02x" (bit-and % 255))
                   (.digest digest (.getBytes text "UTF-8"))))))
(def root (or (first *command-line-args*) "."))
(def revision (schema/materialize sha256 shape/catalog))
(assert (= "8eb19ab0e4b70e205a84a627fd3e15c3ba79c0fd8c792d37e72f3c79511e0bf9"
           (:schema/root revision)))
(println "Root" (:schema/root revision))
(def runs ["4da05854-5161-4351-a1d3-daf00fb4c3f1" "416ca87e-8071-4336-997d-9a4a391eb9e4"])
(def events
  (vec (mapcat
        (fn [run]
          (let [resource (wire/read-one (slurp (str (fs/path root ".ημ/archaeology/resources" (str run ".edn")))))]
            (assert (shape/valid-resource? resource))
            (mapcat (fn [{:ledger/keys [path]}]
                      (mapv wire/read-one (str/split-lines (slurp (str (fs/path root path))))))
                    (-> resource :resources first :archaeology/ledgers vals))))
        runs)))
(assert (= 36 (count events)))
(doseq [e events] (schema/validate-event! [revision] e))
(println "Validated events" (count events))
(doseq [run runs]
  (let [projection (domain/compose-run (reduce domain/apply-event domain/empty-state events) run)]
    (assert (= 3 (count (:archaeology/findings projection))))
    (println "Projected" run (count (:archaeology/findings projection)))))
