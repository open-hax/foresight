# Join native generation before disposing the model

Browser cleanup exposed a real lifecycle defect: closing HTTP sockets did not
mean the underlying ONNX generation had settled. The service could dispose its
model while a disconnected request still used it. A connected request also
prevented shutdown from reaching the interruption step.

Generation admission now retains a completion promise. Closing the service
first refuses new admission and interrupts the active generation, then joins
both HTTP shutdown and that completion before disposing the model. Repeated
close calls share one cleanup promise and dispose once. An already accepted
partial HTTP request cannot start inference after closing begins.

The native regressions execute the pinned SmolLM2 model. They hold only the
completion boundary after real inference, observe actual disposal ordering,
and preserve returned tokens. No generated response is substituted. Two
initial lifecycle assertions failed on the old source; an isolated disposal
regression independently reproduced the premature disposal.

The final combined suite passed **88 tests, zero failures or skips** on
Node24.20.0, including all 85 existing model HTTP/codec cases. Strict devtools
lint reports zero errors or warnings. The root command includes all three new
native regressions through the child-owned manifest:

```sh
pnpm test:model-http
pnpm manifests:check
```

The HTTP request accepts token limits from 1 to 4096 for protocol compatibility.
The local service configuration permits a resource cap from 1 to 1024; generation
uses the smaller of the requested limit and this configured cap. The default
service cap is 512. Request admission does not promise that many generated tokens.

This fixes cleanup correctness. It does not explain the separate browser20 MCP
timeout: that request returned after 63.8 seconds, beyond the SDK's 60-second
deadline. Both failed browser attempts and their annotated screenshots remain
preserved; the two publishing cycles still require a successful browser run.
