;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.shape.evidence-ref
  "Pure constructors and resolver outcomes for EvidenceRef v1."
  (:require [foresight.law.evidence-ref :as law]))

(defn reference
  [kind authority identity & {:keys [selector integrity epistemic-tier freshness]
                              :or {freshness {:status :unknown}}}]
  (let [candidate (cond-> {:evidence-ref/version 1
                           :evidence-ref/kind kind
                           :evidence-ref/authority authority
                           :evidence-ref/identity identity
                           :evidence-ref/freshness freshness}
                    selector (assoc :evidence-ref/selector selector)
                    integrity (assoc :evidence-ref/integrity integrity)
                    epistemic-tier (assoc :evidence-ref/epistemic-tier epistemic-tier))
        errors (law/reference-errors candidate)]
    (if (empty? errors)
      candidate
      (throw (ex-info "Invalid EvidenceRef" {:errors errors})))))

(defn git-object
  [{:keys [authority repository revision resource-id blob-oid path heading-path
           section-ordinal extractor-version epistemic-tier]}]
  (reference :git/object authority
             (cond-> {:source-id (or resource-id repository)
                      :revision revision}
               repository (assoc :repository repository)
               resource-id (assoc :resource-id resource-id)
               blob-oid (assoc :blob-oid blob-oid))
             :selector (cond-> {}
                         path (assoc :path path)
                         heading-path (assoc :heading-path heading-path)
                         (some? section-ordinal) (assoc :section-ordinal section-ordinal)
                         extractor-version (assoc :projection-version extractor-version))
             :epistemic-tier epistemic-tier))

(defn rheos-card [authority board-id card-id]
  (reference :rheos/card authority {:board-id board-id :card-id card-id}))

(defn rheos-event [authority board-id event-id]
  (reference :rheos/event authority {:board-id board-id :event-id event-id}))

(defn rheos-workflow [authority workflow-id workflow-version]
  (reference :rheos/workflow authority
             {:workflow-id workflow-id :workflow-version workflow-version}))

(defn clio-event [authority stream-id event-id]
  (reference :clio/event authority {:stream-id stream-id :event-id event-id}))

(defn skill [authority skill-id revision]
  (reference :skill/definition authority {:skill-id skill-id :revision revision}))

(defn skill-graph-node [authority graph-id node-id revision]
  (reference :skill-graph/node authority
             {:graph-id graph-id :node-id node-id :revision revision}))

(defn resolver-outcome
  [status reference' & {:keys [value diagnostic observed-freshness]}]
  (let [outcome (cond-> {:evidence-resolver/version 1
                         :evidence-resolver/status status
                         :evidence-resolver/reference reference'}
                  (some? value) (assoc :evidence-resolver/value value)
                  diagnostic (assoc :evidence-resolver/diagnostic diagnostic)
                  observed-freshness
                  (assoc :evidence-resolver/observed-freshness observed-freshness))
        errors (law/resolver-outcome-errors outcome)]
    (if (empty? errors)
      outcome
      (throw (ex-info "Invalid EvidenceRef resolver outcome" {:errors errors})))))
