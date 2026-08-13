(ns eta.tui
  "Simple terminal UI using JLine3 for readline support."
  (:require [clojure.string :as str])
  (:import [org.jline.reader LineReaderBuilder LineReader]
           [org.jline.terminal TerminalBuilder]
           [org.jline.utils AttributedStringBuilder AttributedStyle]))

;; ─── Terminal Setup ───────────────────────────────────────────────

(defn- build-terminal []
  (.build (doto (TerminalBuilder/builder)
            (.system true)
            (.jna true))))

(defn- build-reader [^org.jline.terminal.Terminal terminal]
  (.build (doto (LineReaderBuilder/builder)
            (.terminal terminal))))

;; ─── Prompt Rendering ────────────────────────────────────────────

(defn- eta-prompt []
  (let [sb (AttributedStringBuilder.)]
    (-> sb
        (.style (.foreground AttributedStyle/DEFAULT 36))  ;; cyan
        (.append "η> "))
    (.toAnsi sb)))

(defn- eta-continuation-prompt []
  "  ")

;; ─── Input Reading ───────────────────────────────────────────────

(defn read-input
  "Read multi-line user input. Empty line sends; /exit or /quit to exit.
   Returns the trimmed input string, or :exit."
  [^LineReader reader]
  (loop [lines []]
    (let [line (.readLine reader 
                         (if (empty? lines) 
                           (eta-prompt) 
                           (eta-continuation-prompt)))]
      (cond
        (nil? line)               :exit
        (= "/exit" (str/trim line)) :exit
        (= "/quit" (str/trim line)) :exit
        (and (empty? lines) (str/blank? line)) (recur lines)
        (str/blank? line)         (str/join "\n" lines)
        :else                     (recur (conj lines line))))))

;; ─── Banner ──────────────────────────────────────────────────────

(defn print-banner [model-name]
  (println)
  (println "╔══════════════════════════════════════════════╗")
  (println "║          η — Clojure TUI Agent Harness       ║")
  (println "╠══════════════════════════════════════════════╣")
  (println (str "║  Model: " (format "%-36s" model-name) "║"))
  (println "║  /exit or /quit to quit                      ║")
  (println "║  Tools: read · write · bash · nrepl          ║")
  (println "╚══════════════════════════════════════════════╝")
  (println))

;; ─── REPL Loop ───────────────────────────────────────────────────

(defn repl-loop
  "Run the interactive REPL loop.
   agent-fn is (fn [state user-input]) -> new-state"
  [initial-state agent-fn]
  (let [terminal (build-terminal)
        reader   (build-reader terminal)
        model    (or (get-in initial-state [:config :model]) "default")]
    (print-banner model)
    ;; Handle Ctrl+C gracefully
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn [] (println "\nBye!"))))
    (loop [state initial-state]
      (let [input (try
                    (read-input reader)
                    (catch org.jline.reader.UserInterruptException _
                      (println "\n[Interrupted]")
                      nil)
                    (catch org.jline.reader.EndOfFileException _
                      :exit))]
        (cond
          (nil? input)   (recur state)
          (= input :exit) (do (println "Bye!") nil)
          (str/blank? input) (recur state)
          :else
          (let [new-state (try
                            (agent-fn state input)
                            (catch Exception e
                              (println (str "\n⚠️  Error: " (.getMessage e)))
                              state))]
            (recur (or new-state state))))))))
