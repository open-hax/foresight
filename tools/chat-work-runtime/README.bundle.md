# Foresight chat-work runtime

Relocatable Linux tools for small Foresight jobs. The bundle includes Node.js,
npm/npx, NBB, Babashka, clj-kondo, and jscpd. It intentionally excludes a JRE
and JVM Clojure; those belong in a larger component-specific runtime.

Extract the archive and either invoke tools directly:

```sh
./foresight-chat-work-linux-x64/bin/nbb --version
```

or add them to the current shell:

```sh
export PATH="$PWD/foresight-chat-work-linux-x64/bin:$PATH"
nbb scripts/project.clj validate
```

The bundle may be moved after extraction. It requires a 64-bit Linux kernel and
glibc compatible with the bundled Node.js release. Babashka and the x64
clj-kondo executable are statically linked.

Verify the download with the adjacent `.sha256` file. After extraction, verify
regular-file contents, the complete entry/type set, symbolic-link targets, and
required executable modes with:

```sh
./foresight-chat-work-linux-x64/bin/verify-integrity
```

The verifier uses the host's `cmp`, `find`, `mktemp`, `readlink`, `sha256sum`,
`sort`, and `stat` commands. Node-based entry points always resolve the bundled
Node executable, including when invoked directly without adding the bundle to
`PATH`. NBB, jscpd, and
their transitive npm packages were installed from the committed lockfile
included under `lib/chat-work-tools/`.

The Foresight runtime packaging is GPL-3.0-or-later, and reusable Foresight
library code is LGPL-3.0-or-later. Complete license texts are under
`share/licenses/`, including version-bound third-party notices and terms for
Babashka, clj-kondo, rewrite-clj, and jscpd. Publisher-retained licenses remain
inside the Node.js, npm, NBB, jscpd, and import-meta-resolve component trees.
