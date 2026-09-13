;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.infra.local
  "Small compiled Node boundary proving the root consumes canonical Clio."
  (:require [clio.infra.ledger :as ledger]
            [clio.infra.schema-store :as schema-store]))

(defn ledger-summary
  "Validate a complete ledger union against its persisted schema revisions before
   returning a JavaScript summary. Missing files and corrupt histories throw."
  [schema-directory ledger-files]
  (let [history (ledger/canonicalize-files
                (schema-store/load-revisions schema-directory)
                (vec (js->clj ledger-files)))
        events (:canonical/events history)]
    (clj->js {:events (count events)
              :streams (vec (sort (set (map :event/stream events))))})))
