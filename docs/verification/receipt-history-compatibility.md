# Fixed historical Receipt River compatibility

Issue [80](https://github.com/open-hax/foresight/issues/80) describes a baseline
failure: the current envelope law rejects historical scalar `:manifest`/`:refs`
and missing `:pi` fields. Running the actual `verify-receipts` command against
`30b1d321a0184862eaf5f1e41cb25aee8c7ba18a` reproduced exit2 before this repair.
No historical record is rewritten or deleted.

The policy in `src/foresight/receipt_history.cljc` pins the exact original
222,325 bytes, 137 records, immutable Git blob and SHA256. Only the 11 named
physical lines at94–100 and134–137 receive archival classification. Each also
has an exact raw-line digest. These records remain invalid under the current
envelope predicate and cannot attest a promotion result. Their recorded claims
are retained as **unverified**, without asserting that their historical writer
or execution was valid.

The other126 records, including all three evidence-origin records, continue
through the unchanged current envelope and evidence predicates. All appended
records use current law. Neither a newer `--base` nor an archive-registration
record can extend the exception: only a reviewed policy change could alter its
fixed bytes or row list.

The CLI checks this boundary for immutable review, promotion review and the
held file under the existing exclusive append lock. Base bytes must remain an
exact prefix; the fixed archival prefix must match its digest before any row
exception is considered. Current suffix framing must remain complete even when
the caller supplies that suffix as part of `--base`. There is no permissive
parser fallback for changed or malformed rows.

`nbb scripts/evidence.clj register-history` records one canonical observation in
the existing `.ημ/receipts.edn`, using the existing locked append operation. It
identifies the fixed prefix and row digests as unverified archival input. The
command rejects options, an absent or altered prefix, and duplicate
registration. Validation rejects forged registration fields or evidence claims.
The observation records the compatibility decision; it does not grant evidence
or promotion authority.

Verification covers the real pinned bytes plus new canonical receipts,
untrusted future legacy receipts, changed bytes, CRLF conversion, truncation,
malformed evidence-origin records, forged or duplicate registration, held-file
suffix validation, and unchanged-prefix append/retry behavior. A positive
promotion control requires an actual current-schema evidence receipt; archived
records and the registration alone fail the same otherwise-valid control.
Two existing promotion fixtures now provide raw ledger bytes and their matching
digests so the new boundary is exercised instead of mocked away.

The compatibility tests run within the existing `evidence_adapter` gate via
`nbb test/evidence_cli_test.cljs`; no workflow or package-manifest bypass is
introduced. `nbb test/evidence_test.cljs` separately preserves the original pure
evidence/promotion laws. Hosted current-head review and broader suite checks
remain separate requirements.
