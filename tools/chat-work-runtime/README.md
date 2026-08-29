# Chat-work runtime archive

Build a pinned, relocatable toolkit for small Foresight jobs:

```sh
tools/chat-work-runtime/build.sh
```

The default output is `dist/foresight-chat-work-linux-x64.tar.gz` plus its
SHA-256 file. Cross-build ARM64 with `ARCH=arm64`; all downloaded native
executables match the requested architecture, so the build host need not run
them. The final smoke test must run on the target architecture.

Native version pins live in `versions.env`; the NBB/jscpd direct and transitive
npm graph is committed in `package-lock.json` and installed with `npm ci`.
Every upstream archive is verified against its publisher's SHA-256 checksum
before use. npm's target OS, CPU, and libc selectors follow `ARCH`, and the
builder requires the matching locked jscpd native package. The archive contains
a regular-file checksum manifest, an explicit symbolic-link target manifest,
and a verifier for both. Node-based entry points are bundle-relative wrappers,
so direct invocation does not depend on a host Node executable or on first
changing `PATH`.

The archive covers root Foresight NBB tasks and its standard lint/duplication
tools. It does not cover `eta` or `alpha` JVM tasks.

The runtime packaging in this directory is GPL-3.0-or-later. Reusable Foresight
library code remains LGPL-3.0-or-later. Bundled third-party components retain
their upstream licenses.
