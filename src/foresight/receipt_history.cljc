;; SPDX-License-Identifier: LGPL-3.0-or-later
(ns foresight.receipt-history
  "A fixed archival exception for pre-contract receipts; never promotion evidence."
  (:require [foresight.evidence :as evidence]))

(def historical-prefix
  {:ledger/path ".ημ/receipts.edn"
   :ledger/revision "30b1d321a0184862eaf5f1e41cb25aee8c7ba18a"
   :ledger/blob "b7880c31e28ed3b4d1834cca80977a013324a2f9"
   :ledger/bytes 222325
   :ledger/records 137
   :ledger/sha256 "b5d9273a4b9e7f0ea9a72cc7c7ebcd43c93e63d7f3c49bb3682636b8e4fd1d9d"})

;; One-based physical lines; digests cover UTF-8 line bytes without the LF.
;; Changing this policy requires review, not a newer --base argument.
(def unverified-rows
  {94 "40b5ac1d7046f9e641f0fc029df7b0e1a538ce8beef00a531842aff28b23feb0"
   95 "d5e134a8b35e5cb5a52bd6f030a7b8b7930e4cf13eb37ab48fa51bb3d9d36f12"
   96 "3a514b24295017430fbd6b1962232791803e94db042e161235a26f21d58c085a"
   97 "19448445ce6e26533ed9ef53ee58ee49d5810a60b2a6439a6ef1ef250fa2b4e8"
   98 "8d79bde8f48d462a9c95a724454da31c75a68f673cc89380f42b8fb7a1c24ae1"
   99 "8a341b3d1c2f362f775b1a74545170fa981d271191d95e97b556b6901525ed18"
   100 "4e5f9ebc55955e85256c2d3beea70cb5dae92b980fbfd592f1a2a0876fddbe7f"
   134 "c7f156235c3ce8c7d10a747d80d3e8fe81dccd1e1d14112262510c3250638408"
   135 "b447ba54dd6f8fc907f45929fb96c83cc51599c7e2351b4add364d963e577274"
   136 "d8c5027959d24caf61aa6329fb965bdc2c06be6294591a125aff63d6fe91cfcf"
   137 "d4b403ca68dc7a2fb83bdaca081ad1412996be7e8573fe4e3624bf50c4cd1855"})

(def archive-origin "foresight-receipt-history-archive")

(defn evidence-bearing? [receipt]
  (or (= evidence/evidence-receipt-origin (:origin receipt))
      (some #(and (keyword? %) (= "evidence" (namespace %)))
            (keys receipt))))

(defn unverified-row? [line digest receipt]
  (and (contains? unverified-rows line)
       (= (get unverified-rows line) digest)
       (map? receipt)
       (not (evidence-bearing? receipt))))

(defn archive-registration [timestamp hostname]
  {:kind :observation
   :ts timestamp
   :origin archive-origin
   :owner "foresight-receipt-history"
   :dod "Preserve exact historical bytes as unverified archival input"
   :pi "foresight"
   :host hostname
   :manifest [evidence/receipt-ledger-path "src/foresight/receipt_history.cljc"]
   :refs ["https://github.com/open-hax/foresight/issues/80"
          (str (:ledger/revision historical-prefix) ":" evidence/receipt-ledger-path)]
   :archive/classification :unverified
   :archive/prefix historical-prefix
   :archive/rows (mapv (fn [[line digest]] {:line line :sha256 digest})
                      (sort-by key unverified-rows))})

(defn archive-registration-candidate? [receipt]
  (or (= archive-origin (:origin receipt))
      (some #(and (keyword? %) (= "archive" (namespace %)))
            (keys receipt))))

(defn valid-archive-registration? [receipt]
  (and (evidence/receipt-envelope? receipt)
       (= receipt (archive-registration (:ts receipt) (:host receipt)))))
