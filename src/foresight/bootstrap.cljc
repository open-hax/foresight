;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.bootstrap
  "Pinned direct-child planning and supplied-observation assessment; no I/O."
  (:require [foresight.law.bootstrap :as law]
            [foresight.law.project :as project-law]))

(defn- error [stage path code]
  {:stage stage :path path :error code})

(defn- membership-errors [stage section expected records key-field]
  (let [actual (set (map key-field records))]
    (vec
     (concat
      (map #(error stage [section %] :duplicate)
           (project-law/duplicates (map key-field records)))
      (map #(error stage [section %] :missing)
           (sort (remove actual expected)))
      (map #(error stage [section %] :undeclared)
           (sort-by pr-str (remove expected actual)))))))

(defn- planning-boundary-errors [input]
  (vec
   (concat
    (when-not (law/revision? (:bootstrap/root-revision input))
      [(error :planning [:bootstrap/root-revision] :invalid-revision)])
    (when-not (law/project-shape? (:bootstrap/project input))
      [(error :planning [:bootstrap/project] :invalid-project-shape)])
    (for [section [:bootstrap/manifest :bootstrap/gitlinks]
          :when (not (law/records? (get input section)))]
      (error :planning [section] :invalid-records))
    (when (law/records? (:bootstrap/manifest input))
      (keep-indexed (fn [idx record]
                      (when-not (law/manifest-record? record)
                        (error :planning [:bootstrap/manifest idx] :invalid-record)))
                    (:bootstrap/manifest input))))))

(defn plan
  "Plan supplied committed facts. Invalid facts yield errors and no children.

   input carries :bootstrap/root-revision, :bootstrap/project,
   :bootstrap/manifest (.gitmodules records), and :bootstrap/gitlinks
   ({:path string :mode \"160000\" :revision full-object-id}). No child is read."
  [input]
  (let [boundary-errors (planning-boundary-errors input)]
    (if (seq boundary-errors)
      {:bootstrap/status :rejected :bootstrap/errors boundary-errors}
      (let [project (:bootstrap/project input)
            sources (filter #(= :git-submodule (:source/type %))
                            (:project/sources project))
            root (:bootstrap/root-revision input)
            links (:bootstrap/gitlinks input)
            by-path (into {} (map (juxt :path identity)) links)
            children
            (mapv (fn [source]
                    (assoc (select-keys source [:source/id :source/path :source/repository])
                           :source/revision (:revision (get by-path (:source/path source)))
                           :source/actionable? (boolean (:source/actionable? source))
                           :source/consolidation? (boolean (:source/consolidation? source))))
                  (sort-by :source/path sources))
            errors
            (vec
             (concat
              (map #(assoc % :stage :planning)
                   (:errors (project-law/validate-project project (:bootstrap/manifest input))))
              (when (empty? sources)
                [(error :planning [:bootstrap/project :project/sources] :no-direct-children)])
              (membership-errors :planning :bootstrap/gitlinks
                                 (set (map :source/path sources)) links :path)
              (mapcat (fn [{:keys [path mode revision]}]
                        (cond-> []
                          (not (project-law/confined-relative-path? path))
                          (conj (error :planning [:bootstrap/gitlinks path :path] :unconfined-path))
                          (not= "160000" mode)
                          (conj (error :planning [:bootstrap/gitlinks path :mode] :not-gitlink))
                          (or (not (law/revision? revision))
                              (not= (count root) (count revision)))
                          (conj (error :planning [:bootstrap/gitlinks path :revision] :invalid-revision))))
                      links)
              (for [child children :when (not (law/child? child))]
                (error :planning [:bootstrap/children (:source/path child)] :invalid-child))))]
        (if (seq errors)
          {:bootstrap/status :rejected :bootstrap/errors errors}
          {:bootstrap/status :planned :bootstrap/root-revision root
           :bootstrap/children children})))))

(defn- assessment-boundary-errors [expected-plan planned observations required-gates]
  (vec
   (concat
    (when-not (law/planned? planned)
      [(error :assessment [:bootstrap/plan] :invalid-plan)])
    (when-not (= :planned (:bootstrap/status expected-plan))
      [(error :assessment [:bootstrap/planning-input] :invalid-planning-input)])
    (when-not (= expected-plan planned)
      [(error :assessment [:bootstrap/plan] :plan-not-bound-to-input)])
    (when-not (law/required-gates? required-gates)
      [(error :assessment [:bootstrap/required-gates] :missing-gate-policy)])
    (when-not (= (:bootstrap/root-revision planned)
                 (:bootstrap/root-revision observations))
      [(error :assessment [:bootstrap/root-revision] :root-revision-mismatch)])
    (for [section [:bootstrap/checkouts :bootstrap/gates]
          :when (not (law/records? (get observations section)))]
      (error :assessment [section] :invalid-records)))))

(defn assess
  "Assess supplied checkout and gate facts for the planned root revision.

   planning-input is the original project and committed manifest/gitlink facts.
   Recompute the expected plan and reject any edited or truncated supplied plan.
   required-gates is a nonempty set of caller-policy keyword identities.
   Each checkout must explicitly report initialized?, successful fetch and exact
   HEAD/root binding. Each required gate must pass for the same root. These are
   supplied facts, not independently attested host evidence or an executed plan."
  [planning-input planned observations required-gates]
  (let [expected-plan (plan planning-input)
        boundary-errors (assessment-boundary-errors expected-plan planned observations required-gates)
        errors
        (if (seq boundary-errors)
          boundary-errors
          (let [root (:bootstrap/root-revision planned)
                checkouts (:bootstrap/checkouts observations)
                by-path (into {} (map (juxt :source/path identity)) checkouts)
                gates (:bootstrap/gates observations)
                by-gate (into {} (map (juxt :gate/id identity)) gates)]
            (vec
             (concat
              (membership-errors :assessment :bootstrap/checkouts
                                 (set (map :source/path (:bootstrap/children planned)))
                                 checkouts :source/path)
              (mapcat (fn [child]
                        (let [path (:source/path child)
                              observed (get by-path path)]
                          (cond-> []
                            (not (true? (:checkout/initialized? observed)))
                            (conj (error :assessment [:bootstrap/checkouts path] :not-initialized))
                            (not= :passed (:checkout/fetch observed))
                            (conj (error :assessment [:bootstrap/checkouts path] :fetch-not-passed))
                            (not= (:source/revision child) (:source/revision observed))
                            (conj (error :assessment [:bootstrap/checkouts path] :head-mismatch))
                            (not= root (:bootstrap/root-revision observed))
                            (conj (error :assessment [:bootstrap/checkouts path] :stale-observation)))))
                      (:bootstrap/children planned))
              (map #(error :assessment [:bootstrap/gates %] :duplicate)
                   (project-law/duplicates (map :gate/id gates)))
              (for [gate gates :when (not (keyword? (:gate/id gate)))]
                (error :assessment [:bootstrap/gates (:gate/id gate)] :invalid-gate-identity))
              (mapcat (fn [id]
                        (let [gate (get by-gate id)]
                          (cond-> []
                            (not= :passed (:gate/outcome gate))
                            (conj (error :assessment [:bootstrap/gates id] :gate-not-passed))
                            (not= root (:bootstrap/root-revision gate))
                            (conj (error :assessment [:bootstrap/gates id] :stale-observation)))))
                      (sort required-gates))))))]
    {:bootstrap/ready? (empty? errors)
     :bootstrap/root-revision (:bootstrap/root-revision planned)
     :bootstrap/errors errors}))
