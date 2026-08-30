;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns lineage-test
  (:require [cljs.test :as test :refer [deftest is testing]]
            [foresight.law.lineage :as law]
            [foresight.lineage :as lineage]))

(def expected-claim-statuses
  {:promethean/owner-sovereignty :live
   :promethean/intent-compiler :live-with-revision
   :promethean/learn-once :live-with-revision
   :promethean/context-field :recurring-pressure
   :promethean/modular-intent :live
   :promethean/eidolon-physics :rejected-implementation})

(defn validation-error-ids [sources]
  (set (map :law/id (:errors (law/validate-inventory sources)))))

(defn update-first-evidence [source f & args]
  (apply update-in source [:source/evidence 0] f args))

(deftest declares-promethean-as-a-pinned-ancestral-prototype
  (let [source (lineage/source-by-id :promethean)
        claims (:source/claims source)]
    (is (= 1 (count lineage/sources)))
    (is (= 6 (count claims)))
    (is (= :historical-prototype (:source/type source)))
    (is (= :ancestral-prototype (:source/role source)))
    (is (= "octave-commons/promethean" (:source/repository source)))
    (is (= lineage/promethean-revision (:source/revision source)))
    (is (law/full-commit-sha? (:source/revision source)))
    (is (false? (:source/actionable? source)))
    (is (= :none (:source/workspace-authority source)))
    (is (= :none (:source/execution-authority source)))
    (is (not (contains? source :source/path)))
    (is (= :proposed/ancestral-prototype
           (:source/semantic-status source)))
    (is (= :provisional (:source/epistemic-status source)))
    (is (= expected-claim-statuses
           (into {}
                 (map (juxt :claim/id :claim/status))
                 claims)))))

(deftest carries-the-recovered-promethean-claim-details
  (is (= [:eta-mu/autonomy :epiphany/explicit-human-promotion]
         (:claim/carried-by
          (lineage/claim-by-id :promethean/owner-sovereignty))))
  (is (= "Agents compile candidate plans; inference does not become accepted intent without an explicit authority boundary."
         (:claim/revision
          (lineage/claim-by-id :promethean/intent-compiler))))
  (is (= "Preserve solutions, failures, context, contracts, and evidence; reuse only when current conditions satisfy the recorded contract."
         (:claim/revision
          (lineage/claim-by-id :promethean/learn-once))))
  (is (= [:openplanner/semantic-graph :openplanner/epistemic-kernel]
         (:claim/carried-by
          (lineage/claim-by-id :promethean/context-field))))
  (is (= [:eta-mu/skill-registry
          :foresight/independent-capability-constellation]
         (:claim/carried-by
          (lineage/claim-by-id :promethean/modular-intent))))
  (is (= "Context must influence routing; no particular physics simulation is law."
         (:claim/surviving-proposition
          (lineage/claim-by-id :promethean/eidolon-physics)))))

(deftest declared-lineage-inventory-is-lawful
  (let [result (law/validate-inventory lineage/sources)]
    (is (:valid? result) (pr-str (:errors result)))
    (is (= 1 (:source/count result)))
    (is (= 6 (:claim/count result)))
    (is (empty? (:errors result)))))

(deftest recovered-claims-remain-provisional-interpretations
  (doseq [claim (mapcat :source/claims lineage/sources)]
    (testing (str (:claim/id claim))
      (is (= :provisional (:claim/epistemic-status claim)))
      (is (= :interpretation (:claim/recovery claim))))))

(deftest historical-evidence-cannot-forge-workspace-authority
  (let [changed [(-> (first lineage/sources)
                     (assoc :source/actionable? true)
                     (assoc :source/path "promethean")
                     (assoc :source/workspace-authority :checkout)
                     (assoc :source/execution-authority :allowed))]
        result (law/validate-inventory changed)
        error-ids (set (map :law/id (:errors result)))]
    (is (false? (:valid? result)))
    (is (every?
         #(contains? error-ids %)
         [:foresight/lineage-source-non-actionable
          :foresight/lineage-source-no-workspace-path
          :foresight/lineage-source-no-workspace-authority
          :foresight/lineage-source-no-execution-authority]))))

(deftest lineage-revisions-must-be-full-commit-identities
  (let [changed [(assoc (first lineage/sources)
                        :source/revision "main")]
        result (law/validate-inventory changed)]
    (is (false? (:valid? result)))
    (is (some #(= :foresight/lineage-source-revision (:law/id %))
              (:errors result)))))

(deftest continuity-status-requirements-are-explicit
  (let [claims (:source/claims (first lineage/sources))
        without-revision
        [(assoc (first lineage/sources)
                :source/claims
                (mapv #(if (= :promethean/intent-compiler (:claim/id %))
                         (dissoc % :claim/revision)
                         %)
                      claims))]
        without-surviving-proposition
        [(assoc (first lineage/sources)
                :source/claims
                (mapv #(if (= :promethean/eidolon-physics (:claim/id %))
                         (dissoc % :claim/surviving-proposition)
                         %)
                      claims))]
        revision-errors (:errors (law/validate-inventory without-revision))
        proposition-errors
        (:errors (law/validate-inventory without-surviving-proposition))]
    (is (some #(= :foresight/lineage-claim-revision (:law/id %))
              revision-errors))
    (is (some #(= :foresight/lineage-claim-surviving-proposition
                  (:law/id %))
              proposition-errors))))

(deftest evidence-paths-are-confined-to-the-pinned-repository
  (doseq [invalid-path ["/README.md"
                        "\\README.md"
                        "C:\\README.md"
                        "../README.md"
                        "docs/../README.md"
                        "./README.md"
                        "docs//README.md"
                        "docs/"]]
    (testing invalid-path
      (let [changed [(update-first-evidence
                      (first lineage/sources)
                      assoc :evidence/path invalid-path)]
            error-ids (validation-error-ids changed)]
        (is (contains? error-ids
                       :foresight/lineage-evidence-path-confined))))))

(deftest evidence-records-remain-present-typed-and-revision-bound
  (let [source (first lineage/sources)
        without-evidence [(assoc source :source/evidence [])]
        wrong-kind [(update-first-evidence
                     source assoc :evidence/kind :external-url)]
        wrong-revision [(update-first-evidence
                         source assoc :evidence/revision
                         "1111111111111111111111111111111111111111")]]
    (is (contains? (validation-error-ids without-evidence)
                   :foresight/lineage-source-evidence))
    (is (contains? (validation-error-ids wrong-kind)
                   :foresight/lineage-evidence-kind))
    (is (contains? (validation-error-ids wrong-revision)
                   :foresight/lineage-evidence-revision))))

(deftest lineage-identities-must-remain-unique
  (let [source (first lineage/sources)
        duplicate-source (conj lineage/sources source)
        duplicate-claim
        [(update source :source/claims conj (first (:source/claims source)))]]
    (is (contains? (validation-error-ids duplicate-source)
                   :foresight/lineage-source-id-unique))
    (is (contains? (validation-error-ids duplicate-source)
                   :foresight/lineage-claim-id-unique))
    (is (contains? (validation-error-ids duplicate-claim)
                   :foresight/lineage-claim-id-unique))))

(deftest live-continuity-claims-require-keyword-carriers
  (let [source (first lineage/sources)
        claims (:source/claims source)
        without-carriers
        [(assoc source
                :source/claims
                (mapv #(if (= :promethean/owner-sovereignty (:claim/id %))
                         (dissoc % :claim/carried-by)
                         %)
                      claims))]
        invalid-carrier
        [(assoc source
                :source/claims
                (mapv #(if (= :promethean/context-field (:claim/id %))
                         (assoc % :claim/carried-by
                                [:openplanner/semantic-graph "not-a-keyword"])
                         %)
                      claims))]]
    (is (contains? (validation-error-ids without-carriers)
                   :foresight/lineage-claim-carriers))
    (is (contains? (validation-error-ids invalid-carrier)
                   :foresight/lineage-claim-carrier-id))))

(defn test-exit-code [summary]
  (if (test/successful? summary) 0 1))

(defmethod test/report [::test/default :end-run-tests] [summary]
  (set! (.-exitCode js/process) (test-exit-code summary)))

(test/run-tests 'lineage-test)
