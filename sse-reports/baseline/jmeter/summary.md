# CardWiki – Baseline API performance test summary

## 1. Scope and result inventory

This document summarizes the corrected, timed JMeter measurements for the **baseline** version of CardWiki. It covers the 22 measured executions present in this archive. The preliminary smoke/validation execution is intentionally excluded.

| Scenario | Profiles | Repetitions | Executions |
|---|---|---:|---:|
| Load | 5, 10 and 20 users | 3 per profile | 9 |
| Stress | 50, 100 and 200 users | 3 per profile | 9 |
| Spike | 50 users | 3 | 3 |
| Soak | 20 users | 1 | 1 |
| **Total** | | | **22** |

All 22 executions have `status=completed`, JMeter exit code `0`, and report-generation exit code `0`. Each execution has a `summary.json`, `results.jtl`, metadata and generated HTML report (`report/index.html`).

The raw JTL files contain **580,206 samples**. An independent check of the archived rows found:
- 580,206 successful samples (`success=true`);
- 580,206 HTTP `200` responses;
- 0 failed samples and 0 non-200 response codes.

The number of JTL rows matches the `overall_including_ramps_and_warmup.samples` value in each corresponding `summary.json`.

## 2. Application, environment and methodology

### Application and fixture

- Version measured: `baseline`.
- Declared application commit: `4ba66f959a677f00efc882b5344f50758a342e5a`.
- Harness checkout commit recorded in metadata: `4ba66f959a677f00efc882b5344f50758a342e5a`.
- The application commit is user-declared: the running backend does not expose a version endpoint, so the metadata cannot independently verify which commit the process was running.
- Dataset: `dev dataset, unchanged; Haupt search and deck 1 verified`.
- Backend URL: `http://localhost:8080`.
- Read-only operations:
  - `GET /api/v1/decks?name=Haupt&limit=10&offset=0` — Search Deck.
  - `GET /api/v1/decks/1` — Open Deck.

### Recorded test environment

| Item | Recorded value |
|---|---|
| Operating system | macOS 26.6.2 |
| Architecture | ARM64 |
| CPU | Apple A18 Pro, 6 logical CPUs reported |
| Memory | 8 GB |
| Java | Eclipse Temurin OpenJDK 11.0.32.1 |
| Apache JMeter | 5.6.3 |
| Request think time | 500 ms before each request |
| Connect / response timeout | 5 s / 10 s |

The backend was accessed through `localhost` and the JMeter command was run on the recorded Mac. The load generator and backend therefore shared the same host in this setup.

### Profiles

Durations include ramp-up. The runner uses a 10-second warm-up exclusion after ramp-up when calculating steady-state metrics.

| Scenario | Phase | Users | Ramp-up | Duration |
|---|---|---:|---:|---:|
| Load | Load | 5 / 10 / 20 | 10 s | 300 s |
| Stress | Before | 5 | 5 s | 60 s |
| Stress | Stress | 50 / 100 / 200 | 10 s | 180 s |
| Stress | Recovery | 5 | 5 s | 60 s |
| Spike | Before | 5 | 5 s | 60 s |
| Spike | Spike | 50 | 1 s | 30 s |
| Spike | Recovery | 5 | 5 s | 60 s |
| Soak | Soak | 20 | 30 s | 3,600 s |

Five users were used as the reference load for the before/recovery phases of stress and spike. This is an experimental reference level, not a measured production-traffic profile.

The summary tables below use the runner's `steady_at_target_threads` metrics. For repeated profiles, the central value is the median of the three runs; ranges show the minimum and maximum observed run-level values. The p95 is the runner's nearest-rank p95.

## 3. Concurrency evidence

Concurrency was checked against the raw JTL `grpThreads` field, not inferred only from the configured user count or the peak-thread field.

In the central steady-state windows, **100% of recorded request samples had `grpThreads` equal to the configured target** in every execution:
- Load: after the 10-second ramp-up and 10-second warm-up, through the analyzed steady window.
- Stress: from 20 seconds into the stress phase to 170 seconds.
- Spike: from 11 seconds into the spike phase to 25 seconds.
- Soak: from 40 seconds into the soak phase to 3,590 seconds.

The metadata also records the expected peak thread count and distinct thread names for every phase. The before and recovery phases likewise reached and maintained their 5-user target in their central steady-state windows.

This confirms that the measured steady-state samples were collected at the intended concurrency. It does not establish that the same concurrency is representative of real production traffic.

## 4. Load test results

Steady-state results, aggregated across the three repetitions:

| Users | Mean latency (median) | Mean latency range | p95 (median) | p95 range | Throughput (median) |
|---:|---:|---:|---:|---:|---:|
| 5 | 6.20 ms | 5.11–6.88 ms | 14 ms | 10–15 ms | 9.79 req/s |
| 10 | 6.63 ms | 5.83–7.06 ms | 14 ms | 14–15 ms | 19.59 req/s |
| 20 | 5.17 ms | 4.08–5.64 ms | 13 ms | 10–14 ms | 39.31 req/s |

Throughput scales approximately linearly across the tested load levels: about 9.8, 19.6 and 39.3 requests/s at 5, 10 and 20 users. The run-level p95 values remain in a relatively narrow range, and no errors were recorded.

The results show no throughput plateau within the tested load range. They do not establish a production capacity limit.

### Load endpoint breakdown

Median of the three run-level endpoint metrics:

| Users | Endpoint | Mean latency | p95 |
|---:|---|---:|---:|
| 5 | Open Deck | 4.78 ms | 10 ms |
| 5 | Search Deck | 7.62 ms | 16 ms |
| 10 | Open Deck | 5.86 ms | 10 ms |
| 10 | Search Deck | 7.48 ms | 16 ms |
| 20 | Open Deck | 4.35 ms | 11 ms |
| 20 | Search Deck | 6.00 ms | 14 ms |

Search Deck has the higher median latency than Open Deck at each load level.

## 5. Stress test results

Steady-state main-phase results, aggregated across three repetitions:

| Users | Mean latency (median) | Mean latency range | p95 (median) | p95 range | Throughput (median) | Throughput range |
|---:|---:|---:|---:|---:|---:|---:|
| 50 | 6.90 ms | 5.37–16.42 ms | 21 ms | 16–39 ms | 97.98 req/s | 96.03–98.24 |
| 100 | 5.45 ms | 4.46–25.81 ms | 17 ms | 13–138 ms | 196.48 req/s | 188.86–196.69 |
| 200 | 13.00 ms | 4.26–21.35 ms | 43 ms | 16–82 ms | 387.58 req/s | 380.80–394.38 |

Throughput continues to increase as concurrency rises from 50 to 200 users. No errors were recorded and the target thread count was maintained in the analyzed steady-state windows.

However, latency varies substantially between repetitions, especially at 100 and 200 users. The median alone hides important transient slowdowns, so the time-series analysis below is part of the result and should not be discarded.

### Stress time-series inspection

The following values are the **maximum mean and maximum p95 observed in any 10-second bucket** of each main stress phase. Buckets were evaluated after the 10-second ramp-up plus 10-second warm-up and before the final phase transition. Each run contributes 15 analyzed buckets.

| Users | Run | Maximum 10 s mean | Maximum 10 s p95 | Bucket start |
|---:|---|---:|---:|---:|
| 50 | run01 | 6.8 ms | 25 ms | 100 s |
| 50 | run02 | 20.9 ms | 128 ms | 120 s |
| 50 | run03 | 226.1 ms | 695 ms | 90 s |
| 100 | run01 | 7.1 ms | 29 ms | 160 s |
| 100 | run02 | 14.0 ms | 58 ms | 50 s |
| 100 | run03 | 67.8 ms | 362 ms | 50 s |
| 200 | run01 | 82.6 ms | 543 ms | 20 s |
| 200 | run02 | 32.7 ms | 190 ms | 140 s |
| 200 | run03 | 7.3 ms | 38 ms | 130 s |

The 50-user run03, 100-user run03 and 200-user run01/run02 contain pronounced short-lived latency spikes. In particular, the 50-user run03 reaches a 10-second bucket p95 of 695 ms, while its full steady-state phase p95 is 39 ms. The 100-user run03 also shows elevated latency across multiple buckets (median 10-second bucket p95: 126 ms). These observations demonstrate why the time series must accompany phase aggregates.

All of these samples were recorded while the configured target `grpThreads` value was present in the analyzed core window. The spikes therefore are not explained by a drop in recorded active-thread count. The available data does not identify their cause.

### Stress endpoint breakdown

Median of the three run-level endpoint metrics:

| Users | Endpoint | Mean latency | p95 |
|---:|---|---:|---:|
| 50 | Open Deck | 5.57 ms | 16 ms |
| 50 | Search Deck | 8.23 ms | 25 ms |
| 100 | Open Deck | 4.53 ms | 14 ms |
| 100 | Search Deck | 6.37 ms | 19 ms |
| 200 | Open Deck | 11.50 ms | 38 ms |
| 200 | Search Deck | 14.51 ms | 48 ms |

Search Deck has higher median latency than Open Deck at all three stress levels.

## 6. Stress recovery

The before and recovery phases both use five users. The following values are medians across the three runs at each stress level:

| Stress level | Phase | Mean latency | p95 | Throughput |
|---:|---|---:|---:|---:|
| 50 | Before | 7.27 ms | 14 ms | 9.80 req/s |
| 50 | Recovery | 4.76 ms | 9 ms | 9.92 req/s |
| 100 | Before | 5.62 ms | 13 ms | 9.82 req/s |
| 100 | Recovery | 4.58 ms | 10 ms | 9.88 req/s |
| 200 | Before | 8.75 ms | 17 ms | 9.84 req/s |
| 200 | Recovery | 5.33 ms | 12 ms | 9.92 req/s |

The recovery-phase medians return to values comparable to, or lower than, the corresponding before-phase medians. Throughput returns to approximately 9.9 req/s. Individual run values still vary; for example, the 50-user run03 recovery p95 is 15 ms versus 14 ms before. The aggregate comparison supports recovery in the measured steady recovery window, not a guarantee about every instant immediately after the stress phase.

## 7. Spike test results

Median steady-state values across the three repetitions:

| Phase | Users | Mean latency | p95 | Throughput |
|---|---:|---:|---:|---:|
| Before | 5 | 5.09 ms | 10 ms | 9.85 req/s |
| Spike | 50 | 2.35 ms | 7 ms | 98.00 req/s |
| Recovery | 5 | 4.67 ms | 10 ms | 9.91 req/s |

The three runs are consistent in their spike-phase aggregates:
- Mean latency: 2.33–3.09 ms.
- p95: 6–8 ms.
- Throughput: 97.87–98.94 req/s.
- Zero errors in before, spike and recovery phases.

Raw JTL timestamps show that the 50-thread target was first recorded approximately **0.47 seconds** after the spike phase's first sample in each run. In the central steady-state window, every recorded sample had `grpThreads=50`. The 5-thread target in recovery was first recorded approximately 3.5 seconds after the recovery phase's first sample; the central recovery window then maintained the target.

The recovery median p95 (10 ms) is the same as the before median p95 (10 ms), and throughput returns from about 98 to about 9.9 req/s as the workload returns from 50 to 5 users.

### Spike endpoint breakdown

Median of the three run-level endpoint metrics during the spike steady state:

| Endpoint | Mean latency | p95 |
|---|---:|---:|
| Open Deck | 1.92 ms | 6 ms |
| Search Deck | 2.94 ms | 8 ms |

## 8. Soak test results

One 60-minute soak execution was performed with 20 users. The steady-state summary reports:

| Metric | Result |
|---|---:|
| Observed duration (all samples) | 3,599.388 s |
| Configured / peak recorded threads | 20 / 20 |
| Steady-state samples | 139,494 |
| Errors | 0 (0%) |
| Mean latency | 3.77 ms |
| p95 | 10 ms |
| Throughput | 39.20 req/s |

### Soak time series

The following table groups steady-state JTL samples into five-minute intervals after the 30-second ramp-up and 10-second warm-up. The first and last intervals are partial because of those exclusions and the phase boundary.

| Interval | Samples | Mean latency | p95 | Throughput |
|---|---:|---:|---:|---:|
| 00–05 min | 10,234 | 4.37 ms | 11 ms | 39.40 req/s |
| 05–10 min | 11,740 | 3.77 ms | 10 ms | 39.24 req/s |
| 10–15 min | 11,759 | 3.82 ms | 10 ms | 39.23 req/s |
| 15–20 min | 11,760 | 3.87 ms | 10 ms | 39.24 req/s |
| 20–25 min | 11,740 | 3.78 ms | 10 ms | 39.25 req/s |
| 25–30 min | 11,760 | 3.75 ms | 10 ms | 39.25 req/s |
| 30–35 min | 11,760 | 3.79 ms | 10 ms | 39.24 req/s |
| 35–40 min | 11,748 | 3.85 ms | 10 ms | 39.20 req/s |
| 40–45 min | 11,752 | 3.65 ms | 9 ms | 39.23 req/s |
| 45–50 min | 11,760 | 3.69 ms | 9 ms | 39.25 req/s |
| 50–55 min | 11,760 | 3.63 ms | 9 ms | 39.25 req/s |
| 55–60 min | 11,365 | 3.37 ms | 7 ms | 39.24 req/s |

All recorded samples in the analyzed steady-state soak window had `grpThreads=20`. There is no visible upward drift in the five-minute mean latency or p95 series; throughput remains approximately 39.2–39.4 req/s. This is evidence of stable request performance over this one-hour run. It is **not** proof that there are no memory/resource leaks, because resource usage was not recorded and only one soak repetition was run.

### Soak endpoint breakdown

| Endpoint | Samples | Mean latency | p95 | Throughput |
|---|---:|---:|---:|---:|
| Open Deck | 69,747 | 3.04 ms | 8 ms | 19.60 req/s |
| Search Deck | 69,747 | 4.49 ms | 11 ms | 19.60 req/s |

## 9. Saturation and recovery interpretation

The run metadata explicitly states that no response-time SLA or saturation threshold was assumed. Therefore, these measurements cannot be used to claim that a predefined SLA was passed or to identify a formal maximum-capacity number.

For interpretation, useful saturation indicators are:
1. sustained application errors or failed content assertions;
2. inability to reach or maintain the configured thread count;
3. throughput flattening or declining while latency increases.

Observed evidence:
- No failed samples or non-200 responses were recorded.
- The configured target concurrency was maintained in all analyzed steady-state windows.
- Throughput increased from a median of approximately 98 req/s at 50 stress users, to 196 req/s at 100 users, and 388 req/s at 200 users; a throughput plateau was not observed up to 200 users.
- Despite that throughput growth, stress time series contain substantial latency spikes and large run-to-run variability, especially at 100 and 200 users.

**Conclusion:** the tests do not demonstrate a hard saturation point up to 200 configured users, but they do show intermittent latency degradation at higher stress levels. The data is insufficient to certify 200 users as a sustainable production capacity or to attribute the slowdowns to the backend rather than the shared host/load generator.

Recovery is assessed by comparing the 5-user recovery phase with the 5-user before phase. The median latency and throughput values return to a comparable range after both stress and spike workloads.

## 10. Limitations

- **Shared host:** the backend is addressed through `localhost` and JMeter ran on the same Mac. Backend work and load generation competed for the same machine resources. A separate load generator is preferable for isolating backend capacity.
- **No resource telemetry:** CPU utilization, memory usage, JVM heap/GC, database connections and disk activity were not recorded. JMeter response samples alone cannot explain the cause of latency spikes.
- **No predefined SLA or saturation threshold:** no formal pass/fail latency limit or capacity boundary can be asserted retrospectively.
- **Single soak repetition:** the one-hour test is useful endurance evidence, but repeatability across multiple soak runs was not assessed.
- **Fixed local fixture and request mix:** the measurements cover the unchanged development dataset, the `Haupt` search and deck ID `1`, with two read-only endpoints and 500 ms think time. They do not represent all application operations or a validated production traffic distribution.
- **Commit declaration:** the application commit is stored as user-declared because the backend does not expose its commit.
- **No energy measurement:** these are execution-time/performance tests. Lower latency or higher throughput must not be interpreted as lower energy consumption.

## 11. Overall conclusion

The corrected JMeter suite produced 22 completed measurement executions and 580,206 successful HTTP 200 samples, with concurrency confirmed in all analyzed steady-state windows.

Load throughput scaled approximately linearly through 20 users. Stress throughput continued to rise through 200 users, but several runs showed substantial transient latency spikes and meaningful run-to-run variation. The spike test reached 50 users in under half a second and returned to the five-user reference level. The single one-hour soak run maintained 20 users and stable latency/throughput trends without errors.

These results provide a reproducible baseline for comparison with future SSE improvements, provided the same JMeter plans, dataset, think time, environment and measurement settings are reused. They do not establish a production SLA, a definitive saturation limit, resource-level causes, or energy savings.
