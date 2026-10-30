# Image mapper optimization with JMH

Date: **2026-10-29**.

We used JMH to compare image mapping before and after changing how image URLs
are built. The optimized version was faster in both repeated runs, with an
average time reduction of about **52–58%** for the tested batch sizes.

See the [JMH overview](../jmh-overview.md) for shared methodology, limitations,
and the results of the other experiments.

## 1. What we changed

The method
[`ImageMapper.filenameToUrl`](../../../../backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/endpoint/mapper/ImageMapper.java)
originally created a filesystem path for every image and converted it into
a URL string:

```java
imageDto.setUrl(
    Paths.get(imageServedPath, imageDto.getFilename())
        .toString()
        .replace('\\', '/')
);
```

For simple ASCII filenames on macOS and Linux, the new implementation prepares
the URL prefix once per configuration value and stores it in a thread-safe
cache. Each following conversion retrieves the prefix and adds the filename:

```java
String prefix = urlPrefixes.computeIfAbsent(
    imageServedPath, ImageMapper::buildUrlPrefix);

imageDto.setUrl(prefix + filename);
```

The original path-based conversion remains in use for other inputs, such as
empty names, nested paths, and non-ASCII names, and on systems such as Windows.
These cases are not the target of this performance optimization.

## 2. How we tested it

[`ImageMapperBenchmark.mapImages`](../../../../benchmarks/src/main/java/at/ac/tuwien/sepm/groupphase/benchmarks/ImageMapperBenchmark.java)
converts batches of **10, 100, and 1000 images** into DTOs containing a filename
and URL. Filenames contain 64 hexadecimal characters followed by `.png`, and
the configured prefix is `/images`.

Inputs are prepared in `@Setup`, outside the timed operation. Each operation
includes creating the output list, creating all DTOs, and building their URLs.
The benchmark runs in memory without Spring, database access, or image files.
The benchmark source was unchanged between versions.

| Setting | Value |
| --- | --- |
| JMH / JDK | 1.37 / Temurin 11.0.32.1 |
| Machine | Apple M3, 16 GiB RAM, macOS 27.0.1, arm64 |
| Mode | Average time, in nanoseconds per batch (`ns/op`) |
| Threads | 1 |
| JVM forks | 2 separate JVM processes per input case |
| Warmup per fork | 3 iterations of 1 second |
| Measurement per fork | 5 iterations of 1 second |

We performed two runs per version with the same recorded JMH/JVM settings.
Warmup initializes the optimized mapper's cache, so the measurements describe
repeated mapping with a prepared prefix, rather than the first call.

After the change, **all 380 JUnit tests passed**, with no failures, errors, or
skipped tests. This includes the 12 cases in
[`ImageMappingTest`](../../../../backend/src/test/java/at/ac/tuwien/sepm/groupphase/backend/unittests/ImageMappingTest.java),
covering PNG/JPEG filenames, null images, slash handling, nested paths, and
empty or missing filenames.

## 3. Results

The table averages the two run scores for each version. Individual results
and their JMH error estimates are available in the JSON files linked below.

Each operation converts **the whole batch**, not a single image. Lower times
are better.

| Images | Before (ns/op) | After (ns/op) | Time reduction |
| --- | ---: | ---: | ---: |
| 10 | 1937.68 | 849.08 | 56.18% |
| 100 | 17637.84 | 8397.96 | 52.39% |
| 1000 | 148610.91 | 62404.27 | 58.01% |

Time reduction is calculated from the unrounded values as:

```text
100 × (before − after) / before
```

Both optimized runs were faster than both baseline runs for every input size.
Their JMH confidence intervals were also below those of both baseline runs.
The two optimized run scores differed by less than 1% for each batch size.

The baseline runs showed more variability, including some differences between
JVM forks. The improvement is larger than the observed variability, but the
percentages should still be read as results of this experiment rather than
fixed gains for every workload.

For 1000 images, the average time decreased from about **148.61 to 62.40
microseconds per batch**, saving about **86.21 microseconds**.

Original results:

- Before: [run 01](../../../baseline/jmh/image-mapper/image-mapper-run01.json),
  [run 02](../../../baseline/jmh/image-mapper/image-mapper-run02.json).
- After: [run 01](image-mapper-run01.json), [run 02](image-mapper-run02.json).

## 4. How to repeat the experiment

Select the intended implementation and use JDK 11. From the repository root,
build the project and run the benchmark:

```sh
mvn clean package

mkdir -p sse-reports/sse-improvements/jmh/image-mapper
set -o pipefail

java -jar benchmarks/target/benchmarks.jar \
  'ImageMapperBenchmark.mapImages' \
  -t 1 -foe true -rf json \
  -rff sse-reports/sse-improvements/jmh/image-mapper/image-mapper-run03.json \
  2>&1 | tee sse-reports/sse-improvements/jmh/image-mapper/image-mapper-run03.log
```

The benchmark annotations supply the batch sizes, warmup, measurement, and
fork settings listed above. Use new filenames for additional runs.

For the baseline, restore the original `ImageMapper`, rebuild, and save the
outputs under `sse-reports/baseline/jmh/image-mapper/`. Changing the output
directory does not select the code version. See the
[benchmark guide](../../../../benchmarks/README.md) for JDK setup.

## 5. Conclusion and limitations

Caching the URL prefix reduced the measured time of repeated image mapping
for these inputs. The improvement appeared in both optimized runs, while all
existing and new JUnit tests passed.

The performance measurements cover simple PNG filenames on macOS, one worker
thread, and a warm cache. They do not measure Windows performance, the fallback
cases, first-call initialization, or contention between concurrent requests.
