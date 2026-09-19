# Module acceptance register

This register distinguishes **registered in PR #96** from **accepted for
standalone use**. None of the registrations below asserts E1.01–E1.10 completion.
The candidate pins were cloned successfully during reconciliation. The root
inventory, project and catalog tests validate Foresight's registration surface;
they do not certify a child's build, behavior, policy, or production readiness.

## Registered candidate revisions

| Child | Recorded Git revision | Acceptance / remaining evidence |
|---|---|---|
| [kanban-orchestrator](https://github.com/open-hax/kanban-orchestrator/tree/8e16609dd191bdd2e9a3c0320ce4238b5cd83467) | `8e16609dd191bdd2e9a3c0320ce4238b5cd83467` | Pending. Conformance and host-load gates absent; no AGENTS at pin. |
| [clio](https://github.com/open-hax/clio/tree/788cdd3434615a7932b924e68520dbf7f88408c2) | `788cdd3434615a7932b924e68520dbf7f88408c2` | Pending. No AGENTS at pin; installed-consumer/assembly evidence missing. |
| [chat-ui](https://github.com/open-hax/chat-ui/tree/86385532b4f8606946555d0ada8e3fb22f35b4c3) | `86385532b4f8606946555d0ada8e3fb22f35b4c3` | Pending. No AGENTS at pin; standalone build/test/consumer evidence missing. |
| [rheos](https://github.com/open-hax/rheos/tree/11811264a308d406cb612aefa1dad40818675e5e) | `11811264a308d406cb612aefa1dad40818675e5e` | Pending. No AGENTS at pin; browser smoke and clean bootstrap/consumer evidence missing. |
| [session-mycology](https://github.com/open-hax/session-mycology/tree/30339f9aa3df83ef8c335d4544307272abfbb131) | `30339f9aa3df83ef8c335d4544307272abfbb131` | Pending. No AGENTS at pin; standalone CLI/build/test evidence missing. |
| [sol](https://github.com/open-hax/sol/tree/1276955c86ff46936cd1ce7d81fbc790f7068e1e) | `1276955c86ff46936cd1ce7d81fbc790f7068e1e` | Pending. Guide exists; clean dependency closure and test/build evidence missing. |
| [osmos](https://github.com/open-hax/osmos/tree/0c33018e27f861536822086afcdab666a60add44) | `0c33018e27f861536822086afcdab666a60add44` | Pending. No README/AGENTS at pin; claimed Docker/config changes are later revisions; service/client/image proof missing. |
| [receipt-river](https://github.com/open-hax/receipt-river/tree/7c7c62343fea0241ac545e37f14e57ab344bfa30) | `7c7c62343fea0241ac545e37f14e57ab344bfa30` | Pending. Guide exists; eta-mu consumer still references donor; standalone evidence missing. |
| [axxium](https://github.com/open-hax/axxium/tree/ee5284a00eb5c034cd6ef0db281726f00ce77cca) | `ee5284a00eb5c034cd6ef0db281726f00ce77cca` | Pending. Existing history preserved on reconciliation revision; independent acceptance evidence missing. |

`data/registration.json` is the machine-readable companion. Gate entries in
`config/quality-gates.edn` cite files at these pins. Missing commands are explicit
unavailable entries; no no-op is treated as a quality gate. E1.09 requires
repository-owned counts, reports, coverage policy and complete guidance before
acceptance. Registering Receipt River expanded the original eight-target plan
to nine direct registrations; the original eight included existing Axxium.

## Lifecycle reconciliation

The original E1.01–E1.10 cards declared `done` without matching canonical
completion histories. Present-time CLI comments explain each gap. Legal
`done → review → in_progress` reopening preserves the observed prior card state;
it does not invent historical testing/review events. E1.01 and E1.05 remain in
progress; E1.02–E1.04 and E1.06–E1.10 proceed through `breakdown → blocked`.
E1.11–E1.12 remain todo, subject to their recorded prerequisites.

The original “6 new repos” comment is preserved as history and corrected by a
new CLI comment: eight original targets minus existing Axxium equals **seven**
new repositories. Receipt River was added later.

## Accepted-module documentation procedure

When the owning module PR is merged and its exact accepted commit has the
required evidence, update the following together in the Foresight repin PR:

1. Record the merge URL, full accepted SHA, exact-revision test/build/lint and
   applicable consumer/image/browser evidence, plus actual required review
   results. Missing or inaccessible evidence leaves acceptance pending.
2. Update the gitlink, this register and `data/registration.json`; replace the
   pending row only for that module. Keep original snapshots as historical data.
3. Refresh `AGENTS.md`, `README.md`, project routing, the quality-gate catalog,
   and the typed dependency disposition. Scope shared abstractions to Foresight;
   child product guidance remains independently usable.
4. Check the child README commands and AGENTS at the accepted commit. Move
   suite-level documents only with E1.12's source/destination and link map.
5. Append canonical receipts and use lawful CLI transitions with evidence.
   Acceptance does not authorize donor deletion: E1.11 still requires zero live
   inbound edges and consumer regression evidence.

## Follow-up ownership

- [#107 Truth](https://github.com/open-hax/foresight/issues/107),
  [#108 .agents](https://github.com/open-hax/foresight/issues/108),
  [#109 Calliope](https://github.com/open-hax/foresight/issues/109), and
  [#110 Services](https://github.com/open-hax/foresight/issues/110) own separate
  validated repins. PR #96 restores these four paths to the current main pins.
- [#111](https://github.com/open-hax/foresight/issues/111) owns E1.10's trusted
  review-completion rollout. Policy reads returning 403 are unavailable;
  they neither prove absent policy nor authorize weaker protection.
- E1.03/E1.06/E1.07 own cold dependency and consumer closure;
  E1.08 owns Osmos compatibility; E1.09 owns honest child quality gates;
  E1.12 owns the final documentation transfer after accepted cutovers.

## Receipt provenance

The original multiline root ledger is archived byte-for-byte under
`.ημ/archive/pr96-c551e986/root-receipts.edn` (SHA-256
`a9a0f48df3aaf53cec1a0d34f7551e77850577cb9d6a8c38543363643b3fffed`).
The original planning-upload receipt is preserved beside it, SHA-256
`40a811fcb1415bc28dd94391690fd270ce80b47eb2f125bdec74aae91c3ad033`.
Canonical `.ημ/receipts.edn` keeps main's historical prefix unchanged and
appends schema-valid import/correction records. Imported completion claims are
**reported history**, not admitted execution evidence.

The recovered 33-file planning ZIP is 329,573 bytes, SHA-256
`60c2af81121cc94568ee4f5b777b7708a57a5bf18cd95a5f3c0e77d2d40772c1`.
This identifies the recovered archive bytes. It does not retrospectively prove
the original author's claimed upload/download comparison or any product tests.
