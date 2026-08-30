;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.shape.prompt-compiler
  "Deterministic pure compiler for authorized prompt fragments."
  (:require [clojure.string :as str]
            [foresight.law.prompt-fragment :as law]))

(def compiler-version 1)

(defn- context-value [context path]
  (get-in context path))

(defn- condition-held? [context condition]
  (= (:equals condition) (context-value context (:path condition))))

(defn active? [fragment context]
  (every? #(condition-held? context %)
          (get-in fragment [:prompt-fragment/conditions :all] [])))

(defn- target-compatible? [fragment target]
  (let [targets' (:prompt-fragment/targets fragment)]
    (or (contains? targets' :portable)
        (contains? targets' target))))

(defn- authority-index [decisions]
  (group-by :prompt-authority/fragment-id decisions))

(defn- authorized? [decisions fragment]
  (let [matching (get decisions (:prompt-fragment/id fragment))]
    (and (= 1 (count matching))
         (= :granted (:prompt-authority/status (first matching))))))

(defn- fragment-order [fragment]
  [(:prompt-fragment/precedence fragment)
   (:prompt-fragment/order fragment)
   (str (:prompt-fragment/id fragment))])

(defn- conflicts [fragments]
  (->> fragments
       (group-by (juxt :prompt-fragment/scope :prompt-fragment/slot))
       (keep (fn [[[scope slot] entries]]
               (let [exclusive (filter #(= :exclusive (:prompt-fragment/merge %)) entries)]
                 (when (and (seq exclusive) (> (count entries) 1))
                   {:diagnostic/code :prompt/conflict
                    :diagnostic/scope scope
                    :diagnostic/slot slot
                    :diagnostic/fragments (mapv :prompt-fragment/id entries)}))))
       vec))

(defn- char-code [text index]
  #?(:clj (int (.charAt ^String text index))
     :cljs (.charCodeAt text index)))

(defn content-fingerprint [text]
  (reduce (fn [acc index]
            (mod (+ (* acc 31) (char-code text index)) 4294967291))
          7
          (range (count text))))

(defn- render-scope [fragments scope]
  (->> fragments
       (filter #(= scope (:prompt-fragment/scope %)))
       (map :prompt-fragment/content)
       (str/join "\n\n")))

(defn- rendered-receipt [target fragments excluded rendered diagnostics]
  {:rendered-prompt-receipt/version 1
   :rendered-prompt-receipt/compiler-version compiler-version
   :rendered-prompt-receipt/target target
   :rendered-prompt-receipt/fragments
   (mapv #(select-keys % [:prompt-fragment/id
                          :prompt-fragment/revision
                          :prompt-fragment/evidence-refs])
         fragments)
   :rendered-prompt-receipt/order (mapv :prompt-fragment/id fragments)
   :rendered-prompt-receipt/excluded excluded
   :rendered-prompt-receipt/character-count (count rendered)
   :rendered-prompt-receipt/token-estimate (long (Math/ceil (/ (count rendered) 4)))
   :rendered-prompt-receipt/content-fingerprint (content-fingerprint rendered)
   :rendered-prompt-receipt/diagnostics diagnostics})

(defn compile-prompt
  [{:keys [fragments authority-decisions context target token-budget]}]
  (let [invalid-fragments (mapcat law/fragment-errors fragments)
        invalid-decisions (mapcat law/authority-decision-errors authority-decisions)
        authority-by-fragment (authority-index authority-decisions)
        inactive (remove #(active? % context) fragments)
        active (filter #(active? % context) fragments)
        incompatible (remove #(target-compatible? % target) active)
        applicable (filter #(target-compatible? % target) active)
        unauthorized (->> applicable
                          (remove #(authorized? authority-by-fragment %))
                          (mapv :prompt-fragment/id))
        selected (->> applicable
                      (filter #(authorized? authority-by-fragment %))
                      (sort-by fragment-order)
                      vec)
        excluded (vec
                  (concat
                   (map (fn [fragment]
                          {:prompt-fragment/id (:prompt-fragment/id fragment)
                           :reason :condition-not-held})
                        inactive)
                   (map (fn [fragment]
                          {:prompt-fragment/id (:prompt-fragment/id fragment)
                           :reason :target-incompatible})
                        incompatible)
                   (map (fn [fragment-id]
                          {:prompt-fragment/id fragment-id
                           :reason :authority-not-granted})
                        unauthorized)))
        conflict-diagnostics (conflicts selected)
        diagnostics (vec
                     (concat invalid-fragments
                             invalid-decisions
                             (when (seq unauthorized)
                               [{:diagnostic/code :prompt/unauthorized
                                 :diagnostic/fragments unauthorized}])
                             conflict-diagnostics))]
    (if (seq diagnostics)
      {:prompt-compilation/status :rejected
       :prompt-compilation/diagnostics diagnostics
       :prompt-compilation/receipt
       (rendered-receipt target selected excluded "" diagnostics)}
      (let [system (render-scope selected :system)
            task (render-scope selected :task)
            rendered (str system (when (and (seq system) (seq task)) "\n\n") task)
            token-estimate (long (Math/ceil (/ (count rendered) 4)))
            budget-diagnostics (if (and token-budget (> token-estimate token-budget))
                                 [{:diagnostic/code :prompt/over-budget
                                   :diagnostic/token-estimate token-estimate
                                   :diagnostic/token-budget token-budget}]
                                 [])
            status (if (seq budget-diagnostics) :over-budget :compiled)]
        {:prompt-compilation/status status
         :prompt-compilation/system system
         :prompt-compilation/task task
         :prompt-compilation/diagnostics budget-diagnostics
         :prompt-compilation/receipt
         (rendered-receipt target selected excluded rendered budget-diagnostics)}))))
