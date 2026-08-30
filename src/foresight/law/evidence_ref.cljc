;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.law.evidence-ref
  "Source-neutral EvidenceRef v1 laws. A reference identifies evidence in its
   authority's namespace; filesystem placement may select a view but is never
   identity.")

(def reference-kinds
  #{:git/object
    :rheos/card
    :rheos/event
    :rheos/workflow
    :clio/event
    :skill/definition
    :skill-graph/node})

(def epistemic-tiers #{:observed :derived :provisional :accepted})
(def freshness-statuses #{:current :stale :unknown :unavailable})
(def resolver-statuses #{:resolved :stale :unavailable :unsupported})

(def required-identity-keys
  {:git/object #{:source-id :revision}
   :rheos/card #{:board-id :card-id}
   :rheos/event #{:board-id :event-id}
   :rheos/workflow #{:workflow-id :workflow-version}
   :clio/event #{:stream-id :event-id}
   :skill/definition #{:skill-id :revision}
   :skill-graph/node #{:graph-id :node-id :revision}})

(def forbidden-identity-keys
  #{:path :path-raw :local-path :file :filename :cwd :worktree})

(defn- error [law-id path message expected actual]
  {:law/id law-id
   :path path
   :message message
   :expected expected
   :actual actual})

(defn- present-value? [value]
  (and (some? value) (not= "" value)))

(defn reference-errors [reference]
  (let [kind (:evidence-ref/kind reference)
        identity (:evidence-ref/identity reference)
        required (get required-identity-keys kind)
        freshness (get-in reference [:evidence-ref/freshness :status])]
    (vec
     (concat
      (when-not (= 1 (:evidence-ref/version reference))
        [(error :evidence-ref/version
                [:evidence-ref/version]
                "EvidenceRef version must be explicit"
                1
                (:evidence-ref/version reference))])
      (when-not (contains? reference-kinds kind)
        [(error :evidence-ref/kind
                [:evidence-ref/kind]
                "EvidenceRef kind must be registered"
                reference-kinds
                kind)])
      (when-not (present-value? (:evidence-ref/authority reference))
        [(error :evidence-ref/authority
                [:evidence-ref/authority]
                "EvidenceRef must name its authority namespace"
                :non-empty
                (:evidence-ref/authority reference))])
      (when-not (map? identity)
        [(error :evidence-ref/identity
                [:evidence-ref/identity]
                "EvidenceRef identity must be a map"
                :map
                identity)])
      (when (map? identity)
        (for [key' required
              :when (not (present-value? (get identity key')))]
          (error :evidence-ref/identity-required
                 [:evidence-ref/identity key']
                 "EvidenceRef identity is missing a required authority-scoped key"
                 :non-empty
                 (get identity key'))))
      (when (map? identity)
        (for [key' (sort (filter forbidden-identity-keys (keys identity)))]
          (error :evidence-ref/local-path-not-identity
                 [:evidence-ref/identity key']
                 "Local filesystem placement cannot define evidence identity"
                 :authority-scoped-identity
                 (get identity key'))))
      (when-not (contains? freshness-statuses freshness)
        [(error :evidence-ref/freshness
                [:evidence-ref/freshness :status]
                "EvidenceRef freshness must be explicit"
                freshness-statuses
                freshness)])
      (when-let [tier (:evidence-ref/epistemic-tier reference)]
        (when-not (contains? epistemic-tiers tier)
          [(error :evidence-ref/epistemic-tier
                  [:evidence-ref/epistemic-tier]
                  "EvidenceRef epistemic tier must preserve the authority vocabulary"
                  epistemic-tiers
                  tier)]))))))

(defn valid-reference? [reference]
  (empty? (reference-errors reference)))

(defn resolver-outcome-errors [outcome]
  (vec
   (concat
    (when-not (= 1 (:evidence-resolver/version outcome))
      [(error :evidence-resolver/version
              [:evidence-resolver/version]
              "Resolver outcome version must be explicit"
              1
              (:evidence-resolver/version outcome))])
    (when-not (contains? resolver-statuses (:evidence-resolver/status outcome))
      [(error :evidence-resolver/status
              [:evidence-resolver/status]
              "Resolver status must not collapse stale or unavailable into success"
              resolver-statuses
              (:evidence-resolver/status outcome))])
    (reference-errors (:evidence-resolver/reference outcome))
    (when (and (= :resolved (:evidence-resolver/status outcome))
               (nil? (:evidence-resolver/value outcome)))
      [(error :evidence-resolver/resolved-has-value
              [:evidence-resolver/value]
              "A resolved outcome must carry the resolved projection"
              :present
              nil)]))))

(defn valid-resolver-outcome? [outcome]
  (empty? (resolver-outcome-errors outcome)))
