(ns eta.tools
  "Built-in agent tools: read, write, bash, nrepl."
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io File]))

;; ─── Tool Definitions (OpenAI function-calling format) ────────────

(def tool-defs
  "OpenAI-format tool definitions."
  [{:type "function"
    :function
    {:name "read"
     :description "Read the contents of a file. Returns up to 2000 lines or 50KB."
     :parameters
     {:type "object"
      :properties
      {:path   {:type "string" :description "Path to the file (relative or absolute)"}
       :offset {:type "integer" :description "Line number to start from (1-indexed, optional)"}
       :limit  {:type "integer" :description "Max lines to read (optional)"}}
      :required ["path"]}}}

   {:type "function"
    :function
    {:name "write"
     :description "Write content to a file. Creates parent directories. Overwrites if exists."
     :parameters
     {:type "object"
      :properties
      {:path    {:type "string" :description "Path to the file"}
       :content {:type "string" :description "Content to write"}}
      :required ["path" "content"]}}}

   {:type "function"
    :function
    {:name "bash"
     :description "Execute a bash command. Returns stdout and stderr."
     :parameters
     {:type "object"
      :properties
      {:command {:type "string" :description "Bash command to execute"}
       :timeout {:type "integer" :description "Timeout in seconds (optional, default 30)"}}
      :required ["command"]}}}

   {:type "function"
    :function
    {:name "nrepl"
     :description "Evaluate a Clojure expression in a running nREPL server."
     :parameters
     {:type "object"
      :properties
      {:host {:type "string" :description "nREPL host (default localhost)"}
       :port {:type "integer" :description "nREPL port"}
       :code {:type "string" :description "Clojure code to evaluate"}}
      :required ["port" "code"]}}}])

;; ─── Tool Implementations ─────────────────────────────────────────

(defn- normalize-path
  "Expand ~ and resolve relative paths."
  [^String path]
  (let [expanded (if (.startsWith path "~")
                   (str/replace path "~" (System/getProperty "user.home"))
                   path)]
    (.getCanonicalPath (File. expanded))))

(defn- safe-read-file
  "Read file with line offset/limit."
  [path offset limit]
  (let [f (io/file (normalize-path path))]
    (if-not (.exists f)
      {:error (str "File not found: " path)}
      (let [lines (vec (line-seq (io/reader f)))
            start (max 0 (dec (or offset 1)))
            end   (min (count lines) (+ start (or limit 2000)))
            lines-to-return (if (>= start (count lines))
                              []
                              (subvec lines start end))]
        {:path   (.getCanonicalPath f)
         :lines  lines-to-return
         :total  (count lines)
         :offset (inc start)
         :truncated (> (count lines) end)}))))

(def tool-read
  "Read file tool handler."
  (fn [{:keys [path offset limit]}]
    (try
      (let [result (safe-read-file path offset limit)]
        (if (:error result)
          {:error (:error result)}
          {:content (str "File: " (:path result) "\n"
                        "Lines " (:offset result) "-" 
                        (+ (:offset result) (count (:lines result)) -1)
                        " of " (:total result)
                        (when (:truncated result) " (truncated)")
                        "\n\n"
                        (str/join "\n" (:lines result)))}))
      (catch Exception e
        {:error (str "Read failed: " (.getMessage e))}))))

(def tool-write
  "Write file tool handler."
  (fn [{:keys [path content]}]
    (try
      (let [f (io/file (normalize-path path))]
        (.mkdirs (.getParentFile f))
        (spit f content)
        {:content (str "Wrote " (count content) " bytes to " (.getCanonicalPath f))})
      (catch Exception e
        {:error (str "Write failed: " (.getMessage e))}))))

(def tool-bash
  "Execute bash command tool handler."
  (fn [{:keys [command timeout]}]
    (try
      (let [timeout-ms (* 1000 (or timeout 30))
            proc (.exec (Runtime/getRuntime) 
                        ^"[Ljava.lang.String;" 
                        (into-array String ["bash" "-c" command]))
            ;; Consume streams in parallel threads to avoid deadlock
            stdout-future (future (slurp (.getInputStream proc)))
            stderr-future (future (slurp (.getErrorStream proc)))
            exited (.waitFor proc (long timeout-ms) 
                            java.util.concurrent.TimeUnit/MILLISECONDS)
            stdout @stdout-future
            stderr @stderr-future
            exit-code (if exited (.exitValue proc) -1)]
        {:content (str "Exit: " exit-code
                       (when (seq stdout) (str "\n" stdout))
                       (when (seq stderr) (str "\n[stderr] " stderr))
                       (when (not exited) "\n[TIMEOUT]"))})
      (catch Exception e
        {:error (str "Bash failed: " (.getMessage e))}))))

(defn- nrepl-eval
  "Evaluate code via nREPL client."
  [host port code]
  (try
    (require 'nrepl.core)
    (let [connect  (resolve 'nrepl.core/connect)
          client   (resolve 'nrepl.core/client)
          message  (resolve 'nrepl.core/message)
          close    (resolve 'nrepl.core/close)
          transport (connect {:host host :port port})
          clj-client (client transport)
          results (message clj-client {:op "eval" :code code})
          values (keep :value results)
          out (str/join "" (keep :out results))
          err (str/join "" (keep :err results))
          status (last (keep :status results))]
      (when close (close clj-client))
      {:values (vec values)
       :out    out
       :err    err
       :status status})
    (catch Exception e
      {:error (str "nREPL failed: " (.getMessage e))})))

(def tool-nrepl
  "nREPL eval tool handler."
  (fn [{:keys [host port code]}]
    (let [result (nrepl-eval (or host "localhost") port code)]
      (if (:error result)
        {:error (:error result)}
        {:content (str (when (seq (:values result))
                         (str/join "\n" (:values result)))
                       (when (seq (:out result))
                         (str "\n;; stdout:\n" (:out result)))
                       (when (seq (:err result))
                         (str "\n;; stderr:\n" (:err result))))}))))

;; ─── Tool Dispatch ────────────────────────────────────────────────

(def tool-handlers
  "Map of tool name → handler fn."
  {"read"   tool-read
   "write"  tool-write
   "bash"   tool-bash
   "nrepl"  tool-nrepl})

(defn- format-tool-result
  "Format tool result into a string for the LLM."
  [result]
  (cond
    (:error result)  (json/generate-string {:error (:error result)})
    (:content result) (:content result)
    :else (json/generate-string result)))

(defn dispatch-tool
  "Execute a tool call and return the result string.
   tool-call is an OpenAI tool_call object."
  [tool-call]
  (let [fname    (get-in tool-call [:function :name])
        args-str (get-in tool-call [:function :arguments])
        args     (if (string? args-str) (json/parse-string args-str true) args-str)
        handler  (get tool-handlers fname)]
    (if handler
      (let [result (handler args)]
        {:tool_call_id (:id tool-call)
         :role         "tool"
         :content      (format-tool-result result)})
      {:tool_call_id (:id tool-call)
       :role         "tool"
       :content      (json/generate-string {:error (str "Unknown tool: " fname)})})))
