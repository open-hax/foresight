(require '[evidence :as adapter]
         '[foresight.evidence :as law]
         '["node:fs" :as fs])
(let [input (js->clj (js/JSON.parse (fs/readFileSync (first *command-line-args*) "utf8")) :keywordize-keys true)
      receipt (update input :kind keyword)]
  (assert (law/receipt-envelope? receipt))
  (adapter/require-valid-receipt-records! [receipt])
  (adapter/append-edn-line! ".ημ/receipts.edn" receipt)
  (println "One prospective flat root catalog appended; historical gate unchanged."))
