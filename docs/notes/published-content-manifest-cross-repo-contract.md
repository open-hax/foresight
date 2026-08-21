---
title: "Published content manifest — the cross-repo contract"
summary: "Fixes the boundary shared by the three repositories in knoxx-translated-publication-to-website: the artifact Knoxx materializes, the manifest that commits it, the content root Services declares, and the reader expectation the website asserts. EDN, namespaced keys, manifest-is-the-published-fact, empty root is valid."
category: "architecture"
created: "2026-08-21"
---

# Published content manifest — the cross-repo contract

Three repositories meet at one directory and never share a compile:

```text
knoxx     writes artifacts and the manifest      (CLJS, effect boundary)
services  declares where that directory is       (deployment, host contract)
website   reads the manifest and renders          (CLJS, browser)
```

The recorded diagnosis behind Knoxx's layer-enforcement work applies directly:
a writer and a reader that only run together against live infrastructure drift
unseen. This note fixes the boundary so each side can assert it alone. Each
repository declares **its own side** — the website states a reader expectation,
not a copy of the writer's schema — and this note is what they are both
declaring against.

Parent epic: `knoxx:kanban/epics/translated-publication-to-website.md`.

## Why EDN and not JSON

Both writer and reader are ClojureScript. JSON erases keyword namespaces, and
that erasure has already produced one live defect in this constellation: the
translation-config card records a PATCH validated against `:translation/model`
silently leaving the authoritative model unchanged, because `clj->js` in the
shared frontend request helper dropped the namespace and the validator accepted
an ignored extra key.

The manifest is read once at load with `fetch` + `cljs.reader/read-string`.
Namespaced keys survive, and the boundary stays Clojure-shaped, per Foresight's
`:foresight/host-values-stay-at-edge`.

Static servers do not know the `.edn` extension. The serving config **must**
declare `application/edn` for it; otherwise `octet-stream` is served, which
`fetch().text()` still reads correctly but which no human debugging in a browser
will believe.

## The content root

```text
<stateRoot>/website-content/            content root, single writer
├── manifest.edn                        the published fact
└── artifacts/
    └── <document>/<locale>/<revision>.<ext>
```

`<stateRoot>` is the host contract's `stateRoot` (`/srv/open-hax/state`), **not**
a service runtime path. Published content is state, not build output: a website
redeploy that `rsync --delete`s its docroot must not be able to erase published
translations. This is the whole reason the deployment model separates
`build.output` from `serve.docroot`.

Exactly one writer. The reader mounts it read-only.

## PublicationArtifact — what Knoxx hands the adapter

```clojure
{:artifact/content     "<!doctype html>…"   ; string or bytes
 :artifact/media-type  "text/html"
 :artifact/encoding    "utf-8"
 :artifact/locale      :es
 :artifact/revision    "rev-7f3a91c"}
```

Laws, all of which are the artifact-contract card's and are restated here only
because the reader depends on their consequences:

1. `:artifact/revision` **must equal** the op's `:concrete-revision`. A
   disagreement is a typed conflict carrying both revisions, not a warning: it
   means the renderer and the planner disagree about what is being published.
2. No selector keyword (`:source/current`) may appear anywhere in the artifact,
   for the same reason `publish-idempotency-key` refuses one — a selector gives a
   stable-looking identity to a moving target.
3. The artifact is produced **above** the effect boundary. One renderer, and
   adapters that only transport. Producing it below means every adapter renders
   independently and they diverge.
4. Validated in both directions at the boundary: the artifact before `publish!`,
   the returned receipt before a caller reads fields off it.

## The manifest

```clojure
{:manifest/version 1
 :manifest/generated-at "2026-08-21T18:04:11Z"
 :manifest/routes
 [{:route/path       "/es/notes/hello"
   :route/locale     :es
   :route/document   "notes/hello"
   :route/revision   "rev-7f3a91c"
   :route/artifact   "artifacts/notes/hello/es/rev-7f3a91c.html"
   :route/media-type "text/html"
   :route/encoding   "utf-8"
   :route/title      "Hola"
   :publication/id   "pub-0d41ab9c"}]}
```

### Required, per route

`:route/path`, `:route/locale`, `:route/artifact`, `:route/media-type`.
`:manifest/version` is required at the top level.

Everything else is optional. `:route/title` is what a listing renders;
`:publication/id` is what `observe!` keys on.

### Reader rules

| Input | Required behaviour |
| --- | --- |
| manifest absent | empty published set; site renders its own sections; **not** an error |
| `:manifest/routes` empty | same as absent |
| unknown field present | ignored, never fatal |
| required field missing | fails **loudly** — never a silently blank page |
| `:manifest/version` unsupported | fails loudly |
| `:route/revision` is a selector keyword | rejected |
| locale carries no routes | not offered in the language switcher |

The asymmetry is deliberate. An absent manifest is the normal state of every
deploy before the first publication, so it must serve. A malformed manifest is a
writer defect, and a reader that renders a blank page instead of failing turns a
writer defect into an invisible outage.

### Why the manifest is the commit point

A file on disk that no manifest entry names **is not public**. So the adapter
writes the artifact first and updates the manifest second, and the manifest
update is atomic — write beside, then rename. A static file server has no read
lock, so a reader must never be able to observe a half-written manifest.

The consequence the reader relies on: a crash between artifact write and manifest
update leaves nothing public, and the next reconciliation converges. Interrupted
mid-write, the manifest a reader sees is either wholly the old one or wholly the
new one.

## Locale routing

The default locale is `:en` and serves at `/`. Every other locale takes a path
prefix (`/es/…`). An unknown prefix resolves to the default rather than 404ing.
`lang` is set on the document element per rendered locale — the shell currently
hardcodes `lang="en"`.

## Two content sources, kept separate

The website has its own copy — hero, section headings, calls to action, the
asset galleries — and it also renders published documents. These are not the
same thing and the epic's non-goals are explicit that the site's own sections do
not migrate into Knoxx.

```text
the site's own copy   ->  compiled per-locale dictionaries in the website repo
published documents   ->  manifest + artifacts under the content root
```

Both are translated; only the second travels through the publication seam. The
first is what makes Law 6 — "an empty or absent content root is a valid state
that serves the site correctly" — mean something more than serving a blank page.

## The one thing the site must never do

No request from the site reaches a Knoxx origin. The seam is files, so the site
keeps serving what was last published when Knoxx is down. That is the entire
reason a static target was chosen as the first real one.
