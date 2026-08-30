;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.law.prompt-fragment
  "Portable contracts for composable prompt fragments and authority decisions.
   The compiler consumes authority decisions; it does not issue them.")

(def scopes #{:system :task})
(def contributor-kinds #{:actor :role :capability :policy :skill :workflow})
(def merge-modes #{:append :exclusive})
(def targets #{:claude :opencode :codex :mcp :portable})
(def authority-statuses #{:granted :denied :unavailable})

(defn- error [law-id path message expected actual]
  {:law/id law-id :path path :message message :expected expected :actual actual})

(defn- non-empty? [value]
  (and (some? value) (not= "" value)))

(defn fragment-errors [fragment]
  (let [contributor (:prompt-fragment/contributor fragment)]
    (vec
     (concat
      (when-not (= 1 (:prompt-fragment/version fragment))
        [(error :prompt-fragment/version [:prompt-fragment/version]
                "Prompt fragment version must be explicit" 1
                (:prompt-fragment/version fragment))])
      (doseq [[key' value] [[:prompt-fragment/id (:prompt-fragment/id fragment)]
                            [:prompt-fragment/revision (:prompt-fragment/revision fragment)]
                            [:prompt-fragment/content (:prompt-fragment/content fragment)]]
              :when (not (non-empty? value))]
        (error :prompt-fragment/required [key']
               "Prompt fragment field must be non-empty" :non-empty value))
      (when-not (contains? scopes (:prompt-fragment/scope fragment))
        [(error :prompt-fragment/scope [:prompt-fragment/scope]
                "Prompt scope must be portable" scopes
                (:prompt-fragment/scope fragment))])
      (when-not (contains? merge-modes (:prompt-fragment/merge fragment))
        [(error :prompt-fragment/merge [:prompt-fragment/merge]
                "Prompt merge mode must be explicit" merge-modes
                (:prompt-fragment/merge fragment))])
      (when-not (integer? (:prompt-fragment/precedence fragment))
        [(error :prompt-fragment/precedence [:prompt-fragment/precedence]
                "Prompt precedence must be an integer" :integer
                (:prompt-fragment/precedence fragment))])
      (when-not (integer? (:prompt-fragment/order fragment))
        [(error :prompt-fragment/order [:prompt-fragment/order]
                "Prompt order must be an integer" :integer
                (:prompt-fragment/order fragment))])
      (when-not (contains? contributor-kinds (:kind contributor))
        [(error :prompt-fragment/contributor-kind
                [:prompt-fragment/contributor :kind]
                "Contributor kind must be registered" contributor-kinds
                (:kind contributor))])
      (when-not (non-empty? (:id contributor))
        [(error :prompt-fragment/contributor-id
                [:prompt-fragment/contributor :id]
                "Contributor identity is authority-scoped and required"
                :non-empty (:id contributor))])
      (when-not (and (set? (:prompt-fragment/targets fragment))
                     (seq (:prompt-fragment/targets fragment))
                     (every? targets (:prompt-fragment/targets fragment)))
        [(error :prompt-fragment/targets [:prompt-fragment/targets]
                "Targets must be a non-empty subset of registered targets"
                targets (:prompt-fragment/targets fragment))])
      (when-not (vector? (:prompt-fragment/evidence-refs fragment))
        [(error :prompt-fragment/evidence-refs
                [:prompt-fragment/evidence-refs]
                "Prompt provenance must be a vector of EvidenceRef values"
                :vector (:prompt-fragment/evidence-refs fragment))])))))

(defn valid-fragment? [fragment]
  (empty? (fragment-errors fragment)))

(defn authority-decision-errors [decision]
  (vec
   (concat
    (when-not (= 1 (:prompt-authority/version decision))
      [(error :prompt-authority/version [:prompt-authority/version]
              "Authority decision version must be explicit" 1
              (:prompt-authority/version decision))])
    (when-not (non-empty? (:prompt-authority/fragment-id decision))
      [(error :prompt-authority/fragment-id [:prompt-authority/fragment-id]
              "Authority decision must name a fragment" :non-empty
              (:prompt-authority/fragment-id decision))])
    (when-not (non-empty? (:prompt-authority/authority decision))
      [(error :prompt-authority/authority [:prompt-authority/authority]
              "Authority decision must name its issuing authority" :non-empty
              (:prompt-authority/authority decision))])
    (when-not (contains? authority-statuses (:prompt-authority/status decision))
      [(error :prompt-authority/status [:prompt-authority/status]
              "Authority status must remain explicit" authority-statuses
              (:prompt-authority/status decision))]))))
