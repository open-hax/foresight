# Receipt River segments

Each file here is one immutable segment of the Receipt River ledger. Name new
files `<UTC timestamp>-<id>.edn`, for example
`2026-10-13T164500123Z-3f9a1c2b7d4e.edn`, and put one EDN receipt per line,
ending with a newline. Never edit or delete a committed segment.

`../receipts.edn` is the frozen legacy first segment. Do not append to it.

`nbb scripts/evidence.clj verify-receipts --base <commit> --at <commit>` loads
the legacy file and every segment, merges them by `:ts` then record ID, and
checks the merged ledger as a whole: no base segment edited or removed, no
base record dropped, no record repeated, no new record in the legacy file, and
every new record valid under the current receipt law.
