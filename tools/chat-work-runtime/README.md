# Chat-work runtime archive

Build a pinned, relocatable toolkit for small Foresight jobs:

```sh
tools/chat-work-runtime/build.sh
```

The default output is `dist/foresight-chat-work-linux-x64.tar.gz` plus its
SHA-256 file. Cross-build ARM64 with `ARCH=arm64`; all downloaded native
executables match the requested architecture, so the build host need not run
them. The final smoke test must run on the target architecture.

Version pins live in `versions.env`. Every upstream archive is verified against
its publisher's SHA-256 checksum before use. `npm` installs exact versions into
the staged Node prefix. The archive contains its own full-file checksum
manifest.

The archive covers root Foresight NBB tasks and its standard lint/duplication
tools. It does not cover `eta` or `alpha` JVM tasks.
