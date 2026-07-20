# InputStream Processor Core

[![CI](https://github.com/j-util/inputstream-processor-core/actions/workflows/ci.yml/badge.svg)](https://github.com/j-util/inputstream-processor-core/actions/workflows/ci.yml)

A tiny, format-neutral Java core for incremental item processing from
`InputStream`. The core has no third-party runtime dependencies.

## Requirements and installation

InputStream Processor Core requires Java 8 or later.

```xml
<dependency>
    <groupId>io.github.j-util</groupId>
    <artifactId>inputstream-processor-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Responsibilities

The API separates three responsibilities:

* An `InputParser<T>` interprets an input format and incrementally emits logical
  items.
* An `InputStreamProcessor<T>` connects the parser to the client consumer and
  counts consumer calls that return successfully.
* A client `Consumer<? super T>` handles each item and owns the application's
  failure policy.

Every emitted item, including `null`, is forwarded unchanged. An item is counted
when its client consumer call returns normally, including a `null` item.

The core does not catch, wrap, or classify parser or consumer exceptions; they
propagate unchanged and terminate `process(...)`. Consumer calls completed
before a later failure remain completed, with no rollback. When `process(...)`
throws, no `ProcessingResult` is returned. Parsers and consumers can handle
their own recoverable failures if processing should continue.

## Execution and concurrency

Core V1 processing is synchronous and blocking. A parser emits items during its
`parse(...)` call, so all emissions occur before `process(...)` returns.
Asynchronous and reactive processing are outside this core artifact.

`InputStreamProcessor` is immutable, but safe concurrent reuse depends on the
configured parser being thread-safe. Separate processing calls must use separate
`InputStream` instances; the processor does not add synchronization.

## JDK-only example

```java
import io.github.jutil.inputstreamprocessor.core.InputParser;
import io.github.jutil.inputstreamprocessor.core.InputStreamProcessor;
import io.github.jutil.inputstreamprocessor.core.ProcessingResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

Path path = Paths.get("input.txt");

InputParser<String> parser = (input, emit) -> {
    BufferedReader reader = new BufferedReader(
            new InputStreamReader(input, StandardCharsets.UTF_8)
    );
    String line;

    while ((line = reader.readLine()) != null) {
        emit.accept(line);
    }
};

InputStreamProcessor<String> processor = new InputStreamProcessor<>(parser);

try (InputStream input = Files.newInputStream(path)) {
    ProcessingResult result = processor.process(input, System.out::println);
    System.out.println("Processed: " + result.getProcessedCount());
}
```

The caller owns the supplied `InputStream`. The processor never closes it,
including when parsing or consumption terminates exceptionally. Parser
implementations must also leave it open. The caller should close the stream
after processing, as shown above. In particular, the example intentionally does
not close the `BufferedReader`, because doing so would also close the
caller-owned stream.

The core itself does not materialize the complete input or all emitted items.
Incremental, bounded-memory behavior depends on the parser reading and emitting
incrementally; the core cannot prevent a parser implementation from buffering
the complete input or all parsed items.

JSON, CSV, and XML integrations are intentionally outside this dependency-free
core. Applications can implement `InputParser<T>` using whichever format
library they choose.
