# JMH microbenchmark overview

Experiments: **2026-10-28 to 2026-10-30**. Reports reviewed: **2026-10-30**.

We evaluated six code changes using seven JMH benchmarks. Five changes reduced
execution time in specific tested scenarios. These results concern individual Java operations, not the performance
of the whole application.

## 1. Why we chose these operations

We selected operations with visible repeated work: temporary collections,
repeated path construction, per-byte string conversion, eager default-object
creation, and repeated message formatting inside a loop.

Each experiment tested a specific hypothesis about avoiding that work. The
selection came from code inspection, not profiling of a real user workload.

## 2. What the benchmarks measure

There are **34 input combinations per implementation**, distributed across
the seven benchmark classes:

| Benchmark | Inputs | One measured operation |
| --- | --- | --- |
| `CategoryMapperBenchmark` | 10, 100, 1000 categories | Convert the whole list to simple DTOs |
| `CategoryDetailedMapperBenchmark` | 0, 10, 100, 1000 children | Convert one category and its children |
| `ImageMapperBenchmark` | 10, 100, 1000 images | Convert the whole batch to DTOs with URLs |
| `RevisionMapperBenchmark` | 10, 100, 1000 revisions; 0, 1, 2 images each | Convert the whole batch to card DTOs |
| `SimpleImageServiceBenchmark` | One fixed 32-byte input | Convert the bytes to a 64-character hexadecimal string |
| `SimpleLearnServiceBenchmark` | 3 progress states and 3 answers | Reset the fixture and process one in-memory attempt |
| `SimpleDeckServiceBenchmark` | 0, 1, 10, 100, 1000 cards | Create a destination and copy the whole deck in memory |

Input preparation is outside measurement.
The same benchmark approach was used before and after each change.

## 3. Common measurement procedure

The saved JSON files record the same JMH/JVM configuration:

| Setting | Value |
| --- | --- |
| JMH / JDK | 1.37 / Temurin 11.0.32.1 |
| Recorded machine | Apple M3, 16 GiB RAM, macOS 27.0.1, arm64 |
| Mode / unit | Average time / nanoseconds per operation (`ns/op`) |
| Worker threads | 1 |
| JVM forks per input case and run | 2 |
| Warmup per fork | 3 iterations of 1 second |
| Measurement per fork | 5 iterations of 1 second |
| Additional JVM arguments | None |

We saved two complete runs per version for every benchmark except the simple
category mapper, which has one run per version. A run contains ten measurement
samples per input case: five from each fork. Warmup samples are excluded.

The comparison tables average the complete run scores for each version,
except the simple category table, which shows its single run scores.
Percentages use unrounded values:

```text
Time reduction (%) = 100 × (before − after) / before
```

A negative reduction means a longer measured time. These averages are
descriptive summaries.

## 4. Results across the experiments

Each linked report contains the full tables, original JSON files, and commands.

| Report | Change | Main result |
| --- | --- | --- |
| [Categories](category-mapper/category-mapper-comparison.md) | Map children directly without a temporary category list | Detailed mapping time reduced by 14.37–60.71%, depending on child count. Simple mapping showed no clear change in its single run per version. |
| [Image URLs](image-mapper/image-mapper-comparison.md) | Cache the normalized URL prefix | Batch mapping time reduced by 52.39–58.01% for the tested filenames. |
| [Revision mapping](revision-mapper/revision-mapper-comparison.md) | Cache the normalized image URL prefix | Roughly 50–55% lower time with images. The detailed report explains the variable baseline case. No clear change without images. |
| [Hex conversion](simple-image-service/bytes-to-hex-comparison.md) | Fill a character array using a hexadecimal lookup table | Mean conversion time reduced by 88.90%, from 243.94 to 27.08 ns. |
| [Learning attempts](simple-learn-service/save-attempt-comparison.md) | Create default progress with `orElseGet` | No consistent improvement. Descriptive reductions range from -3.49% to 5.28% across the nine cases. |
| [Deck copying](simple-deck-service/deck-copy-comparison.md) | Build one message with concatenation before copying cards | Time reduced by 67.70–77.68% for non-empty decks. Empty decks rose from 55.08 to 61.07 ns, an increase of 5.99 ns. |

## 5. Correctness and report review

JUnit checks behavior; JMH measures execution time. The latest full code build
passed **457 JUnit tests**, with no failures, errors, or skipped tests.
Smaller counts in earlier reports describe the builds at those stages, before
later test cases were added; they are not conflicting totals for the same build.

## 6. Reproduction benchmark

Use JDK 11 and the commands in the individual reports or the
[benchmark guide](../../../benchmarks/README.md). Rebuild after selecting the
intended production implementation, keep the benchmark definition unchanged,
and preserve new JSON and log files under unused run numbers. Selecting a
baseline or improvement output directory does not select a code version.

The current experiments provide evidence about the selected local changes.
