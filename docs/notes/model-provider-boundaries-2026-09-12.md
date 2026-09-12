# Local model and integration provider boundaries

This successor addresses root PR #91 findings
[3996448991](https://github.com/open-hax/foresight/pull/91#discussion_r3996448991)
and [3996448995](https://github.com/open-hax/foresight/pull/91#discussion_r3996448995).
The exact file hashes and verification results are in
[model-provider-boundaries.json](evidence/model-provider-boundaries.json).

The Epiphany service supervisor now imports MongoDB from its owning devtools
package, which pins version `6.21.0`. A selected root installation no longer
depends on an independently installed Knoxx backend. The generated root package
and provenance manifest were regenerated using the unchanged declared eta-mu
revision `a7b19825fb5d7c624c38f1d41043c42e92d7f0c3`. The lockfile adds only MongoDB
and its dependency closure; all previously locked versions remain unchanged.

The embedding HTTP adapter checks the final native tensor conversion before any
successful response. It requires exactly one row per input, exactly 384 numbers
per row, and finite values that remain finite when encoded as Float32. Iteration
also rejects sparse rows. The same check precedes both OpenAI-compatible aliases
and the Ollama-compatible route, including base64 serialization. Failed model
output returns HTTP 500 with `embedding_failed`.

| Obstacle | Resolution and evidence |
| --- | --- |
| The active eta-mu checkout differs from the root's recorded gitlink. | An isolated root worktree uses the exact recorded eta-mu revision. Normal manifest generation and drift checking pass; no generator guard was bypassed. |
| The fresh offline install lacks `@mongodb-js/saslprep@1.5.4`. | The frozen install reused 284 packages from the shared store and downloaded the one missing package. No dependency version changed during that install. |
| The package manager reports ignored dependency build scripts. | Existing package-manager policy remains in effect. Actual native MiniLM inference, Mongo ping, S3 roundtrip and JVM integration all succeed under that policy; the installation warning is retained in the evidence. |
| The HTTP service accepts malformed provider tensors. | Ten tests run real MiniLM inference and corrupt only its final pooled `Tensor.tolist` conversion. All ten first failed because the old service returned HTTP 200; all pass after validation. Cases cover row count, dimensions, NaN, both infinities, nonnumeric values, sparse entries and Float32 overflow. |
| Strict lint flags the supervisor's existing ANSI control-character regular expression. | Node's `stripVTControlCharacters` performs that conversion for summary matching. The original transcript is still retained. Scoped lint reports zero errors and warnings. |

The full model HTTP suite passes **71 tests with no skips**. The separate offline
MiniLM smoke test verifies 384 finite normalized dimensions, semantic ordering,
base64 output and invalid-request refusal. Manifest tests pass **7 tests and 19
assertions**.

The actual service supervisor also passes **22 JVM integration tests and 108
assertions, with no skips**, in the isolated root checkout where Knoxx backend
dependencies are absent. It starts native MongoDB, S3rver and offline MiniLM,
checks Mongo's command protocol and an actual S3 EDN object roundtrip, and stops
its owned processes afterward. This run uses the independently frozen Clio
revision `6c5af6077d069620583b29a180eb925a0805e94b` and Epiphany tree
`4a5d25da32f28a08cb6a1c27392aeb032bb34600` (local checkpoint
`02d81135c7e273d5da01757e8b4603cc449fd069`, published as
`7c157fb3a824cf6ff23205486ccf8e285ea78fba`). Their source is checked unchanged
throughout the run. This service-proof selection is separate from the declared
eta-mu revision consumed by the manifest generator.

These are scoped provider and installation checks. Final root child promotion,
aggregate build/browser gates and external review remain separate steps.
