# Foresight main reconciliation

This is the integration of upstream `d5921c30` and personal-fork `a8dff342`
for [PR #150](https://github.com/open-hax/foresight/pull/150). The independent
upstream `root-ci.yml` remains in place; the retired review wrapper is not
restored. The OpenPlanner pin includes the already-published removal of its
unavailable machine-local nested submodule.

The Kanban and Session Mycology files keep upstream's bytes as a prefix and
append the personal side's bytes after the shared ancestor. They do not change
layout or format. Receipt River keeps the fork's frozen legacy blob and
existing segments, and stores all 48 upstream-only legacy rows as exact
one-record segments. Receipt River remains the semantic verifier.

## Offline receipt round trip

Use a full checkout with both parent commits accessible. The tool targets
Linux and Python 3; its atomic publication uses `renameat2`. Choose a private
parent directory outside `.ημ/`. Neither command changes the checkout.

```sh
mkdir -m 700 /path/to/receipt-review
scripts/reconcile_receipts_sync_main_150_v1.py forward \
  --output /path/to/receipt-review/forward --plan
scripts/reconcile_receipts_sync_main_150_v1.py forward \
  --output /path/to/receipt-review/forward
scripts/reconcile_receipts_sync_main_150_v1.py reverse \
  --input /path/to/receipt-review/forward --output /path/to/receipt-review/reverse
PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover \
  -s test -p 'receipt_reconcile_sync_main_150_v1_test.py'
```

The reverse output reconstructs the exact upstream legacy file and the
original fork segments. It is a recovery snapshot, not permission to
reintroduce an active legacy writer. Reverse refuses additional or changed
files; later writes need their own reconciliation. Both parent commits retain
their original histories. Verify the committed candidate against **each**
parent with `nbb -cp src:scripts scripts/evidence.clj verify-receipts --base
<parent-sha> --at <candidate-sha>` before activation.
