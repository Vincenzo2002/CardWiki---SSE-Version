# Deck copy optimization with JMH

Date: **2026-10-30**.

We compared `SimpleDeckService.copy` before and after changing how the copy
message is built. The measured time decreased by about **68–78% for non-empty
decks**. Empty decks were about **6 ns slower**, so the change did not improve
every case.

See the [JMH overview](../jmh-overview.md) for shared methodology, limitations,
and the results of the other experiments.

## 1. What we changed

[`SimpleDeckService.copy`](../../../../backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/SimpleDeckService.java)
originally formatted the same message for every copied card:

```java
rev.setMessage(String.format("Copied from deck %s.", id));
```

The final implementation builds the message once, before the per-card mapping:

```java
String copyMessage = "Copied from deck " + id + ".";
```

Each new revision then receives `copyMessage`. This both removes repeated
message construction and replaces general-purpose formatting with string
concatenation. For the non-null `Long` source ID used by the service, the
resulting text is unchanged. Card contents, image references, categories,
authors, and repository operations keep the same behavior.

## 2. How we tested it

[`SimpleDeckServiceBenchmark.copyDeck`](../../../../benchmarks/src/main/java/at/ac/tuwien/sepm/groupphase/benchmarks/SimpleDeckServiceBenchmark.java)
calls the real service method for **0, 1, 10, 100, and 1000 source cards**.
One operation creates a new destination and copies the whole deck in memory.

The source deck has ID 42, author ID 2, distinct front/back texts, two shared
image references per revision, and no categories. The current user has ID 1.
Source entities and JDK proxy stubs are prepared in `@Setup`. Each lookup
returns a fresh stream and each operation uses a new destination, so repeated
invocations do not reuse exhausted streams or accumulate copied cards.

The measurement includes destination creation, proxy calls, stream creation,
card/revision construction, messages, and output collections. It does not
include Spring, actual authentication, SQL, generated IDs, persistence
callbacks, real flushing, transactions, image-file copying, or HTTP handling.

| Setting | Value |
| --- | --- |
| JMH / JDK | 1.37 / Temurin 11.0.32.1 |
| Machine | Apple M3, 16 GiB RAM, macOS 27.0.1, arm64 |
| Mode | Average time, nanoseconds per complete deck copy (`ns/op`) |
| Threads | 1 |
| JVM forks per size | 2 |
| Warmup per fork | 3 iterations of 1 second |
| Measurement per fork | 5 iterations of 1 second |
| Complete runs per version | 2 |

The 18 cases in
[`DeckCopyTest`](../../../../backend/src/test/java/at/ac/tuwien/sepm/groupphase/backend/unittests/DeckCopyTest.java)
passed on the original and final implementations. They check all five sizes,
independent card/revision objects, texts, optional image references, category
links, authors, exact messages for different IDs, successive copies, source
card preservation, and rejected calls with missing users, missing decks, or
null arguments. After the final change, the full build passed **457 JUnit
tests**, with no failures, errors, or skipped tests.

## 3. Results

The table averages the two complete run scores for each version. It describes
**one whole in-memory deck copy**, not one card. Lower times are better.

| Cards | Before (ns/op) | After (ns/op) | Time reduction |
| --- | ---: | ---: | ---: |
| 0 | 55.08 | 61.07 | -10.88% |
| 1 | 445.43 | 143.86 | 67.70% |
| 10 | 3614.10 | 806.54 | 77.68% |
| 100 | 37643.30 | 9380.87 | 75.08% |
| 1000 | 381515.58 | 92134.39 | 75.85% |

The percentage is calculated from unrounded values:

```text
100 × (before − after) / before
```

Both final optimized runs were faster than both baseline runs for every
non-empty size. Their JMH confidence intervals were also below those of both
baseline runs. Final run scores differed by at most about 3.32% of their mean.

For 1000 cards, the mean time decreased from about **381.52 to 92.13
microseconds per deck**, saving about **289.38 microseconds**.

Original results and logs:

- Before: [run 01 JSON](../../../baseline/jmh/simple-deck-service/deck-copy-run01.json)
  and [log](../../../baseline/jmh/simple-deck-service/deck-copy-run01.log);
  [run 02 JSON](../../../baseline/jmh/simple-deck-service/deck-copy-run02.json)
  and [log](../../../baseline/jmh/simple-deck-service/deck-copy-run02.log).
- After: [run 01 JSON](deck-copy-run01.json) and [log](deck-copy-run01.log);
  [run 02 JSON](deck-copy-run02.json) and [log](deck-copy-run02.log).

## 4. How to repeat the experiment

With JDK 11 selected, run from the repository root:

```sh
mvn clean package

mkdir -p sse-reports/sse-improvements/jmh/simple-deck-service
set -o pipefail

java -jar benchmarks/target/benchmarks.jar \
  'SimpleDeckServiceBenchmark.copyDeck' \
  -t 1 -foe true -rf json \
  -rff sse-reports/sse-improvements/jmh/simple-deck-service/deck-copy-run03.json \
  2>&1 | tee sse-reports/sse-improvements/jmh/simple-deck-service/deck-copy-run03.log
```

The annotations supply all five sizes and the settings above. Use new
filenames for additional runs. Recorded optimized measurements used `-o`
to save logs directly; this example uses `tee` to also display progress.

For the baseline, restore the original per-card `String.format` expression,
remove the new shared message variable, rebuild, and use
`sse-reports/baseline/jmh/simple-deck-service/`. Changing the output directory
does not select a code version. See the
[benchmark guide](../../../../benchmarks/README.md#copying-decks) for details.

## 5. Conclusion and limitations

Building one copy message with concatenation reduced measured copy time for
the tested non-empty decks while preserving the behavior checked by JUnit.
The small absolute slowdown for empty decks remains a tradeoff.
