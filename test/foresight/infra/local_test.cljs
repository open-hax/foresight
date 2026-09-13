;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.infra.local-test
  (:require [cljs.test :refer [deftest is]]
            [clio.infra.ledger :as ledger]
            [clio.infra.runtime :as runtime]
            [clio.law.schema :as schema]
            [foresight.infra.local :as local]
            ["fs" :as fs]
            ["os" :as os]
            ["path" :as path]))

(deftest root-consumes-persisted-clio-and-refuses-corruption
  (let [directory (fs/mkdtempSync (path/join (os/tmpdir) "foresight-clio-"))
        schemas (path/join directory "schemas")
        file (path/join directory "events.edn")]
    (try
      (ledger/create-ledger! file)
      (let [rt (runtime/open schemas {:draft/created (schema/event-schema :draft/created [:map [:title :string]])})]
        (runtime/append! rt file :draft/created
                         {:event/stream "draft:one" :event/seq 1 :event/actor "user:writer"
                          :event/subject "draft:one" :event/data {:title "A local draft"}})
        (is (= {:events 1 :streams ["draft:one"]}
               (js->clj (local/ledger-summary schemas #js [file]) :keywordize-keys true)))
        (fs/appendFileSync file "{:malformed true}\n")
        (is (thrown? js/Error (local/ledger-summary schemas #js [file])))
        (is (thrown? js/Error (local/ledger-summary schemas #js [(path/join directory "missing.edn")]))))
      (finally (fs/rmSync directory #js {:recursive true :force true})))))
