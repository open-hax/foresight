;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns bootstrap-test
  (:require [cljs.test :as test :refer [deftest is testing]]
            [foresight.bootstrap :as bootstrap]
            [foresight.law.bootstrap :as law]
            [foresight.project :as project]))

(def root-sha (apply str (repeat 40 "a")))
(def child-sha (apply str (repeat 40 "b")))
(def other-sha (apply str (repeat 40 "c")))

(def input
  {:bootstrap/root-revision root-sha
   :bootstrap/project project/project
   :bootstrap/manifest (project/gitmodule-declarations)
   :bootstrap/gitlinks
   (mapv (fn [source] {:path (:source/path source) :mode "160000"
                      :revision child-sha})
         (project/submodule-sources))})

(def required-gates #{:project-law :workspace-law})

(def reference-plan
  {:bootstrap/status :planned
   :bootstrap/root-revision root-sha
   :bootstrap/children
   (mapv (fn [source]
           (assoc (select-keys source [:source/id :source/path :source/repository])
                  :source/revision child-sha
                  :source/actionable? (boolean (:source/actionable? source))
                  :source/consolidation? (boolean (:source/consolidation? source))))
         (sort-by :source/path (project/submodule-sources)))})

(defn observations [planned]
  {:bootstrap/root-revision root-sha
   :bootstrap/checkouts
   (mapv (fn [child] (assoc (select-keys child [:source/path :source/revision])
                            :bootstrap/root-revision root-sha
                            :checkout/initialized? true :checkout/fetch :passed))
         (:bootstrap/children planned))
   :bootstrap/gates
   (mapv (fn [gate] {:gate/id gate :gate/outcome :passed
                    :bootstrap/root-revision root-sha})
         (sort required-gates))})

(defn rejected-plan? [changed]
  (let [result (bootstrap/plan changed)]
    (and (= :rejected (:bootstrap/status result))
         (seq (:bootstrap/errors result))
         (not (contains? result :bootstrap/children))
         (every? #(and (= :planning (:stage %)) (contains? % :path))
                 (:bootstrap/errors result)))))

(deftest pins-every-direct-child-without-granting-inventory-authority
  (let [result (bootstrap/plan input)
        children (:bootstrap/children result)
        agents (first (filter #(= ".agents" (:source/path %)) children))]
    (is (law/planned? result))
    (is (= root-sha (:bootstrap/root-revision result)))
    (is (= 23 (count children)))
    (is (= (set (map :source/path (project/submodule-sources)))
           (set (map :source/path children))))
    (is (every? #(= child-sha (:source/revision %)) children))
    (is (false? (:source/actionable? agents)))
    (is (true? (:source/consolidation? agents)))
    (is (not-any? #{"eta" "clobber"} (map :source/path children)))
    (is (= (sort (map :source/path children)) (map :source/path children)))))

(deftest valid-plan-is-independent-of-input-enumeration-order
  (let [reordered (-> input
                      (update :bootstrap/manifest reverse)
                      (update :bootstrap/gitlinks reverse)
                      (update-in [:bootstrap/project :project/sources] reverse))]
    (is (= (bootstrap/plan input) (bootstrap/plan reordered)))))

(deftest planning-fails-closed-on-each-invalid-fact
  (doseq [[label changed]
          [[:missing (update input :bootstrap/gitlinks subvec 1)]
           [:extra (update input :bootstrap/gitlinks conj
                           {:path "undeclared" :mode "160000" :revision child-sha})]
           [:wrong-mode (assoc-in input [:bootstrap/gitlinks 0 :mode] "040000")]
           [:duplicate (update input :bootstrap/gitlinks conj
                               (first (:bootstrap/gitlinks input)))]
           [:escape (assoc-in input [:bootstrap/gitlinks 0 :path] "../outside")]
           [:manifest-drift (assoc-in input [:bootstrap/manifest 0 :url] "changed")]
           [:duplicate-manifest (update input :bootstrap/manifest conj
                                        (first (:bootstrap/manifest input)))]
           [:duplicate-source (update-in input [:bootstrap/project :project/sources]
                                         conj (first (project/submodule-sources)))]]]
    (testing (name label) (is (rejected-plan? changed))))
  (doseq [bad [nil "main" "abc" (apply str (repeat 40 "A"))
              (apply str (repeat 40 "0")) 1]]
    (is (rejected-plan? (assoc input :bootstrap/root-revision bad)))
    (is (rejected-plan? (assoc-in input [:bootstrap/gitlinks 0 :revision] bad)))))

(deftest malformed-boundaries-return-errors-instead-of-an-executable-plan
  (doseq [changed [nil [] {}
                  (assoc input :bootstrap/project nil)
                  (assoc input :bootstrap/manifest nil)
                  (assoc input :bootstrap/gitlinks {})
                  (assoc input :bootstrap/gitlinks [nil])
                  (assoc-in input [:bootstrap/project :project/sources] [nil])]]
    (is (rejected-plan? changed))))

(deftest assessment-needs-exact-checkouts-and-nonempty-current-gates
  (let [planned reference-plan
        observed (observations planned)
        result (bootstrap/assess planned observed required-gates)]
    (is (true? (:bootstrap/ready? result)))
    (is (empty? (:bootstrap/errors result)))
    (is (= root-sha (:bootstrap/root-revision result)))
    (doseq [gates [#{} nil [] #{"project-law"}]]
      (is (false? (:bootstrap/ready? (bootstrap/assess planned observed gates)))))
    (is (false? (:bootstrap/ready? (bootstrap/assess planned nil required-gates))))
    (is (false? (:bootstrap/ready? (bootstrap/assess nil observed required-gates))))))

(deftest assessment-refuses-missing-duplicate-stale-or-failed-observations
  (let [planned reference-plan
        observed (observations planned)]
    (doseq [[label changed]
            [[:root (assoc observed :bootstrap/root-revision other-sha)]
             [:absent (update observed :bootstrap/checkouts subvec 1)]
             [:uninitialized (assoc-in observed [:bootstrap/checkouts 0 :checkout/initialized?] false)]
             [:wrong-head (assoc-in observed [:bootstrap/checkouts 0 :source/revision] other-sha)]
             [:failed-fetch (assoc-in observed [:bootstrap/checkouts 0 :checkout/fetch] :failed)]
             [:stale-child (assoc-in observed [:bootstrap/checkouts 0 :bootstrap/root-revision] other-sha)]
             [:duplicate-child (update observed :bootstrap/checkouts conj
                                       (first (:bootstrap/checkouts observed)))]
             [:unexpected-child (update observed :bootstrap/checkouts conj
                                        {:source/path "nested/undeclared"})]
             [:no-gates (assoc observed :bootstrap/gates [])]
             [:failed-gate (assoc-in observed [:bootstrap/gates 0 :gate/outcome] :failed)]
             [:skipped-gate (assoc-in observed [:bootstrap/gates 0 :gate/outcome] :not-applicable)]
             [:stale-gate (assoc-in observed [:bootstrap/gates 0 :bootstrap/root-revision] other-sha)]
             [:duplicate-gate (update observed :bootstrap/gates conj
                                      (first (:bootstrap/gates observed)))]]]
      (testing (name label)
        (let [result (bootstrap/assess planned changed required-gates)]
          (is (false? (:bootstrap/ready? result)))
          (is (seq (:bootstrap/errors result)))
          (is (every? #(and (= :assessment (:stage %)) (contains? % :path))
                      (:bootstrap/errors result))))))))

(deftest supplied-plan-must-retain-its-child-contract
  (let [planned reference-plan]
    (doseq [changed [(assoc planned :bootstrap/children [])
                    (assoc-in planned [:bootstrap/children 0 :source/revision] "main")
                    (update planned :bootstrap/children conj
                            (first (:bootstrap/children planned)))]]
      (is (false? (:bootstrap/ready?
                   (bootstrap/assess changed (observations planned) required-gates)))))))

(test/run-tests)
