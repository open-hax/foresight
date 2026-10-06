;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.bootstrap
  "Pinned direct-child planning and supplied-observation assessment; no I/O.")

(defn plan [_input]
  {:bootstrap/status :unimplemented})

(defn assess [_plan _observations _required-gates]
  {:bootstrap/ready? false :bootstrap/errors []})
