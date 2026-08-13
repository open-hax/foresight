(ns eta.provider
  "PROXX_* provider client  - OpenAI-compatible chat completions API."
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str])
  (:import [java.io BufferedReader InputStreamReader]))

;; ─── Config ───────────────────────────────────────────────────────

(def default-model "mimo-v2.5-pro")

(defn env-or
  "Read env var with fallback."
  [k fallback]
  (or (System/getenv k) fallback))

(defn provider-config
  "Build provider config from PROXX_* env vars."
  []
  {:base-url  (env-or "PROXX_URL"       "http://localhost:8789")
   :auth-token (env-or "PROXX_AUTH_TOKEN" "")
   :model      (env-or "PROXX_MODEL"     default-model)})

;; ─── API ──────────────────────────────────────────────────────────

(defn- url [config path]
  (str (:base-url config) path))

(defn- auth-headers [config]
  {"Authorization" (str "Bearer " (:auth-token config))
   "Content-Type"  "application/json"})

(defn list-models
  "Fetch available models from provider."
  ([] (list-models (provider-config)))
  ([config]
   (let [resp (http/get (url config "/v1/models")
                        {:headers         (auth-headers config)
                         :as              :json
                         :decompress-body false})]
     (get-in resp [:body :data]))))

(defn chat-completions
  "Send chat completion request. Returns the full response body map.
   
   opts keys:
     :model         - model id (default from config)
     :messages      - [{:role :content} ...]
     :tools         - [tool-def ...]  (optional)
     :temperature   - float (optional)
     :max-tokens    - int   (optional)
     :stream        - bool  (optional, default false)"
  ([messages] (chat-completions (provider-config) messages {}))
  ([config messages opts]
   (let [body (cond-> {:model    (or (:model opts) (:model config))
                       :messages messages}
                (:tools opts)       (assoc :tools (:tools opts))
                (:temperature opts) (assoc :temperature (:temperature opts))
                (:max-tokens opts)  (assoc :max_tokens (:max-tokens opts))
                (:stream opts)      (assoc :stream (:stream opts)))]
     (let [resp (http/post (url config "/v1/chat/completions")
                           {:headers          (auth-headers config)
                            :body             (json/generate-string body)
                            :as               :json
                            :decompress-body  false
                            :socket-timeout   120000
                            :conn-timeout     10000})]
       (:body resp)))))

(defn extract-choice
  "Get the first choice content from a completion response."
  [response]
  (-> response :choices first :message))

(defn extract-tool-calls
  "Extract tool calls from response message, or nil."
  [message]
  (:tool_calls message))

(defn extract-content
  "Extract text content from response message, or nil."
  [message]
  (:content message))

;; ─── Streaming ───────────────────────────────────────────────────

(defn- parse-sse-line
  "Parse a single SSE data line. Returns parsed JSON or nil."
  [^String line]
  (when (and line (.startsWith line "data: "))
    (let [data (subs line 6)]
      (when-not (= data "[DONE]")
        (try (json/parse-string data true) (catch Exception _ nil))))))

(defn- read-sse-stream
  "Lazy seq of parsed SSE chunks from an input stream."
  [is]
  (let [reader (BufferedReader. (InputStreamReader. is "UTF-8"))]
    (->> (repeatedly #(try (.readLine reader) (catch Exception _ nil)))
         (take-while some?)
         (keep parse-sse-line))))

(defn chat-completions-stream
  "Send streaming chat completion request. Returns a lazy seq of parsed chunks.
   Each chunk is {:choices [{:delta {:content ...}}]} etc.

   opts keys:
     :model         - model id (default from config)
     :messages      - [{:role :content} ...]
     :tools         - [tool-def ...]  (optional)
     :temperature   - float (optional)
     :max-tokens    - int   (optional)"
  ([messages] (chat-completions-stream (provider-config) messages {}))
  ([config messages opts]
   (let [body (cond-> {:model    (or (:model opts) (:model config))
                       :messages messages
                       :stream   true}
                (:tools opts)       (assoc :tools (:tools opts))
                (:temperature opts) (assoc :temperature (:temperature opts))
                (:max-tokens opts)  (assoc :max_tokens (:max-tokens opts)))]
     (let [resp (http/post (url config "/v1/chat/completions")
                           {:headers          (auth-headers config)
                            :body             (json/generate-string body)
                            :as               :stream
                            :decompress-body  false
                            :socket-timeout   300000
                            :conn-timeout     10000})]
       (read-sse-stream (:body resp))))))

(defn stream-delta-text
  "Extract delta text content from a streaming chunk, or nil."
  [chunk]
  (get-in chunk [:choices 0 :delta :content]))

(defn stream-delta-tool-calls
  "Extract tool call deltas from a streaming chunk."
  [chunk]
  (get-in chunk [:choices 0 :delta :tool_calls]))

(defn stream-finish-reason
  "Extract finish reason from a streaming chunk, or nil."
  [chunk]
  (get-in chunk [:choices 0 :finish_reason]))
