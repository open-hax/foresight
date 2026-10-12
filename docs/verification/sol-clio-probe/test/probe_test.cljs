(ns probe-test
  (:require [cljs.test :refer [deftest is run-tests]]
            [clio.domain.canonicalize :as canonicalize]
            [clio.domain.schema :as schema]
            [clio.extern.js.fs :as fs]
            [clio.extern.js.runtime :as host]
            [clio.infra.ledger :as ledger]
            [clio.infra.runtime :as runtime]
            [clio.law.schema :as law]))

(def catalog
  {:sol.probe/started
   (law/event-schema :sol.probe/started
                     [:map [:run/id :string] [:payload :map]])})

(def legacy-envelope
  {:envelope/version 1 :event/id "event-1" :event/type "sol.run.started"
   :event/time "2026-10-03T00:00:00.000Z"
   :event/from {:actor-id "agent:probe" :actor-kind "agent"}
   :causal/root "event-1" :session/id "session-1" :run/id "run-1"
   :episode/id "episode-1" :turn/id "turn-1" :delivery/mode "stream"
   :payload {:status "running"}})

(defn error-code [f]
  (try (f) nil (catch :default error (:clio/error (ex-data error)))))

(deftest real-clio-append-read-retry-reopen
  (let [directory (str ".ημ/clio-operation-" (host/random-uuid))
        schema-dir (str directory "/schemas")
        ledger-file (str directory "/events.edn")]
    (try
      (fs/ensure-dir! directory)
      (ledger/create-ledger! ledger-file)
      (let [rt (runtime/open schema-dir catalog)
            first-write (runtime/append!
                         rt ledger-file :sol.probe/started
                         {:event/stream "episode:probe" :event/seq 1
                          :event/actor "agent:probe" :event/subject "run-1"
                          :event/data {:run/id "run-1" :payload {:status "running"}}})
            first-event (:event first-write)
            second-write (runtime/append!
                          rt ledger-file :sol.probe/started
                          {:event/stream "episode:probe" :event/seq 2
                           :event/causes [(:event/id first-event)]
                           :event/actor "agent:probe" :event/subject "run-1"
                           :event/data {:run/id "run-1" :payload {:status "completed"}}})
            second-event (:event second-write)
            reopened (runtime/open schema-dir catalog)
            revisions (:schema/revisions reopened)
            canonical (ledger/canonicalize-files revisions [ledger-file])]
        (is (= :appended (:append/result first-write)))
        (is (= :appended (:append/result second-write)))
        (is (= [first-event second-event] (ledger/read-ledger ledger-file)))
        (is (= :already-present (ledger/append-event! revisions ledger-file second-event)))
        (is (= 2 (count (ledger/read-ledger ledger-file))))
        (is (= (:schema/root (:schema/current rt))
               (:schema/root (:schema/current reopened))))
        (is (= [(:event/id first-event) (:event/id second-event)]
               (:canonical/event-ids canonical)))
        (is (= (:canonical/event-ids canonical)
               (:canonical/event-ids
                (canonicalize/canonicalize revisions [[second-event] [first-event second-event]]))))
        (is (= :clio.ledger/id-collision
               (error-code #(ledger/append-event! revisions ledger-file
                                                 (assoc-in second-event [:event/data :payload] {})))))
        (is (= :clio.ledger/concurrent-stream-write
               (error-code #(ledger/append-event! revisions ledger-file
                                                 (assoc second-event :event/id (host/random-uuid))))))
        (is (= :clio.ledger/missing-file
               (error-code #(ledger/append-event! revisions (str directory "/missing.edn") first-event))))
        (is (= :clio.schema/invalid-bootstrap
               (error-code #(schema/validate-event! revisions legacy-envelope))))
        (is (= :clio.canonicalize/stream-gap
               (error-code #(canonicalize/canonicalize revisions [[second-event]])))))
      (finally (fs/remove-tree! directory)))))

(defn main [] (run-tests 'probe-test))
