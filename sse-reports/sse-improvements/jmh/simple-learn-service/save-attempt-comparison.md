# Learning attempt comparison with JMH

Date: **2026-10-29**.

We used JMH to compare `SimpleLearnService.saveAttempt` before and after
changing the creation of its default `Progress` object. The tests passed,
but the measurements **do not show a consistent execution-time improvement**.

## 1. What we changed

[`SimpleLearnService.saveAttempt`](../../../../backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/SimpleLearnService.java)
updates a card's learning progress after an `AGAIN`, `GOOD`, or `EASY` answer.

The original code evaluated `new Progress(id)` even when the repository
returned an existing progress record:

```java
Progress progress = progressRepository.findById(id).orElse(new Progress(id));
```

The new code evaluates the constructor expression only when no record exists:

```java
Progress progress = progressRepository.findById(id)
    .orElseGet(() -> new Progress(id));
```

The learning algorithm, repository calls, and scheduling rules are unchanged.
This source-level change does not guarantee a measurable speedup: the JVM
may already eliminate the unused object in the original version. That possible
explanation was not investigated in this timing experiment.

## 2. How we tested it

[`SimpleLearnServiceBenchmark.saveAttempt`](../../../../benchmarks/src/main/java/at/ac/tuwien/sepm/groupphase/benchmarks/SimpleLearnServiceBenchmark.java)
calls the real service method with in-memory substitutes for the current user
and progress repository. It covers nine combinations:

| Initial progress | Answers | Initial interval | Easiness factor |
| --- | --- | ---: | ---: |
| `NEW`: no record found | `AGAIN`, `GOOD`, `EASY` | New record defaults to 1 | 2.5 |
| `LEARNING`: existing record | `AGAIN`, `GOOD`, `EASY` | 10 | 2.5 |
| `REVIEWING`: existing record | `AGAIN`, `GOOD`, `EASY` | 4 | 2.5 |

The user ID is 1, the card ID is 7, and `reverse` is false. Inputs and JDK
dynamic proxies are prepared in `@Setup`, without Spring or a database.
Debug logging is disabled for this service in the benchmark JVM.

Before each attempt, the benchmark restores the existing progress fields
inside the measured operation. This prevents repeated calls from changing
the scenario. `NEW` remains absent on every lookup, simulating independent
first attempts. The simulated save captures only the most recent result,
which the benchmark returns to JMH.

One operation includes the fixture reset, proxy calls, service logic,
`LocalDateTime.now()`, and the simulated save. It does not include actual
authentication, SQL, database flushing, transactions, or HTTP processing.

| Setting | Value |
| --- | --- |
| JMH / JDK | 1.37 / Temurin 11.0.32.1 |
| Operating system / architecture | macOS 27.0.1 / arm64 |
| Mode | Average time, in nanoseconds per in-memory attempt (`ns/op`) |
| Threads | 1 |
| JVM forks per case | 2 |
| Warmup per fork | 3 iterations of 1 second |
| Measurement per fork | 5 iterations of 1 second |
| Complete runs per version | 2 |

The benchmark source and recorded JMH/JVM settings were unchanged between versions.
Both candidate runs used the same rebuilt JAR, with SHA-256 checksum:

```text
045d0c0aa6b635a44029c2544e1b05d17010fd2d080cf96b773352d4d93782fc
```

We added **26 cases** to
[`LearnServiceTest`](../../../../backend/src/test/java/at/ac/tuwien/sepm/groupphase/backend/unittests/LearnServiceTest.java),
bringing that class to 34 tests. They passed before and after the change.
The new cases check missing and existing progress, object identity, user/card
IDs, both study directions, all answer types, learning intervals 1 and 10,
review intervals, the minimum easiness factor, and due dates.

After the change, the full Maven build passed with **439 JUnit tests** and
no failures, errors, or skipped tests.

## 3. Results

The table averages the two complete run scores for each version. Each result
is for **one in-memory attempt including fixture reset**, rather than a
database write or an API request. Lower times are better.

| Initial progress | Answer | Before (ns/op) | After (ns/op) | Time reduction |
| --- | --- | ---: | ---: | ---: |
| `NEW` | `AGAIN` | 105.71 | 106.58 | -0.82% |
| `NEW` | `GOOD` | 109.54 | 112.59 | -2.78% |
| `NEW` | `EASY` | 97.75 | 98.72 | -0.98% |
| `LEARNING` | `AGAIN` | 109.71 | 107.68 | 1.85% |
| `LEARNING` | `GOOD` | 97.23 | 97.87 | -0.66% |
| `LEARNING` | `EASY` | 96.22 | 99.58 | -3.49% |
| `REVIEWING` | `AGAIN` | 99.00 | 98.83 | 0.17% |
| `REVIEWING` | `GOOD` | 102.50 | 97.09 | 5.28% |
| `REVIEWING` | `EASY` | 102.69 | 102.03 | 0.64% |

The percentage is calculated from unrounded values:

```text
100 × (before − after) / before
```

A negative percentage means a longer measured time. The means show both
increases and decreases, rather than a uniform gain. In `NEW` cases, both
versions must create a progress record.

The baseline `NEW/AGAIN` scores were 100.48 and 110.95 ns/op. Candidate run
scores also varied, by up to 8.07% of their mean for a single case. Every
case had at least one overlapping baseline/candidate JMH confidence interval
comparison. No case had both candidate intervals below both baseline intervals.
These observations do not establish equal performance, but they do not
support a clear, repeatable speedup across these scenarios either.

Original results and execution logs:

- Before: [run 01 JSON](../../../baseline/jmh/simple-learn-service/save-attempt-run01.json)
  and [log](../../../baseline/jmh/simple-learn-service/save-attempt-run01.log);
  [run 02 JSON](../../../baseline/jmh/simple-learn-service/save-attempt-run02.json)
  and [log](../../../baseline/jmh/simple-learn-service/save-attempt-run02.log).
- After: [run 01 JSON](save-attempt-run01.json) and [log](save-attempt-run01.log);
  [run 02 JSON](save-attempt-run02.json) and [log](save-attempt-run02.log).

## 4. How to repeat the experiment

Select JDK 11 and run from the repository root:

```sh
mvn clean package

mkdir -p sse-reports/sse-improvements/jmh/simple-learn-service
set -o pipefail

java -jar benchmarks/target/benchmarks.jar \
  'SimpleLearnServiceBenchmark.saveAttempt' \
  -t 1 -foe true -rf json \
  -rff sse-reports/sse-improvements/jmh/simple-learn-service/save-attempt-run03.json \
  2>&1 | tee sse-reports/sse-improvements/jmh/simple-learn-service/save-attempt-run03.log
```

The annotations supply all nine cases and the timing settings above. A full
run takes about three minutes. Use new filenames for additional runs.
The recorded candidate measurements used `-o` to save logs directly; this
example uses `tee` to also show progress in the terminal.

For baseline measurements, restore the original `orElse(new Progress(id))`
expression, rebuild, and save outputs under
`sse-reports/baseline/jmh/simple-learn-service/`. Changing the output directory
does not select the implementation. See the
[benchmark guide](../../../../benchmarks/README.md) for JDK setup.

## 5. Conclusion and limitations

The change makes default progress creation lazy, and the correctness tests
pass. **This experiment does not demonstrate a consistent reduction in
execution time**, so the change should not be presented as a proven speedup.

The measurements use one thread, prepared in-memory dependencies, and fixed
starting states on macOS after JVM warmup. Reset, proxy, and clock costs are
included. The versions were measured in separate runs rather than an
interleaved comparison; background load and power conditions were not recorded.
The results do not describe the time needed to save an attempt to a real
database or process a complete HTTP request.
