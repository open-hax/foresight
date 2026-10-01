---
original_name: "claude-code session 576342b7, 2026-10-01"
title: "The muse process is the product: epiphany → calliope → knoxx"
summary: "While asking for a PR skill graph, the user named the recurring process (muse, connect, review with agents, promote) as the thing epiphany, calliope and Muse each implement. Records their words verbatim and the connections found, each labelled with its epistemic tier."
category: "architecture"
created: "2026-10-01"
---

# What was said

The user, while describing the PR workflow they want captured as a skill graph
(verbatim, lightly trimmed):

> 0. from beginning, to end, I muse about, talk about what I am thinking about,
> we are making connections between documents/code/programs/cards/issues/prs.

> this process is what epiphany is about. you found clobber in another git, you
> did the archeology. epiphany is about doing all of that, except in an
> intuitive user interface, and cli. with way more post processing.

> This process is what calliope is about, except calliope is built around music
> and video production.

> it is what the "Muse" is/are/were. A process of iterative content review with
> agents. I've been circling it. The tool, foresight, promethean, eta mu. an
> ecosystem for describing these transductive workflows easily that binds well
> to peer interfaces that users and agents both use to best see what they are
> working on in a context.

> calliope is/should be built on top of what epiphany is. knoxx is then built
> using the whole suite.

> We describe versions of these workflows all over the place. I'm realizing the
> mental overload for me lately has just been I have a lot of these kinds of
> workflow ideas. I want tools that help me build the workflows. The reason we
> are doing so much extra work is that everything must be addressable by the
> system. Katamorph resources

> *my* deployment of knoxx, is a tool for spreading my ideas on bluesky, and
> discord. Composed of a mesh network of my machines. One cloud node as an entry
> point and general worker. up to 5 other devices floating in and out.

# Connections

Tiers follow Epiphany's ladder (`epiphany/PROCESS.md`). Nothing below is
*accepted*; acceptance is the user's explicit decision.

| Tier | Connection | Basis |
| --- | --- | --- |
| observed | The same session recovered `clobber` from `riatzukiza/devel@01d0f7200^` by git archaeology, then filed it as a consolidation input (open-hax/foresight#120) and a supervisor-IR epic (octave-commons/shx#2). | session 576342b7; `clobber/PROVENANCE.md` |
| observed | Epiphany already defines the promotion ladder observed → derived → provisional → accepted, an append-only inbox journal, and review decisions as events. | `epiphany/PROCESS.md`, `epiphany/docs/process/inbox.md`, `epiphany/src/epiphany/domain/review.clj` |
| observed | Calliope holds append-only ingestion truth and separates facts, proposals and decisions ("viewing a proposal must not count as accepting it"). | `docs/notes/mu-studio-archaeology-calliope-knoxx-epiphany.md` |
| observed | Foresight's working vocabulary already names the stages: α integrity, η transduction, μ evaluation, Π representation, with Katamorph between them and Clio remembering. | `AGENTS.md` "Working vocabulary"; `docs/architecture/workflows/alpha-eta-mu-pi.mmd` |
| derived | The PR flow captured as skills (`~/.agents/skills/pr-flow`) is one instance of that pipeline: muse (η input) → cards (α-checked artifacts) → review loops (μ) → merged product (Π), with receipts and spores as Clio-style memory. | structural comparison of `pr-flow/flow.edn` with the α/η/μ/Π diagram; method: manual reading |
| provisional | Layering: epiphany is the general process engine with UI + CLI; calliope specialises it for music/video; knoxx composes the whole suite as the user-facing product. | user statement above; not yet reflected in any roadmap |
| provisional | "Tools that help build the workflows" means workflow definitions themselves become Katamorph resources (like `pr-flow/flow.edn`), so they are addressable, validated and composable rather than restated in prose per repository. | user statement + the "everything must be addressable" line |
| provisional | The user's own Knoxx deployment target: publishing to Bluesky and Discord over a mesh of one cloud entry/worker node plus up to five intermittent devices. Relevant to the supervisor-IR epic (one host at a time today) and to `promethean-host-slotting`. | user statement |

# Open questions

- Should `pr-flow/flow.edn` become a Katamorph resource (a workflow contract) rather than a skill-local data file, so Epiphany and Knoxx can read it?
- Which surface is the "peer interface" for this process first: Rheos board UI, Epiphany CLI, or Knoxx?
