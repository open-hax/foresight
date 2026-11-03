;; SPDX-License-Identifier: GPL-3.0-or-later
(ns research.colab-budget-law)
(def campaign "colab-gemma-2026-10-25")
(def cap 30.0)
(def reserve-per-job 2.0)
(def cleanup-headroom 2.0)
(def max-rate-per-hour 1.0) ; CPU-only pilot. GPU admission is deliberately absent.
(defn finite-nonnegative? [x]
  (and (number? x) (<= 0 x) (< x 1.0e9)))
(defn admission [{:keys [campaign-id spent balance initial-balance rate cleanup-verified telemetry-age-seconds]}]
  (let [numeric? (every? finite-nonnegative? [spent balance initial-balance rate telemetry-age-seconds])
        charged (when numeric? (max spent (- initial-balance balance)))]
    {:admit (boolean (and (= campaign campaign-id) numeric? (true? cleanup-verified)
                         (<= telemetry-age-seconds 30) (<= rate max-rate-per-hour)
                         (>= balance (+ reserve-per-job cleanup-headroom))
                         (<= (+ charged reserve-per-job cleanup-headroom) cap)))
     :charged charged :cap cap :reservation reserve-per-job :headroom cleanup-headroom}))
(defn promotion [{:keys [stealth yoga same-inputs human-approved]}]
  {:promote (boolean (every? true? [stealth yoga same-inputs human-approved]))})
