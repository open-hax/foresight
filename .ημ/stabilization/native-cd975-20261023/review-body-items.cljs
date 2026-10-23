(require '[pr-flow.law :as law] '["node:fs" :as fs])
(let [capture (js/JSON.parse (fs/readFileSync ".ημ/stabilization/native-cd975-20261023/eta-reviews.json" "utf8"))
      rows (js->clj (js/JSON.parse (.-stdout capture)) :keywordize-keys true)
      flagged (law/outstanding-review-bodies "cd9751d01a2b93123281d032f42a83fb4b9d42cb" rows)]
 (let [items (mapv (fn [r] {:review-id (:id r) :head (:commit_id r) :items (law/review-findings r)}) flagged)]
  (fs/writeFileSync ".ημ/stabilization/native-cd975-20261023/review-body-items.json" (js/JSON.stringify (clj->js items) nil 2))
  (println (pr-str items))))
