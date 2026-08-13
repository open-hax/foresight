(ns eta.agent
  "Agent loop: message → LLM → tool calls → results → repeat.
   Supports streaming text output with non-streaming fallback."
  (:require [eta.provider :as provider]
            [eta.tools    :as tools]
            [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

;; ─── System Prompt ────────────────────────────────────────────────

(def default-system-prompt
  (str "You are a Clojure-savvy coding agent running in a terminal. "
       "You have access to tools: read files, write files, execute bash commands, "
       "and evaluate Clojure in running nREPL servers.\n\n"
       "Be concise. Use tools when needed. "
       "When writing Clojure, prefer idiomatic style.\n"
       "Current working directory: " (System/getProperty "user.dir")))

;; ─── Agent State ──────────────────────────────────────────────────

(def max-messages
  "Maximum messages to keep in history before trimming."
  50)

(defn- trim-messages
  "Keep system prompt + last N messages to avoid context overflow."
  [messages]
  (if (<= (count messages) max-messages)
    messages
    (let [system-msg (first messages)
          rest-msgs  (rest messages)
          keep-n     (- max-messages 1)
          trimmed    (vec (take-last keep-n rest-msgs))]
      (into [system-msg] trimmed))))

(defn make-state
  "Create initial agent state."
  ([] (make-state {}))
  ([{:keys [system-prompt model config]}]
   {:messages  (if system-prompt
                 [{:role "system" :content system-prompt}]
                 [{:role "system" :content default-system-prompt}])
    :config    (or config (provider/provider-config))
    :model     model
    :max-rounds 20}))

;; ─── Streaming Helpers ────────────────────────────────────────────

(defn- accumulate-tool-call-deltas
  "Merge streaming tool call deltas into accumulated tool calls.
   Deltas arrive piecewise: index, id, function.name, function.arguments chunks."
  [acc deltas]
  (if-not deltas
    acc
    (reduce
      (fn [acc delta]
        (let [idx      (:index delta)
              existing (get acc idx {})]
          (assoc acc idx
            (cond-> existing
              (:id delta)             (assoc :id (:id delta))
              (:type delta)           (assoc :type (:type delta))
              (:function delta)       (update :function
                                        (fn [fn-obj]
                                          (cond-> (or fn-obj {})
                                            (get-in delta [:function :name])
                                            (assoc :name (get-in delta [:function :name]))
                                            (get-in delta [:function :arguments])
                                            (update :arguments str (get-in delta [:function :arguments])))))))))
      acc
      deltas)))

(defn- finalize-tool-calls
  "Convert accumulated tool call map to a vector of complete tool calls."
  [acc]
  (->> acc
       (sort-by first)
       (mapv (fn [[_ tc]]
               (-> tc
                   (update :function
                     (fn [f]
                       (if (string? (:arguments f))
                         (assoc f :arguments (:arguments f))
                         (assoc f :arguments (json/generate-string (:arguments f)))))))))))

;; ─── Tool Execution ───────────────────────────────────────────────

(defn- print-tool-call
  "Print a summary of a tool call for the user."
  [tool-call]
  (let [fname (get-in tool-call [:function :name])
        args  (get-in tool-call [:function :arguments])
        args-parsed (cond
                      (nil? args) {}
                      (map? args) args
                      (string? args) (try (json/parse-string args true) (catch Exception _ {}))
                      :else {})]
    (case fname
      "read"  (println (str "\n  📖 read: " (:path args-parsed)))
      "write" (println (str "\n  📝 write: " (:path args-parsed)))
      "bash"  (println (str "\n  ⚡ bash: " (:command args-parsed)))
      "nrepl" (println (str "\n  🔌 nrepl:" (:port args-parsed) " — "
                           (subs (str (:code args-parsed)) 0 (min 60 (count (str (:code args-parsed)))))))
      (println (str "\n  🔧 " fname)))))

(defn- print-tool-result
  "Print a summary of a tool result."
  [result]
  (let [content (:content result)
        preview (if (> (count content) 300)
                  (str (subs content 0 300) "...")
                  content)]
    (println (str "  → " (str/replace preview "\n" "\n    ")))))

(defn- execute-tool-calls
  "Execute all tool calls and return tool result messages."
  [tool-calls]
  (mapv tools/dispatch-tool tool-calls))

;; ─── Non-streaming Step ───────────────────────────────────────────

(defn- call-llm
  "Call the LLM non-streaming with current messages and tools."
  [state]
  (let [config   (:config state)
        messages (:messages state)
        opts     (cond-> {:tools tools/tool-defs}
                   (:model state) (assoc :model (:model state)))]
    (provider/chat-completions config messages opts)))

(defn step-fallback
  "Non-streaming agent step. Returns [new-state response-text-or-nil]."
  [state]
  (let [response (call-llm state)
        message  (provider/extract-choice response)]
    (if (seq (provider/extract-tool-calls message))
      ;; Tool calls
      (let [tool-calls   (provider/extract-tool-calls message)
            _            (doseq [tc tool-calls] (print-tool-call tc))
            tool-results (execute-tool-calls message)
            _            (doseq [r tool-results] (print-tool-result r))]
        [(-> state
             (update :messages conj message)
             (update :messages into tool-results))
         nil])
      ;; Text response
      (let [content (provider/extract-content message)]
        (when content (println content))
        [(update state :messages conj message)
         content]))))

;; ─── Streaming Step ───────────────────────────────────────────────

(defn step-streaming
  "Run one agent step with streaming output.
   Returns [new-state response-text-or-nil]."
  [state]
  (let [config   (:config state)
        messages (:messages state)
        opts     (cond-> {:tools tools/tool-defs}
                   (:model state) (assoc :model (:model state)))]

    (try
      (let [chunks (provider/chat-completions-stream config messages opts)]
        ;; Consume the stream
        (loop [chunks          chunks
               text-buffer     (StringBuilder.)
               tool-call-acc   {}
               finish-reason   nil]

          (if-let [chunk (first chunks)]
            ;; Process this chunk
            (let [delta-text  (provider/stream-delta-text chunk)
                  delta-tools (provider/stream-delta-tool-calls chunk)
                  finish      (provider/stream-finish-reason chunk)]

              ;; Print text tokens as they arrive
              (when delta-text
                (print delta-text)
                (flush))

              (recur (next chunks)
                     (if delta-text (.append text-buffer delta-text) text-buffer)
                     (accumulate-tool-call-deltas tool-call-acc delta-tools)
                     (or finish finish-reason)))

            ;; Stream exhausted
            (let [text-content (when (pos? (.length text-buffer)) (str text-buffer))
                  has-tools?   (seq tool-call-acc)
                  tool-calls   (when has-tools? (finalize-tool-calls tool-call-acc))
                  message      (cond-> {:role "assistant"}
                                 (seq text-content) (assoc :content text-content)
                                 has-tools?         (assoc :tool_calls tool-calls))]

              (if has-tools?
                ;; Tool calls — execute
                (do
                  (doseq [tc tool-calls] (print-tool-call tc))
                  (let [tool-results (execute-tool-calls tool-calls)]
                    (doseq [r tool-results] (print-tool-result r))
                    [(-> state
                         (update :messages conj message)
                         (update :messages into tool-results))
                     nil]))

                ;; No tools — final text response
                (do
                  (println)  ;; newline after streamed text
                  [(update state :messages conj message)
                   text-content]))))))

      ;; Fallback to non-streaming on error
      (catch Exception e
        (log/warn "Streaming failed, falling back to non-streaming:" (.getMessage e))
        (step-fallback state)))))

;; ─── Agent Loop ───────────────────────────────────────────────────

(defn run
  "Run the agent loop until it produces a final text response or hits max rounds.
   Streams text output token-by-token. Returns the final state."
  [state user-input]
  (println)
  (let [state (-> state
                  (update :messages conj {:role "user" :content user-input})
                  (update :messages trim-messages))]
    (loop [state state
           rounds 0]
      (if (>= rounds (:max-rounds state))
        (do (println "\n⚠️  Max rounds reached.")
            state)
        (let [[new-state text] (step-streaming state)]
          (if text
            ;; Final response (already printed via streaming)
            new-state
            ;; Tool calls — continue
            (recur new-state (inc rounds))))))))
