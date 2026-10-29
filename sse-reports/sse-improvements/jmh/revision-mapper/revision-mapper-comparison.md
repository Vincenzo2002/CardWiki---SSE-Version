# Revision mapper optimization with JMH

Date: **2026-10-29**.

We compared revision mapping before and after caching image URL prefixes.
Both optimized runs were faster when revisions contained images. Cases
without images showed small differences compatible with measurement noise.

## 1. What we changed

[`RevisionMapper.prefixImageServedPath`](../../../../backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/endpoint/mapper/RevisionMapper.java)
originally built a filesystem path for every non-null image filename:

```java
return Paths.get(imageServedPath, filename)
    .toString()
    .replace('\\', '/');
```

The new implementation caches the normalized prefix for each configuration
value and combines it with simple ASCII filenames on macOS and Linux:

```java
String prefix = urlPrefixes.computeIfAbsent(
    imageServedPath, RevisionMapper::buildUrlPrefix);
return prefix + filename;
```

Null filenames still return `null`. The original conversion remains in use
for special cases, including empty or nested filenames, and on Windows.
The cache is thread-safe and uses the same approach as `ImageMapper`.

## 2. How we tested it

[`RevisionMapperBenchmark.mapRevisions`](../../../../benchmarks/src/main/java/at/ac/tuwien/sepm/groupphase/benchmarks/RevisionMapperBenchmark.java)
calls `revisionEditToCardSimpleDto` for batches of 10, 100, and 1000 revisions.
Each revision has zero, one, or two images, giving nine input combinations.
With one image, only the front image is present.

Texts remain fixed, filenames contain 64 hexadecimal characters followed by
`.png`, and the URL prefix is `/images`. Setup creates the input objects and
configures the mapper outside the timed operation. No Spring context,
database, or image files are used.

| Setting | Value |
| --- | --- |
| JMH / JDK | 1.37 / Temurin 11.0.32.1 |
| Machine | Apple M3, 16 GiB RAM, macOS 27.0.1, arm64 |
| Mode | Average time, in nanoseconds per whole batch (`ns/op`) |
| Threads | 1 |
| JVM forks per input case | 2 |
| Warmup per fork | 3 iterations of 1 second |
| Measurement per fork | 5 iterations of 1 second |

We used two complete runs per version with the same recorded configuration.
The benchmark source remained unchanged, and both optimized runs used the
same rebuilt JAR. The cache is initialized during warmup, so the measured
times describe repeated mapping rather than first-call initialization.

After the change, **all 398 JUnit tests passed**, including the 18 cases in
[`RevisionMappingTest`](../../../../backend/src/test/java/at/ac/tuwien/sepm/groupphase/backend/unittests/RevisionMappingTest.java).
These check the card ID, texts, optional images, null values, slash handling,
successive mappings, and prefix changes.

## 3. Results

The table averages the two complete run scores for each version. It is a
descriptive summary; individual results and JMH error estimates are available
in the linked JSON files. Each operation converts the whole batch.

| Revisions | Images per revision | Before (ns/op) | After (ns/op) | Time reduction |
| --- | --- | ---: | ---: | ---: |
| 10 | 0 | 72.33 | 73.16 | -1.14% |
| 100 | 0 | 685.15 | 686.11 | -0.14% |
| 1000 | 0 | 7306.36 | 7362.03 | -0.76% |
| 10 | 1 | 1819.69 | 858.89 | 52.80% |
| 100 | 1 | 18210.53 | 8390.80 | 53.92% |
| 1000 | 1 | 153438.03 | 71681.56 | 53.28% |
| 10 | 2 | 3723.16 | 1781.87 | 52.14% |
| 100 | 2 | 54981.84 | 17870.55 | 67.50% |
| 1000 | 2 | 290160.72 | 131791.27 | 54.58% |

The percentage is calculated from unrounded values as:

```text
100 × (before − after) / before
```

A negative percentage means a longer measured time. For cases without
images, the confidence intervals overlap in all baseline/optimized run
comparisons, so the small changes do not show a clear regression.

For cases with images, both optimized runs have confidence intervals below
those of both baseline runs. The optimized run scores differ by less than
1.2% for each input combination.

### The variable baseline case

The complete baseline runs were particularly variable for **100 revisions
with two images**. Their average produces the 67.50% reduction in the table,
which should not be treated as a precise estimate of the gain.

A separate [targeted baseline check](../../../baseline/jmh/revision-mapper/revision-mapper-size100-images2-check01.json)
measured **35606.43 ± 480.15 ns/op** with the same JMH settings. Compared with
that check, the optimized average of **17870.55 ns/op** represents a
**49.81% reduction**. This check is kept separate from the complete-run averages.

The results therefore support a reduction of roughly **50–55%** in the tested
image-containing cases, while acknowledging the noisier baseline measurement.

Original complete results:

- Before: [run 01](../../../baseline/jmh/revision-mapper/revision-mapper-run01.json),
  [run 02](../../../baseline/jmh/revision-mapper/revision-mapper-run02.json).
- After: [run 01](revision-mapper-run01.json), [run 02](revision-mapper-run02.json).
- Optimized execution logs: [run 01](revision-mapper-run01.log),
  [run 02](revision-mapper-run02.log).

## 4. How to repeat the experiment

With JDK 11 selected, run from the repository root:

```sh
mvn clean package

mkdir -p sse-reports/sse-improvements/jmh/revision-mapper
set -o pipefail

java -jar benchmarks/target/benchmarks.jar \
  'RevisionMapperBenchmark.mapRevisions' \
  -t 1 -foe true -rf json \
  -rff sse-reports/sse-improvements/jmh/revision-mapper/revision-mapper-run03.json \
  2>&1 | tee sse-reports/sse-improvements/jmh/revision-mapper/revision-mapper-run03.log
```

The benchmark annotations supply the nine cases and the settings listed
above. Use new filenames for additional runs. These optimized measurements
used `-o` to write the log directly; the example uses `tee` to also show it
in the terminal.

For baseline measurements, restore the original implementation, rebuild,
and use the directory `sse-reports/baseline/jmh/revision-mapper/`. Changing
the output directory does not select a code version.

## 5. Conclusion and limitations

Caching URL prefixes reduced repeated revision mapping time when images were
present. The no-image cases remained compatible with unchanged performance,
and the full JUnit suite passed.

The measurements cover synthetic PNG filenames, a warm cache, and one thread
on macOS. They do not measure Windows performance, fallback cases, concurrent
request contention, or first-call cost.