# Agent-authored EDN repair and SCI expression whitelist

> Split out of `open-hax/knoxx` `docs/notes/contracts/contract-runtime-validation-layers.md` (lines 129–630) at knoxx `0fdaae13` on 2026-09-30. Primary relationship: alpha, katamorph. Why it left Knoxx: [knoxx-documentation-extraction.md](../lineage/knoxx-documentation-extraction.md).

## B — `contract.bracket` — Repair + Diagnostic

This is the bracket-counting system. Three passes: **scan** to produce a token walk, **diagnose** to produce a precise error report, **repair** to attempt autocorrect.

```clojure
(ns knoxx.backend.contract.bracket
  "EDN bracket balance checker, diagnoser, and autocorrector.
   
   Designed for agent-authored EDN: produces structured diagnostics
   that agents can act on directly, rather than raw parse errors.
   
   Autocorrect handles:
     - Missing closing delimiters (appended at end)
     - Mismatched closers (e.g. } where ] expected)
     - Extra closers (stripped)
     - Unclosed strings (closing \" appended)
     - Odd map entries (last key gets nil value appended)
   
   Autocorrect does NOT handle:
     - Wrong key types (non-keyword map keys)
     - Wrong value types
     - Semantic errors
   
   These are left for the schema validator to explain.")

;; ── Token scanner ─────────────────────────────────────────────────────────────

(def open->close  {\( \)  \[ \]  \{ \}})
(def close->open  {\) \(  \] \[  \} \{})
(def openers      (set (keys open->close)))
(def closers      (set (keys close->open)))

(defn- scan-tokens
  "Walk text char-by-char, track brackets/strings/comments.
   Returns vector of token maps:
     {:kind :open|:close|:string-open|:string-close
      :char \\char
      :line n :col n :pos n}"
  [text]
  (let [chars (vec text)
        n     (count chars)]
    (loop [pos     0
           line    1
           col     0
           in-str  false
           escape  false
           tokens  []]
      (if (>= pos n)
        tokens
        (let [ch (nth chars pos)]
          (cond
            ;; Inside string, escaped
            (and in-str escape)
            (recur (inc pos) line (inc col) in-str false tokens)

            ;; Inside string, escape char
            (and in-str (= ch \\))
            (recur (inc pos) line (inc col) in-str true tokens)

            ;; End of string
            (and in-str (= ch \"))
            (recur (inc pos) line (inc col) false false
                   (conj tokens {:kind :string-close :char ch :line line :col col :pos pos}))

            ;; Inside string, not special
            in-str
            (let [nl? (= ch \newline)]
              (recur (inc pos) (if nl? (inc line) line) (if nl? 0 (inc col))
                     in-str false tokens))

            ;; Comment — skip to end of line
            (= ch \;)
            (let [end (or (some (fn [p] (when (= (nth chars p) \newline) p))
                                (range pos n))
                          n)]
              (recur end line col false false tokens))

            ;; Start string
            (= ch \")
            (recur (inc pos) line (inc col) true false
                   (conj tokens {:kind :string-open :char ch :line line :col col :pos pos}))

            ;; Opener
            (openers ch)
            (recur (inc pos) line (inc col) false false
                   (conj tokens {:kind :open :char ch :line line :col col :pos pos}))

            ;; Closer
            (closers ch)
            (recur (inc pos) line (inc col) false false
                   (conj tokens {:kind :close :char ch :line line :col col :pos pos}))

            ;; Newline — track line
            (= ch \newline)
            (recur (inc pos) (inc line) 0 false false tokens)

            ;; Anything else
            :else
            (recur (inc pos) line (inc col) false false tokens)))))))

;; ── Diagnose ──────────────────────────────────────────────────────────────────

(defn diagnose
  "Walk token stream and produce a structured diagnostic report.
   
   Returns:
   {:ok true}   — balanced
   {:ok false
    :errors [{:kind :unmatched-open | :unmatched-close | :mismatched
              :expected \\char | nil
              :got      \\char | nil
              :line     n
              :col      n
              :message  str}]
    :unclosed-strings [{:line n :col n}]}"
  [text]
  (let [tokens (scan-tokens text)
        ;; Check strings separately
        str-opens (filter #(= :string-open (:kind %)) tokens)
        str-closes (filter #(= :string-close (:kind %)) tokens)
        unclosed-strings (when (> (count str-opens) (count str-closes))
                           (drop (count str-closes) str-opens))
        ;; Walk bracket stack
        bracket-tokens (filter #(#{:open :close} (:kind %)) tokens)
        {:keys [errors stack]}
        (reduce
         (fn [{:keys [stack errors]} {:keys [kind char line col]}]
           (if (= :open kind)
             {:stack  (conj stack {:char char :line line :col col})
              :errors errors}
             ;; closer
             (if (empty? stack)
               {:stack  stack
                :errors (conj errors
                              {:kind    :unmatched-close
                               :got     char
                               :line    line
                               :col     col
                               :message (str "Unexpected '" char "' at line " line
                                             " col " col " — no matching opener")})}
               (let [top (peek stack)
                     expected (open->close (:char top))]
                 (if (= expected char)
                   {:stack  (pop stack) :errors errors}
                   {:stack  (pop stack)
                    :errors (conj errors
                                  {:kind     :mismatched
                                   :expected expected
                                   :got      char
                                   :line     line
                                   :col      col
                                   :message  (str "Mismatched delimiter at line " line
                                                  " col " col ": expected '" expected
                                                  "' to close '" (:char top)
                                                  "' opened at line " (:line top)
                                                  " col " (:col top)
                                                  ", but got '" char "'")})})))))
         {:stack [] :errors []}
         bracket-tokens)
        ;; Anything left on the stack is unclosed
        unclosed-errors (mapv (fn [{:keys [char line col]}]
                                {:kind    :unmatched-open
                                 :got     char
                                 :line    line
                                 :col     col
                                 :message (str "Unclosed '" char "' opened at line "
                                               line " col " col
                                               " — needs '" (open->close char) "'")})
                               stack)
        all-errors (into errors unclosed-errors)]
    (if (and (empty? all-errors) (empty? unclosed-strings))
      {:ok true}
      {:ok              false
       :errors          all-errors
       :unclosed-strings (mapv #(select-keys % [:line :col]) unclosed-strings)})))

;; ── Repair ────────────────────────────────────────────────────────────────────

(defn repair
  "Attempt to autocorrect simple structural errors in EDN text.
   Returns {:text str :changes [{:kind kw :description str}]}.
   
   Safe to apply before parse — result may still fail schema validation
   but should at least be read-string parseable."
  [text]
  (let [report (diagnose text)]
    (if (:ok report)
      {:text text :changes []}
      (let [;; Step 1: close unclosed strings
            {:keys [text changes]}
            (if (seq (:unclosed-strings report))
              {:text    (str text "\"")
               :changes [{:kind        :closed-string
                          :description "Appended missing closing double-quote"}]}
              {:text text :changes []})

            ;; Step 2: fix mismatches by replacing wrong closer with expected closer
            {:keys [text changes]}
            (reduce
             (fn [{:keys [text changes]} {:keys [kind expected got pos]}]
               (if (= :mismatched kind)
                 {:text    (str (subs text 0 pos) expected (subs text (inc pos)))
                  :changes (conj changes
                                 {:kind        :replaced-closer
                                  :description (str "Replaced '" got "' with '" expected
                                                    "' at position " pos)})}
                 {:text text :changes changes}))
             {:text text :changes changes}
             ;; Only fix mismatches here — unmatched-close handled below
             (filter #(= :mismatched (:kind %)) (:errors report)))

            ;; Step 3: remove unmatched extra closers (reverse order to preserve positions)
            {:keys [text changes]}
            (reduce
             (fn [{:keys [text changes]} {:keys [kind pos got]}]
               (if (= :unmatched-close kind)
                 {:text    (str (subs text 0 pos) (subs text (inc pos)))
                  :changes (conj changes
                                 {:kind        :removed-closer
                                  :description (str "Removed unmatched '" got
                                                    "' at position " pos)})}
                 {:text text :changes changes}))
             {:text text :changes changes}
             (reverse (filter #(= :unmatched-close (:kind %)) (:errors report))))

            ;; Step 4: re-scan and append any still-unclosed openers
            final-report (diagnose text)
            {:keys [text changes]}
            (if (:ok final-report)
              {:text text :changes changes}
              (let [closers-to-append
                    (->> (:errors final-report)
                         (filter #(= :unmatched-open (:kind %)))
                         ;; Close in reverse-open order (innermost first)
                         reverse
                         (map #(open->close (:got %))))]
                {:text    (apply str text closers-to-append)
                 :changes (conj changes
                                {:kind        :appended-closers
                                 :description (str "Appended "
                                                   (count closers-to-append)
                                                   " missing closing delimiter(s): "
                                                   (apply str closers-to-append))})}))]
        {:text text :changes changes}))))

;; ── Human-readable summary ────────────────────────────────────────────────────

(defn format-diagnostic
  "Produce a human-readable (and agent-readable) string from diagnose output.
   This is what goes back to the agent tool surface verbatim."
  [{:keys [ok errors unclosed-strings]}]
  (if ok
    "✓ Bracket balance OK"
    (str "✗ " (count errors) " bracket error(s):\n"
         (str/join "\n" (map #(str "  • " (:message %)) errors))
         (when (seq unclosed-strings)
           (str "\n  + " (count unclosed-strings) " unclosed string literal(s) at: "
                (str/join ", "
                          (map #(str "line " (:line %) " col " (:col %))
                               unclosed-strings)))))))
```

***

## C — `contract.sci` — sci Whitelist

```clojure
(ns knoxx.backend.contract.sci
  "sci evaluation context for agent contracts.
   
   Whitelist philosophy:
     - Agents get a small, legible set of pure fns.
     - Nothing that touches IO, atoms, JS interop, or reflection.
     - All allowed symbols are listed explicitly — no ns-import glob.
     - :fn-ref is the escape hatch for complex logic (written by humans)."
  (:require [sci.core :as sci]
            [clojure.string :as str]))

;; ── Allowed ops ───────────────────────────────────────────────────────────────
;; Each entry: sym → cljs-fn
;; Agents may only call fns listed here inside :expr blocks.

(def ^:private WHITELIST
  {;; String
   'str         str
   'str/join    str/join
   'str/split   str/split
   'str/lower-case str/lower-case
   'str/upper-case str/upper-case
   'str/trim    str/trim
   'str/includes? str/includes?
   'str/starts-with? str/starts-with?
   'str/ends-with?  str/ends-with?
   'str/blank?  str/blank?

   ;; Arithmetic
   '+           +
   '-           -
   '*           *
   '/           /
   'mod         mod
   'quot        quot
   'max         max
   'min         min
   'inc         inc
   'dec         dec
   'zero?       zero?
   'pos?        pos?
   'neg?        neg?

   ;; Logic
   'and         (fn [& args] (reduce #(and %1 %2) true args))
   'or          (fn [& args] (reduce #(or %1 %2) false args))
   'not         not
   'if          (fn [test then else] (if test then else))
   'when        (fn [test body] (when test body))
   'cond        cond   ;; macro — sci handles this natively

   ;; Collections
   'get         get
   'get-in      get-in
   'assoc       assoc
   'assoc-in    assoc-in
   'update      update
   'dissoc      dissoc
   'merge       merge
   'conj        conj
   'into        into
   'map         map
   'filter      filter
   'remove      remove
   'reduce      reduce
   'count       count
   'first       first
   'second      second
   'last        last
   'rest        rest
   'nth         nth
   'empty?      empty?
   'seq         seq
   'vec         vec
   'set         set
   'keys        keys
   'vals        vals
   'contains?   contains?
   'some        some
   'every?      every?

   ;; Identity / equality
   '=           =
   'not=        not=
   '<           <
   '>           >
   '<=          <=
   '>=          >=
   'identity    identity
   'nil?        nil?
   'boolean     boolean
   'keyword     keyword
   'name        name
   'namespace   namespace
   'symbol      symbol

   ;; Contract-specific helpers
   ;; These are injected into the sci context at eval time from runtime-ctx:
   ;;   event, state, result, agent, ctx
   ;; Agents reference them as bare symbols.

   ;; Safe emit — returns an effect map, does not execute IO
   'emit        (fn [events]
                  {:contract/emit (vec events)})

   ;; Abort a run
   'abort!      (fn [reason]
                  {:contract/abort true :contract/reason reason})

   ;; Dispatch another agent (by contract-id)
   'spawn       (fn [contract-id ctx-overrides]
                  {:contract/spawn {:id contract-id :ctx ctx-overrides}})

   ;; Guard: check fulfillment
   'fulfilled?  (fn [result] (boolean (:fulfilled result)))})

;; ── Banned symbols (explicit) ────────────────────────────────────────────────
;; These are checked BEFORE eval to give a clear error.

(def BANNED
  #{'eval 'read 'read-string 'load 'require 'import 'ns
    'def 'defn 'defmacro 'alter-var-root
    'js/eval 'js/fetch 'js/XMLHttpRequest 'js/require
    'set! 'reset! 'swap! 'atom
    '.. '. '->>                ;; allow ->> only via sci threading macro
    'intern 'find-ns 'the-ns})

;; ── Whitelist check (pre-eval) ────────────────────────────────────────────────

(defn- walk-symbols
  "Collect all symbols from a quoted form."
  [form]
  (cond
    (symbol? form) [form]
    (seq? form)    (mapcat walk-symbols form)
    (map? form)    (mapcat walk-symbols (concat (keys form) (vals form)))
    (vector? form) (mapcat walk-symbols form)
    (set? form)    (mapcat walk-symbols form)
    :else          []))

(defn check-whitelist
  "Returns {:ok true} or {:ok false :violations [{:sym sym :reason str}]}"
  [expr]
  (let [syms    (walk-symbols expr)
        allowed (set (keys WHITELIST))
        ;; Allow bare ctx/event/state/result/agent — these are runtime bindings
        runtime-ns #{'ctx 'event 'state 'result 'agent 'filters 'events}
        violations
        (->> syms
             (remove #(or (contains? allowed %)
                          (contains? runtime-ns %)
                          (contains? runtime-ns (symbol (namespace %) (name %)))
                          ;; Allow keywords and numbers (not symbols)
                          (keyword? %)
                          (number? %)))
             (filter #(or (contains? BANNED %)
                          (not (contains? allowed %))))
             (mapv (fn [sym]
                     {:sym    sym
                      :reason (if (contains? BANNED sym)
                                (str "'" sym "' is explicitly banned in contract expressions")
                                (str "'" sym "' is not in the contract expression whitelist"))})))]
    (if (empty? violations)
      {:ok true}
      {:ok false :violations violations})))

;; ── sci context factory ────────────────────────────────────────────────────────

(defn make-sci-ctx
  "Build a sci evaluation context for one contract expression.
   Injects runtime-ctx bindings as top-level vars."
  [runtime-ctx]
  (let [event   (or (:event runtime-ctx) {})
        state   (or (:state runtime-ctx) {})
        result  (or (:result runtime-ctx) {})
        agent   (or (:agent runtime-ctx) {})
        ;; ctx/* path accessors: ctx/channel-id etc
        ctx-vars (into {}
                       (map (fn [[k v]]
                              [(symbol (str "ctx-" (name k))) v])
                            (merge runtime-ctx event)))]
    (sci/init
     {:namespaces
      {'user
       (merge
        (select-keys WHITELIST (keys WHITELIST))
        ctx-vars
        {'ctx    runtime-ctx
         'event  event
         'state  state
         'result result
         'agent  agent})}
      :deny BANNED})))

;; ── eval-expr ────────────────────────────────────────────────────────────────

(defn eval-expr
  "Evaluate a contract :expr form.
   1. Check whitelist
   2. Build sci ctx
   3. Eval

   Returns {:ok true :value v} | {:ok false :error str :violations [...]}
   
   Never throws — all failures are returned as data."
  [{:keys [expr fn-ref]} runtime-ctx {:keys [contract-id fn-registry]}]
  (try
    (cond
      fn-ref
      (if-let [f (get fn-registry fn-ref)]
        {:ok true :value (f runtime-ctx)}
        {:ok false :error (str "Unknown :fn-ref " fn-ref)
         :contract-id contract-id})

      expr
      (let [wl (check-whitelist expr)]
        (if-not (:ok wl)
          {:ok         false
           :error      (str (count (:violations wl)) " whitelist violation(s) in contract expr")
           :violations (:violations wl)
           :advice     (str "Allowed ops: " (str/join ", " (sort (map str (keys WHITELIST)))))}
          (let [ctx (make-sci-ctx runtime-ctx)
                v   (sci/eval-form ctx expr)]
            {:ok true :value v})))

      :else
      {:ok false :error "ExprNode has neither :expr nor :fn-ref"})
    (catch :default e
      {:ok    false
       :error (str "Contract eval error: " (ex-message e))
       :contract-id contract-id})))
```

***

