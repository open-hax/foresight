(ns knoxx-extraction-snippets
  (:require [cljs.test :refer [deftest is run-tests]]
            [cljs.reader :as edn]
            [clojure.string :as str]
            [nbb.core :refer [await]]
            ["fs" :as fs]))

(def root (str (.cwd js/process) "/"))
(defn blocks [path]
  (mapv second (re-seq #"(?s)```clojure\n(.*?)\n```" (.readFileSync fs (str root path) "utf8"))))

;; Evaluate only reviewed repository document examples, not external inputs.
(defn eval-snippet [source]
  (#_{:clj-kondo/ignore [:unresolved-symbol]} load-string source))

(def bracket
  (eval-snippet (str (first (blocks "docs/research/agent-authored-edn-repair-and-expression-whitelist.md"))
                    "\n{:repair repair :diagnose diagnose :format format-diagnostic}")))

(deftest bracket-regressions
  (doseq [[source expected] [["(]" "()"] [")" ""] ["[1 2" "[1 2]"]
                             [(str "[\"text" "\\") (str "[\"text" "\\\\" "\"]")]
                             ["[\\)]" "[\\)]"] ["[\\newline]" "[\\newline]"]
                             ["[\\;]" "[\\;]"] ["[\\\"]" "[\\\"]"]]]
    (let [repaired ((:repair bracket) source)]
      (is (= expected (:text repaired)) source)
      (is (:ok ((:diagnose bracket) (:text repaired))) source)))
  (is (= 1 (:pos (first (:errors ((:diagnose bracket) "(]"))))))
  (is (= 0 (:pos (first (:errors ((:diagnose bracket) ")"))))))
  (is (str/includes? ((:format bracket) ((:diagnose bracket) "(]")) "Mismatched")))

(def migration-blocks (blocks "docs/notes/driver-agnostic-migration-protocol.md"))
(def protocol (eval-snippet (str (second migration-blocks)
                               "\n{:apply apply-migration! :rollback rollback-migration! :applied applied-migrations}")))
(def memory (eval-snippet (str (nth migration-blocks 4) "\n->MemMigrationDriver")))
(def state* (atom {:actor {"a" {:actor/id "a" :actor/kind :agent}
                          "human" {:actor/id "human" :actor/kind :human}}
                   :role {:writer {:role/slug :writer :name "tool.write"}
                          :reader {:role/slug :reader :name "tool.read"}}}))
(def driver (memory state* (atom {})))
(def actor-spec
  {:migration/id "2026-04-27-001-actor-kind-agent-to-system"
   :migration/entity :actor
   :migration/expr '(fn [r] (assoc r :actor/kind :system))
   :migration/predicate '(fn [r] (= :agent (:actor/kind r)))
   :migration/reversible true
   :migration/down-expr '(fn [r] (assoc r :actor/kind :agent))
   :migration/down-predicate '(fn [r] (= :system (:actor/kind r)))})
(def role-spec
  {:migration/id "2026-04-27-004-role-case"
   :migration/entity :role
   :migration/expr '(fn [r] (assoc r :name (clojure.string/replace (:name r) "tool." "cap.")))
   :migration/reversible true
   :migration/down-expr '(fn [r] (assoc r :name (clojure.string/replace (:name r) "cap." "tool.")))
   :migration/down-predicate '(fn [r] (clojure.string/starts-with? (:name r) "cap."))})

;; Await real documented driver operations before asserting their state.
(await ((:apply protocol) driver actor-spec))
(def applied-actors (:actor @state*))
(await ((:rollback protocol) driver actor-spec))
(await ((:apply protocol) driver role-spec))
(def applied-roles (:role @state*))
(await ((:rollback protocol) driver role-spec))
(def missing-down-rejected (atom false))
(await (.catch ((:rollback protocol) driver (dissoc actor-spec :migration/down-expr))
               (fn [_] (reset! missing-down-rejected true))))

(deftest memory-driver-regressions
  (is (= :system (get-in applied-actors ["a" :actor/kind])))
  (is (= :human (get-in applied-actors ["human" :actor/kind])))
  (is (= :agent (get-in @state* [:actor "a" :actor/kind])))
  (is (= :human (get-in @state* [:actor "human" :actor/kind])))
  (is (= #{:writer :reader} (set (keys applied-roles))))
  (is (= "cap.write" (get-in applied-roles [:writer :name])))
  (is (= #{:writer :reader} (set (keys (:role @state*)))))
  (is (= "tool.write" (get-in @state* [:role :writer :name])))
  (is (= "tool.read" (get-in @state* [:role :reader :name])))
  (is @missing-down-rejected))

(def pg-code (nth migration-blocks 3))
(def pg-rows [{:id "2026-04-27-001-actor-kind" :version 1 :applied_at "2026-10-06T00:00:00Z" :status "ok"}])
(eval-snippet "(ns knoxx.backend.db.pg) (defn query! [& _] (js/Promise.resolve nil)) (defn hq! [& _] (js/Promise.resolve {:rows [{:id \"2026-04-27-001-actor-kind\" :version 1 :applied_at \"2026-10-06T00:00:00Z\" :status \"ok\"}]}))")
;; The PG example targets compiled CLJS js-await; evaluate just its actual log
;; methods in a namespace with stubbed query effects, preserving mapping logic.
(def pg-prefix (subs pg-code 0 (.indexOf pg-code "(defrecord")))
(def pg-log-method
  (subs pg-code (.indexOf pg-code "  (applied-migrations") (.indexOf pg-code "  (apply-migration!")))
(def pg-read
  (eval-snippet (str "(ns knoxx.backend.db.pg-migration-driver (:require [knoxx.backend.db.pg :as pg] [clojure.string :as str] [sci.core :as sci]))\n"
                    (subs pg-prefix (.indexOf pg-prefix "(def ^:private entity->table"))
                    (str/replace pg-log-method "(applied-migrations [_]" "(defn read-log [pool]")
                    "\n{:read read-log :tables entity->table :eval safe-eval}")))
(def applied-log (await ((:read pg-read) nil)))
(deftest postgres-log-and-context-regressions
  (is (= "2026-04-27-001-actor-kind" (:migration/id (first applied-log))))
  (is (= :ok (:migration/status (first applied-log))))
  (is (= :capabilities (get (:tables pg-read) :capability)))
  (is ((:eval pg-read) '(clojure.string/starts-with? "tool.read" "tool."))))

(def examples (edn/read-string (str "[" (.readFileSync fs (str root "docs/architecture/epistemic-examples.edn") "utf8") "]")))
(deftest epistemic-reference-regressions
  (let [obs (first (filter #(= :obs (:kind %)) examples))
        attestation (first (filter #(= :attestation (:kind %)) examples))
        inference (first (filter #(= :inference (:kind %)) examples))]
    (is (uuid? (:causedby attestation)))
    (is (= (:id obs) (:causedby attestation)))
    (is (= (:id obs) (:ref (first (:from inference)))))
    (is (= #{:kind :ref} (set (keys (first (:from inference))))))))

(let [result (run-tests 'knoxx-extraction-snippets)]
  (when (pos? (+ (:fail result) (:error result))) (js/process.exit 1)))
