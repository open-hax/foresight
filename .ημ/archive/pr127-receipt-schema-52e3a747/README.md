# PR #127 receipt schema correction

The Receipt River helper emitted `manifest` and `refs` as strings in three
new receipts. Foresight's `receipt-envelope?` requires vectors. These original
bytes are retained in `helper-emitted-receipts.edn`, with SHA-256
`f457b1c98e6e9420e77b7549d5368dcdd92afe275e5aebf46d32ba81021e834e`. They are also inspectable in Git commit
`52e3a7477482ea23a35909db2afb9b3cd7b75ebe`.

On October 4, 2026, the user explicitly approved the displayed three-entry
schema correction and preservation of the originals. Only those three new
helper-emitted entries are normalized in the active ledger. All prior main
receipt bytes remain its unchanged prefix. The decision receipt records this
scoped append-only exception. The correction retains every field and value;
comma-separated manifest/ref strings become vectors and `manifest: none`
becomes an empty vector. The separate native review-ID correction remains in
the ledger, without silently erasing the erroneous reference.

These are raw historical helper outputs, not valid current receipt envelopes.
No reviewer approval, board event, or qualification evidence is fabricated.
