# An always-on creative cephalon

## Accepted operator intent

The operator described OpenHax as the fun, visible head of Knoxx:

> he's constantly making artwork. He's constantly making music, or lyrics, or MIDI things.

> it will always immediately respond to me, and me talking to it will never interrupt it.

Creation and publication to the configured Bluesky account, with occasional
sharing to the home Discord channels, are authorized. This is a continuing
product outcome, not a request for a single generated greeting.

## Observed connections

- Knoxx `contracts/namespaces/ussyverse_social.edn` declares a 30-minute
  schedule, a synthetic event and an agent-start agreement. Its task permits
  doing nothing; it does not require a saved creative artifact.
- `contracts/namespaces/ussyverse.edn` starts a separate reply agent on mentions
  and keywords. The local deployment also scopes this agreement to two home
  channels. Keyword gating misses ordinary operator conversation.
- `backend/src/cljs/knoxx/backend/infra/agent/runner.cljs` admits triggered
  runs through one bounded FIFO. `shape/event_turn_queue.cljc` is its pure
  reservation/release layer. Interactive HTTP starts bypass that event FIFO;
  Discord conversation currently does not.
- The local runtime uses one event slot and a ten-minute event deadline. The
  scheduler prevents concurrent dispatch calls, but dispatch admission is not
  full creative-run completion. These are different lifetimes.
- `contracts/agents/ussyverse_social_creative.edn` has audio, research and
  notifier workers. Native tool execution already produced a spoken artifact
  and a real Discord attachment; see the restoration report and Receipt River.
- The active consumer is currently the durable local Compose instance at
  `~/.local/share/promethean/services/knoxx-social-local`. The cloud consumer
  for the same identity was persistently disabled before activation. This
  proves one owner; it does not prove availability while the local host sleeps.

## Derived connections

- A responsive head and independent makers require independent admission and
  conversation coordinates, not only different prompts or a larger FIFO.
- The same artifact can move through Alpha integrity, Eta production, Mu
  evaluation and Pi publication. Katamorph contracts describe replaceable
  boundaries; Clio records durable facts. The existing Knoxx adapters should
  consume these seams rather than invent another ledger or board authority.
- Epiphany's iterative content examination and Calliope's music/video work are
  related workflow surfaces. Knoxx is the configured user-facing composition.
  This relation does not grant either corpus repository ownership of the live
  agent scheduler.

## Proposed delivery and limits

Epic `25e3a688-09b5-4e9a-8765-9b13944b1a00` tracks four reviewable slices:
independent head admission; recurring artifact production; event-driven
publication; and durable operation/recovery. Keep the current consumer useful
while reviewing the stronger source guarantees. Label deployment overlays and
measured behavior separately from merged source guarantees.

"Immediate" means prompt admission with no waiting behind a maker, followed by
a measured response target. Provider latency and outages remain observable;
neither a queued acknowledgment nor a typing indicator counts as a completed
reply. "Always-on" means supervised recurring work with bounded retries and
recovery, not unlimited generation or uncontrolled publication.

License: GPL-3.0-or-later.
