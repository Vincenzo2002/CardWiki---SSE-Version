# Image mapper: initial JMH measurements

Date: **2026-10-29**, Europe/Rome.

We measured the current `ImageMapper` before changing its URL construction.
Two complete runs were saved to establish a reference for the next experiment.

## What the benchmark measures

[`ImageMapperBenchmark.mapImages`](../../../../benchmarks/src/main/java/at/ac/tuwien/sepm/groupphase/benchmarks/ImageMapperBenchmark.java)
converts a batch of 10, 100, or 1000 `Image` objects to DTOs. Each operation
includes creating the output list, creating the DTOs, and building their URLs
with the existing `ImageMapper` implementation.

Input objects are prepared in `@Setup`, outside the measurement. Filenames
contain 64 hexadecimal characters followed by `.png`; the URL prefix is
`/images`, matching the application configuration. Spring, the database, and
image file contents are not involved. Reflection is used only during setup
to configure the mapper's prefix.

## Configuration

| Setting | Value |
| --- | --- |
| JMH / JDK | 1.37 / Temurin 11.0.32.1 |
| Machine | Apple M3, 16 GiB RAM, macOS 27.0.1, arm64 |
| Mode | Average time, in nanoseconds per batch (`ns/op`) |
| Worker threads | 1 |
| JVM forks per input case | 2 |
| Warmup per fork | 3 iterations of 1 second |
| Measurement per fork | 5 iterations of 1 second |
| Additional JVM arguments | None |

Both runs used the same JAR, source files, and configuration. Each case has
10 measurement values per run: 5 from each fork. Background workload and power
conditions were not recorded.

## Results

Cells show **JMH score ± scoreError** in ns per whole batch. A result for
100 images is the time for all 100 conversions, not the time for one image.

| Images | Run 01 (ns/op) | Run 02 (ns/op) |
| --- | ---: | ---: |
| 10 | 2001.294 ± 39.319 | 1874.069 ± 449.747 |
| 100 | 18337.684 ± 2795.012 | 16938.001 ± 339.062 |
| 1000 | 151417.482 ± 9938.333 | 145804.340 ± 1136.016 |

The run means differ by approximately 6.36%, 7.63%, and 3.71% for 10, 100,
and 1000 images respectively. Some cases also show substantial variation
between forks. Keep both runs when comparing with an optimized version;
small differences should not be interpreted as improvements without further
measurements. No optimization has been measured yet.

Original outputs:

- Run 01: [JSON](image-mapper-run01.json), [log](image-mapper-run01.log).
- Run 02: [JSON](image-mapper-run02.json), [log](image-mapper-run02.log).

## Code and build

The base commit is `2f91cd157ead84763f24be947410f9fe97b71bba` on
`testing/microbenchmarkingJMH`, with the new benchmark added locally.
The production `ImageMapper` matches that commit. This reference already
includes the earlier category mapper optimization.

The JAR was rebuilt with JDK 11 using:

```sh
mvn -o -pl benchmarks -am clean package -DskipTests
```

The build succeeded. This build skipped JUnit execution because the production
code was unchanged. The earlier short smoke run was separate from these
full measurements.

SHA-256 values captured before measurement and checked again afterwards:

| Artifact | SHA-256 |
| --- | --- |
| Benchmark source | `fea2c6066fae08817bb6deab596f1feffc05eab1d08f6c59f1398019a189b1ed` |
| Production `ImageMapper.java` | `b9b89b9ea58777451803738a6d43a48dc4b543054516d50187b3e774a0805d71` |
| Measured JAR | `42b3d30c056ae5f5fab772c6559e3f8603e4336db1c4013fbfbcbe34ee67c172` |

The JAR is generated under `benchmarks/target/` and is not archived here.

## Measurement command

Run from the repository root with `JAVA_HOME` pointing to JDK 11:

```sh
mkdir -p sse-reports/baseline/jmh/image-mapper

"$JAVA_HOME/bin/java" -jar benchmarks/target/benchmarks.jar \
  'ImageMapperBenchmark.mapImages' \
  -t 1 -foe true -rf json \
  -rff sse-reports/baseline/jmh/image-mapper/image-mapper-run01.json \
  -o sse-reports/baseline/jmh/image-mapper/image-mapper-run01.log
```

Run 02 used the same command with `run02` in both filenames. Use new filenames
for additional runs to preserve the archived results. The Java executable
path is expressed through `JAVA_HOME` for portability; the JSON files contain
the actual path recorded by JMH.

These results measure mapping time only. They do not measure complete image
uploads, HTTP latency, database access, or energy consumption.
