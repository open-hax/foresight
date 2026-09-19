# E1.06–E1.07 — Dependency disposition

Updated during PR #96 reconciliation. **Acceptance remains pending:** coordinates
below are observed declarations, not proof of successful isolated consumer builds.

## Three separate revision scopes

- Planning mainline: eta-mu `476b07bd66efb84566a4159556deacb1e9407e6f`.
  `data/sources.json` binds the original 46 relationships to this snapshot.
- Extraction donor: eta-mu `0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90`.
  Retained donor subdirectory dependencies use this commit.
- Current root pin: eta-mu `c2bbf7547592cb9e0c82eee01c2b01555c6cee68`.
  Its CLI manifest switches three npm dependencies and a Mycology source adapter.

## Receiver declarations at the registered pins

| Consumer | Target | Full coordinate | Source |
|---|---|---|---|
| Rheos | Chat UI | `open-hax/chat-ui@86385532b4f8606946555d0ada8e3fb22f35b4c3`, `deps/chat-ui/src` | `rheos/scripts/bootstrap-source-deps.sh` |
| Rheos | Protocols | `github:open-hax/eta-mu#0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90&path:/packages/protocols` | `rheos/package.json`; source adapter fetches the same donor |
| Sol | eta-mu CLI | `open-hax/eta-mu@0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90`, `:deps/root "packages/eta-mu"` | `sol/deps.edn` |
| Sol | Turn Processor | `open-hax/eta-mu@0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90`, `:deps/root "packages/turn-processor"` | `sol/deps.edn` |
| Sol | Katamorph | `open-hax/katamorph@305a5e49d834aca27566f739e8510f6b409fda78`, tag label `v0.2.0` | `sol/deps.edn` |
| Sol | legacy event-ledger | `open-hax/event-ledger@ada7374b7f4e1c3b0ab4e6bbe996f10f06e9b93a` | `sol/deps.edn`; Clio cutover remains separate work |

Rheos at `11811264a308d406cb612aefa1dad40818675e5e` has no `deps.edn`
or Katamorph dependency in `shadow-cljs.edn`. The previous Rheos-to-Katamorph
`v0.2.0` row was unsupported. R004 is instead the **planning mainline**
observation of Katamorph `be7cc332d865cfedc57b55b10cab3c9f2bd41fc4`.
A tag is an informational label; the full commit is the coordinate.

Clio, Chat UI, Session Mycology, and Osmos manifests have no declared sibling
workspace package edges; this bounded observation does not prove runtime closure.
Kanban Orchestrator is contract data with host-loader and Rheos MCP dependencies.
See the [acceptance register](acceptance.md) for missing execution evidence.

## Eta-mu CLI: partial current cutover

At `c2bbf7547592cb9e0c82eee01c2b01555c6cee68`,
`packages/eta-mu/package.json` declares:

| Dependency | Repository revision |
|---|---|
| Rheos | `open-hax/rheos@fbf6aa0c7745765528a0a6cc7bd3fdbd344032d7` |
| Sol | `open-hax/sol@1276955c86ff46936cd1ce7d81fbc790f7068e1e` |
| Session Mycology | `open-hax/session-mycology@30339f9aa3df83ef8c335d4544307272abfbb131` |
| Fork Tax, Receipt River, Turn Processor, Terminal UI, Contracts Output | `open-hax/eta-mu@0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90` with explicit package subdirectory selectors |

`packages/eta-mu/shadow-cljs.edn` retains sibling source paths for Fork Tax,
Receipt River, Turn Processor, and Terminal UI. The Mycology bootstrap adapter
fetches `30339f9aa3df83ef8c335d4544307272abfbb131`. Receipt River is now a
registered child but its CLI consumer is still on the donor. The CLI also pins
an older Rheos revision than Foresight. These are explicit remaining edges.

E1.07 stays incomplete until all affected consumers pass isolated installation,
launcher, build, and test checks with retired donor paths unavailable. Neither
manifest inspection nor reopening a card supplies that proof.
