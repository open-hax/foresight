(ns foresight.river-city.collector
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]
            [foresight.river-city.infra :as infra]
            [foresight.river-city.law :as law]
            [foresight.river-city.shape :as host-shape]
            [river-city.domain.portwatch :as portwatch-domain]
            [river-city.shape.portwatch :as portwatch])
  (:import [java.net URI URLEncoder]
           [java.net.http HttpClient HttpRequest HttpResponse$BodyHandlers]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files StandardCopyOption]))

(def resource-path ".ημ/river-city/resources/imf-portwatch.edn")
(def projection-path ".ημ/river-city/projections/maritime.edn")

(defn- root-path []
  (.getCanonicalPath
   (io/file (or (System/getenv "FORESIGHT_ROOT") ".."))))

(defn- rooted [root path]
  (.getPath (io/file root path)))

(defn- encode [value]
  (URLEncoder/encode (str value) StandardCharsets/UTF_8))

(defn- query-string [pairs]
  (str/join "&"
            (map (fn [[k v]] (str (encode k) "=" (encode v))) pairs)))

(defn- fetch-portwatch! []
  (let [uri (URI/create
             (str portwatch/endpoint
                  "?"
                  (query-string
                   [["where" "1=1"]
                    ["outFields" (str/join "," portwatch/out-fields)]
                    ["orderByFields" "date DESC"]
                    ["resultRecordCount" 1000]
                    ["returnGeometry" "false"]
                    ["f" "json"]])))
        request (-> (HttpRequest/newBuilder uri) .GET .build)
        response (.send (HttpClient/newHttpClient)
                        request
                        (HttpResponse$BodyHandlers/ofString))]
    (when-not (= 200 (.statusCode response))
      (throw (ex-info "PortWatch request failed"
                      {:river-city/error :portwatch-http
                       :status (.statusCode response)
                       :body (.body response)})))
    (let [payload (json/read-str (.body response))]
      (when-let [error (get payload "error")]
        (throw (ex-info "PortWatch API returned an error"
                        {:river-city/error :portwatch-api
                         :provider/error error})))
      payload)))

(defn- normalize-observations [payload]
  (let [observations
        (->> (get payload "features")
             (map #(get % "attributes"))
             (filter #(portwatch/target-chokepoint? (get % "portname")))
             (map portwatch/normalize-attributes)
             (map portwatch/assert-observation!)
             vec)
        grouped (group-by :source/record-id observations)]
    (doseq [[record-id rows] grouped]
      (when (> (count (distinct rows)) 1)
        (throw (ex-info "PortWatch response contains conflicting copies of one source record"
                        {:river-city/error :duplicate-source-record
                         :source/record-id record-id
                         :rows rows}))))
    (->> grouped
         vals
         (map first)
         (sort-by (comp str :source/record-id))
         vec)))

(defn- read-resource! [root]
  (let [value (edn/read-string (slurp (rooted root resource-path)))]
    (infra/assert-resource! value)))

(defn- clio-launcher [root]
  (rooted root "eta-mu/packages/clio/bin/clio.mjs"))

(defn- run-clio! [root & args]
  (let [result (apply sh
                      (concat ["node" (clio-launcher root)]
                              args
                              [:dir root]))]
    (when-not (zero? (:exit result))
      (throw (ex-info "Clio command failed"
                      {:river-city/error :clio-command-failed
                       :args args
                       :exit (:exit result)
                       :stderr (:err result)
                       :stdout (:out result)})))
    (edn/read-string (:out result))))

(defn- blank-ledger? [root ledger-path]
  (str/blank? (slurp (rooted root ledger-path))))

(defn- canonical-events [root schema-dir ledger-path]
  (if (blank-ledger? root ledger-path)
    []
    (:canonical/events
     (run-clio! root "canonicalize" schema-dir ledger-path))))

(defn- current-by-stream [events]
  (into {}
        (map (fn [[stream stream-events]]
               [stream (apply max-key :event/seq stream-events)]))
        (group-by :event/stream events)))

(defn- append-observation!
  [root {:keys [schema-dir catalog-path ledger-path]} current observation]
  (let [stream (portwatch-domain/stream-id observation)
        previous (get current stream)]
    (if (= observation (:event/data previous))
      {:result :unchanged
       :current current}
      (let [event-data
            {:event/stream stream
             :event/seq (if previous (inc (:event/seq previous)) 1)
             :event/causes (if previous [(:event/id previous)] [])
             :event/actor "river-city:portwatch"
             :event/subject (portwatch-domain/subject observation)
             :event/data observation}
            appended
            (run-clio! root
                       "append"
                       schema-dir
                       catalog-path
                       ledger-path
                       (pr-str portwatch/event-type)
                       (pr-str event-data))
            event (:event appended)]
        {:result (:append/result appended)
         :event event
         :current (assoc current stream event)}))))

(defn- write-projection! [root projection]
  (let [path (rooted root projection-path)]
    (io/make-parents path)
    (let [parent (.getParentFile (io/file path))
          tmp (Files/createTempFile (.toPath parent)
                                    "river-city-maritime-"
                                    ".edn"
                                    (make-array java.nio.file.attribute.FileAttribute 0))]
      (spit (.toFile tmp) (str (pr-str projection) "\n"))
      (try
        (Files/move tmp
                    (.toPath (io/file path))
                    (into-array StandardCopyOption
                                [StandardCopyOption/REPLACE_EXISTING
                                 StandardCopyOption/ATOMIC_MOVE]))
        (catch java.nio.file.AtomicMoveNotSupportedException _
          (Files/move tmp
                      (.toPath (io/file path))
                      (into-array StandardCopyOption
                                  [StandardCopyOption/REPLACE_EXISTING])))))
    path))

(defn -main [& _]
  (let [root (root-path)
        resource (read-resource! root)
        entry (law/resource-entry resource)
        catalog-path (get-in entry [:river-city/schema :catalog/path])
        schema-dir (get-in entry [:river-city/schema :history/path])
        ledger-path (get-in entry [:river-city/ledgers :observations :ledger/path])
        catalog (edn/read-string (slurp (rooted root catalog-path)))]
    (when-not (= host-shape/catalog catalog)
      (throw (ex-info "Materialized River City catalog does not match source catalog"
                      {:river-city/error :catalog-drift
                       :catalog/path catalog-path})))
    (let [observations (normalize-observations (fetch-portwatch!))
          initial-events (canonical-events root schema-dir ledger-path)
          initial-current (current-by-stream initial-events)
          results
          (loop [remaining observations
                 current initial-current
                 results []]
            (if-let [observation (first remaining)]
              (let [result (append-observation!
                            root
                            {:schema-dir schema-dir
                             :catalog-path catalog-path
                             :ledger-path ledger-path}
                            current
                            observation)]
                (recur (next remaining)
                       (:current result)
                       (conj results (dissoc result :current))))
              results))
          final-events (canonical-events root schema-dir ledger-path)
          projection (portwatch-domain/project final-events)]
      (when-not (host-shape/valid-projection? projection)
        (throw (ex-info "River City projection failed host validation"
                        {:river-city/error :invalid-projection
                         :projection projection})))
      (write-projection! root projection)
      (prn {:river-city/job :portwatch
            :observations (count observations)
            :appended (count (remove #(= :unchanged (:result %)) results))
            :unchanged (count (filter #(= :unchanged (:result %)) results))
            :canonical-events (count final-events)
            :projection projection-path}))))
