# Receipt River segments

Each file here is one immutable segment of the Receipt River ledger. Name new
files `<UTC timestamp>-<id>.edn`, for example
`2026-10-13T164500123Z-3f9a1c2b7d4e.edn`. Each file holds exactly one EDN
receipt on one line, ending with a newline. Never edit or delete a committed segment.

`../receipts.edn` is the frozen legacy first segment. Do not append to it.

`nbb scripts/evidence.clj verify-receipts --base <commit> --at <commit>` loads
the legacy file and every segment, merges them by the UTC instant of `:ts`
(offsets and fractional seconds normalized) then record ID, and
checks the merged ledger as a whole: no base segment edited or removed, no
base record dropped, no record repeated, no new record in the legacy file, and
every new record valid under the current receipt law.

New records need an RFC 3339 `:ts` naming a real instant (second 60 only at
an actual IERS leap second, 23:59:60 UTC); historical records
keep whatever `:ts` they were written with. Writers in one checkout take
`.receipt-river.writer.lock` for the whole reserve, gate and write sequence, so
they run one at a time. A retained `.<segment>.append.lock` is a quarantine
marker: the ledger stays closed to writers until someone adjudicates it. So
does `../.receipt-river.quarantine`, left when this directory was renamed or
replaced while a gate ran.
Segment and legacy files must not be executable (Git `100644` blobs only).
