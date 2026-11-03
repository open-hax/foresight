;; SPDX-License-Identifier: GPL-3.0-or-later
(require '[cheshire.core :as json])
(load-file (str (.getParent (java.io.File. *file*)) "/budget_law.cljc"))
(println (json/generate-string (research.colab-budget-law/admission (json/parse-string (slurp *in*) true))))
