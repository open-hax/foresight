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
the payload from inside the bundle with `sha256sum -c SHA256SUMS`.
