# Image hash conversion optimization with JMH

Date: **2026-10-29**.

We used JMH to compare `SimpleImageService.bytesToHex` before and after
changing how the hexadecimal string is built. The average measured time
decreased by **88.90%**, from **243.94 to 27.08 ns per conversion**.

## 1. What we changed

[`SimpleImageService.bytesToHex`](../../../../backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/SimpleImageService.java)
converts an image's SHA-256 hash into the hexadecimal part of its filename.
Each byte becomes two lowercase hexadecimal characters.

The original implementation called `Integer.toHexString` for every byte,
added a leading zero when needed, and appended the result to a `StringBuffer`.

The new implementation uses a shared lookup table containing
`0123456789abcdef` and fills a character array directly:

```java
char[] hex = new char[hash.length * 2];
for (int i = 0; i < hash.length; i++) {
    int value = hash[i] & 0xff;
    hex[i * 2] = HEX_DIGITS[value >>> 4];
    hex[i * 2 + 1] = HEX_DIGITS[value & 0x0f];
}
return new String(hex);
```

This avoids creating a temporary string for each byte. Leading zeros,
lowercase letters, and the handling of empty and null inputs remain unchanged.
The method is still private.

## 2. How we tested it

[`SimpleImageServiceBenchmark.bytesToHex`](../../../../benchmarks/src/main/java/at/ac/tuwien/sepm/groupphase/benchmarks/SimpleImageServiceBenchmark.java)
converts a **32-byte array into a 64-character string**, matching the SHA-256
hash length used by the application. One operation is one complete conversion.

`@Setup` creates the service without Spring and prepares the bytes using
`new Random(42)`, so both versions receive the same input. The benchmark does
not calculate SHA-256, access the database, or read or write image files.

A static final `MethodHandle` provides access to the private method. Access
is resolved outside measurement; any invocation overhead remaining after JIT
optimization is included. The benchmark source and invocation approach were
unchanged between versions, and both optimized runs used the same rebuilt JAR.

| Setting | Value |
| --- | --- |
| JMH / JDK | 1.37 / Temurin 11.0.32.1 |
| Operating system / architecture | macOS 27.0.1 / arm64 |
| Mode | Average time, in nanoseconds per conversion (`ns/op`) |
| Threads | 1 |
| JVM forks per run | 2 |
| Warmup per fork | 3 iterations of 1 second |
| Measurement per fork | 5 iterations of 1 second |
| Complete runs per version | 2 |

The measurements used local changes based on commit `8e6056d`. The baseline
used the original conversion method; the optimized version used the character
array implementation above. The SHA-256 checksum of the optimized JAR used
for both runs was:

```text
fb16887a4e73e3f4f86a6ffad3eca1af95375ccb7cd02be52acc09b095ad573f
```

The 15 cases in
[`SimpleImageServiceTest`](../../../../backend/src/test/java/at/ac/tuwien/sepm/groupphase/backend/unittests/SimpleImageServiceTest.java)
passed on both implementations. They cover all 256 byte values, leading zeros,
lowercase output, a known SHA-256 hash, empty and null inputs, unchanged input
arrays, and successive conversions. After the change, the full build passed
with **413 JUnit tests**, no failures, errors, or skipped tests.

## 3. Results

Lower times are better. The mean row averages the two complete run scores
for each version; it does not combine their error estimates.

| Measurement | Before (ns/op) | After (ns/op) |
| --- | ---: | ---: |
| Run 01 | 248.59 | 27.02 |
| Run 02 | 239.30 | 27.13 |
| **Mean** | **243.94** | **27.08** |

Using unrounded values:

```text
Time reduction = 100 × (before − after) / before = 88.90%
Speedup = before / after = 9.01×
```

Both optimized runs were faster than both baseline runs, and their JMH
confidence intervals were below those of both baseline runs. The two
optimized scores differed by **0.42% of their mean**. The baseline showed
more variability, so the exact improvement may vary between experiments.

Original results and execution logs:

- Before: [run 01 JSON](../../../baseline/jmh/simple-image-service/bytes-to-hex-run01.json)
  and [log](../../../baseline/jmh/simple-image-service/bytes-to-hex-run01.log);
  [run 02 JSON](../../../baseline/jmh/simple-image-service/bytes-to-hex-run02.json)
  and [log](../../../baseline/jmh/simple-image-service/bytes-to-hex-run02.log).
- After: [run 01 JSON](bytes-to-hex-run01.json) and [log](bytes-to-hex-run01.log);
  [run 02 JSON](bytes-to-hex-run02.json) and [log](bytes-to-hex-run02.log).

## 4. How to repeat the experiment

Select JDK 11, then run from the repository root:

```sh
mvn clean package

mkdir -p sse-reports/sse-improvements/jmh/simple-image-service
set -o pipefail

java -jar benchmarks/target/benchmarks.jar \
  'SimpleImageServiceBenchmark.bytesToHex' \
  -t 1 -foe true -rf json \
  -rff sse-reports/sse-improvements/jmh/simple-image-service/bytes-to-hex-run03.json \
  2>&1 | tee sse-reports/sse-improvements/jmh/simple-image-service/bytes-to-hex-run03.log
```

The annotations supply the warmup, measurement, and fork settings listed above.
Use new filenames for additional runs. The recorded optimized measurements
used `-o` to save logs directly; this example uses `tee` to also display progress.

For baseline measurements, restore the original method, rebuild, and save
outputs under `sse-reports/baseline/jmh/simple-image-service/`. Changing the
output directory does not select a code version. See the
[benchmark guide](../../../../benchmarks/README.md) for JDK setup.

## 5. Conclusion and limitations

The character array implementation reduced the measured conversion time by
about **89%**, while all correctness tests passed.

These measurements cover repeated conversion of one fixed 32-byte input,
with one thread on macOS after JVM warmup. They measure only `bytesToHex`,
so they do not imply that a complete image upload becomes nine times faster.
