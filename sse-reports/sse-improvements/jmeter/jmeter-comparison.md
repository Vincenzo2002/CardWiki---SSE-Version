# JMeter comparison: baseline vs SSE improvements

This report compares the JMeter runs of the baseline backend with the runs of
the optimized backend. All figures are computed from the `summary.json` files of
each run; the full JMeter dashboards are linked in the detailed sections.

## Runs compared

| | Baseline (B) | SSE improvements (I) |
|---|---|---|
| Result directories | `baseline/jmeter/*/run02-varied-*` | `sse-improvements/jmeter/*/run01-varied-*` |
| Date (UTC) | 2026-11-05, 15:00–16:31 | 2026-11-17, 08:50–10:20 |
| Declared application commit | `effaf820971d298422ddaea15ab73b9cef5b25e0` | `0badfec448f93d1a96ad413980721ea1e2300965` |
| Dataset | Database dev; input from `jmeter/data/varied.csv` | same |
| Input CSV | 15 search/deck pairs, 5 decks, SHA-256 `e1e17f56…` | same file (same SHA-256) |
| Test plans and runner | identical SHA-256 for every `.jmx` and for the runner | same |
| Workload | think time 500 ms; same users, ramp-up and durations per scenario | same |
| Environment | macOS 26.6.2, Apple A18 Pro (6 cores, arm64), 8 GB RAM, Temurin JDK 11.0.32.1, JMeter 5.6.3 | same |
| Outcome | all runs `completed`, JMeter and report exit code 0 | same |

The metadata of each pair was checked automatically: apart from the commit and
the date, every recorded setting is identical. Concurrency was confirmed in
every phase (peak recorded threads equal to the configured users).

## Method

- Metrics use the **steady** window of each phase (`steady_at_target_threads`):
  ramp-up and the first 10 seconds after it are excluded.
- **Mean** and **p95** are response times in milliseconds; p95 is the time
  within which at least 95% of requests finished.
- **Throughput** is requests per second. Each virtual user waits 500 ms before
  every request, so throughput is capped at about `users / (0.5 s + response
  time)`. It only changes noticeably when response times become large compared
  with the think time.
- Δ is the relative change from baseline to SSE improvements: negative is
  better for response times, positive is better for throughput.

## Summary

| Test | Main phase | Errors (B / I) | Mean ms (B → I) | Δ mean | p95 ms (B → I) | Δ p95 | Throughput req/s (B → I) | Δ throughput |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| Load, 50 users | load | 0 / 0 | 20.0 → 4.3 | -78% | 72 → 12 | -83% | 95.3 → 98.5 | +3% |
| Load, 200 users | load | 0 / 0 | 31.2 → 3.4 | -89% | 151 → 9 | -94% | 373.9 → 395.4 | +6% |
| Load, 500 users | load | 0 / 0 | 8.0 → 4.9 | -38% | 31 → 16 | -48% | 977.5 → 985.6 | +1% |
| Stress, 20 → 2,000 → 20 users | stress | 0 / 0 | 519 → 11.8 | -98% | 1212 → 48 | -96% | 1950.5 → 3883.6 | +99% |
| Spike, 20 → 200 → 20 users | spike | 0 / 0 | 5.6 → 3.9 | -31% | 23 → 12 | -48% | 394.2 → 393.8 | 0% |
| Spike, 20 → 500 → 20 users | spike | 0 / 0 | 10.7 → 5.1 | -52% | 62 → 17 | -73% | 978.6 → 985.3 | +1% |
| Soak, 100 users | soak | 0 / 0 | 6.7 → 3.5 | -48% | 18 → 9 | -50% | 195.4 → 197.3 | +1% |
No run, in either version, produced errors.

## Findings

- **Response times dropped in every scenario.** The steady mean fell by 31–98%
  and the p95 by 48–96%. In the improved version the steady mean of the main
  phase stays between 3.4 and 11.8 ms in all tests, including 2,000 concurrent users.
- **Stress test: the baseline saturated, the improved version did not.** At
  2,000 users the baseline mean rose to 519 ms (p95 1,212 ms) and throughput
  stopped at 1,950 req/s, about half the ~3,900 req/s that the think time
  allows. The improved version kept an 11.8 ms mean (p95 48 ms) and reached
  3,884 req/s, i.e. it served roughly twice as many requests in the same time.
- **Load, spike and soak: throughput is essentially unchanged (0% to +6%).**
  This is expected: below saturation, throughput is set by the 500 ms think
  time, not by the backend. The improvement there is visible in latency only.
- **Both endpoints benefit.** In the baseline `Search Deck` was consistently
  slower than `Open Deck`; in the improved version the two endpoints have
  practically the same response times.
- **Recovery.** After the stress and spike peaks, both versions return to
  low-load response times; over the whole recovery phase the improved version
  is equal or faster in every test (for `Open Deck` alone it is slightly slower
  in two recovery phases, by less than 1 ms).
- **Soak (1 hour, 100 users).** The improved version halves mean and p95 over
  more than 700,000 requests with no errors, so the gain is stable over time.

## Limitations

- **One run per version and scenario.** No repetitions were made, so the
  variance between runs is unknown. Small differences (a few milliseconds, or
  throughput changes of a few percent) should not be over-interpreted.
- **Baseline anomaly.** In the baseline, the 50- and 200-user load tests have
  higher response times than the 500-user test, which ran right after them.
  This suggests a warm-up effect (JIT compilation, caches, connection pools)
  that inflates the first baseline runs. The tests were run in the same order
  in both versions, but the improvement for the first two load tests is likely
  overestimated. The later tests (load 500, spike, soak) give a more
  conservative estimate: roughly −30% to −50% on the mean and −50% to −70% on
  the p95, plus the stress-test result.
- **Runs on different days** on the same laptop, with other applications
  possibly running. Background load was not controlled.
- **Declared commit.** The application commit is declared by the user; the
  backend does not expose it.
- **No energy conclusion.** Shorter response times are not, by themselves,
  evidence of lower energy consumption; see the EnergiBridge measurements for
  that.

## Detailed results

`B` = baseline, `I` = SSE improvements. Each table shows the whole phase
(**All**) and the two endpoints separately.

### Load, 50 users

Runs: [`baseline/.../run02-varied-u050`](../../baseline/jmeter/load_test/run02-varied-u050/report/index.html) and [`sse-improvements/.../run01-varied-u050`](load_test/run01-varied-u050/report/index.html).

| Phase | Endpoint | Samples (B / I) | Errors (B / I) | Mean ms (B → I) | Δ mean | p95 ms (B → I) | Δ p95 | Throughput req/s (B → I) | Δ throughput |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| load (50 users) | **All** | 26,597 / 27,513 | 0 / 0 | 20.0 → 4.3 | -78% | 72 → 12 | -83% | 95.3 → 98.5 | +3% |
| load (50 users) | Open Deck | 13,303 / 13,756 | 0 / 0 | 17.8 → 4.3 | -76% | 66 → 12 | -82% | 47.7 → 49.3 | +3% |
| load (50 users) | Search Deck | 13,294 / 13,757 | 0 / 0 | 22.2 → 4.3 | -80% | 79 → 12 | -85% | 47.7 → 49.3 | +3% |

### Load, 200 users

Runs: [`baseline/.../run02-varied-u200`](../../baseline/jmeter/load_test/run02-varied-u200/report/index.html) and [`sse-improvements/.../run01-varied-u200`](load_test/run01-varied-u200/report/index.html).

| Phase | Endpoint | Samples (B / I) | Errors (B / I) | Mean ms (B → I) | Δ mean | p95 ms (B → I) | Δ p95 | Throughput req/s (B → I) | Δ throughput |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| load (200 users) | **All** | 104,409 / 110,301 | 0 / 0 | 31.2 → 3.4 | -89% | 151 → 9 | -94% | 373.9 → 395.4 | +6% |
| load (200 users) | Open Deck | 52,190 / 55,157 | 0 / 0 | 27.9 → 3.4 | -88% | 127 → 9 | -93% | 186.9 → 197.7 | +6% |
| load (200 users) | Search Deck | 52,219 / 55,144 | 0 / 0 | 34.5 → 3.4 | -90% | 173 → 9 | -95% | 187.0 → 197.7 | +6% |

### Load, 500 users

Runs: [`baseline/.../run02-varied-u500`](../../baseline/jmeter/load_test/run02-varied-u500/report/index.html) and [`sse-improvements/.../run01-varied-u500`](load_test/run01-varied-u500/report/index.html).

| Phase | Endpoint | Samples (B / I) | Errors (B / I) | Mean ms (B → I) | Δ mean | p95 ms (B → I) | Δ p95 | Throughput req/s (B → I) | Δ throughput |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| load (500 users) | **All** | 272,691 / 274,933 | 0 / 0 | 8.0 → 4.9 | -38% | 31 → 16 | -48% | 977.5 → 985.6 | +1% |
| load (500 users) | Open Deck | 136,376 / 137,468 | 0 / 0 | 6.9 → 4.9 | -29% | 26 → 16 | -38% | 488.9 → 492.8 | +1% |
| load (500 users) | Search Deck | 136,315 / 137,465 | 0 / 0 | 9.0 → 4.9 | -46% | 34 → 16 | -53% | 488.6 → 492.8 | +1% |

### Stress, 20 → 2,000 → 20 users

Runs: [`baseline/.../run02-varied-u2000`](../../baseline/jmeter/stress_test/run02-varied-u2000/report/index.html) and [`sse-improvements/.../run01-varied-u2000`](stress_test/run01-varied-u2000/report/index.html).

| Phase | Endpoint | Samples (B / I) | Errors (B / I) | Mean ms (B → I) | Δ mean | p95 ms (B → I) | Δ p95 | Throughput req/s (B → I) | Δ throughput |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| before (20 users) | **All** | 1,725 / 1,733 | 0 / 0 | 5.8 → 3.3 | -42% | 15 → 6 | -60% | 39.3 → 39.5 | 0% |
| before (20 users) | Open Deck | 862 / 867 | 0 / 0 | 4.4 → 3.3 | -24% | 12 → 6 | -50% | 19.7 → 19.7 | 0% |
| before (20 users) | Search Deck | 863 / 866 | 0 / 0 | 7.2 → 3.3 | -54% | 18 → 6 | -67% | 19.7 → 19.8 | 0% |
| stress (2000 users) | **All** | 310,169 / 617,624 | 0 / 0 | 519 → 11.8 | -98% | 1212 → 48 | -96% | 1950.5 → 3883.6 | +99% |
| stress (2000 users) | Open Deck | 155,057 / 308,826 | 0 / 0 | 514 → 11.8 | -98% | 1209 → 49 | -96% | 975.1 → 1941.9 | +99% |
| stress (2000 users) | Search Deck | 155,112 / 308,798 | 0 / 0 | 524 → 11.7 | -98% | 1214 → 48 | -96% | 975.4 → 1941.7 | +99% |
| recovery (20 users) | **All** | 1,732 / 1,742 | 0 / 0 | 4.2 → 3.9 | -7% | 10 → 8 | -20% | 39.3 → 39.4 | 0% |
| recovery (20 users) | Open Deck | 869 / 871 | 0 / 0 | 3.6 → 3.9 | +9% | 8 → 8 | 0% | 19.7 → 19.7 | 0% |
| recovery (20 users) | Search Deck | 863 / 871 | 0 / 0 | 4.9 → 3.9 | -19% | 11 → 8 | -27% | 19.7 → 19.7 | 0% |

### Spike, 20 → 200 → 20 users

Runs: [`baseline/.../run02-varied-u200`](../../baseline/jmeter/spike_test/run02-varied-u200/report/index.html) and [`sse-improvements/.../run01-varied-u200`](spike_test/run01-varied-u200/report/index.html).

| Phase | Endpoint | Samples (B / I) | Errors (B / I) | Mean ms (B → I) | Δ mean | p95 ms (B → I) | Δ p95 | Throughput req/s (B → I) | Δ throughput |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| before (20 users) | **All** | 1,735 / 1,740 | 0 / 0 | 4.3 → 2.9 | -32% | 11 → 5 | -55% | 39.3 → 39.6 | +1% |
| before (20 users) | Open Deck | 870 / 872 | 0 / 0 | 3.5 → 2.9 | -16% | 9 → 5 | -44% | 19.7 → 19.8 | +1% |
| before (20 users) | Search Deck | 865 / 868 | 0 / 0 | 5.2 → 2.9 | -44% | 12 → 5 | -58% | 19.6 → 19.8 | +1% |
| spike (200 users) | **All** | 7,063 / 7,097 | 0 / 0 | 5.6 → 3.9 | -31% | 23 → 12 | -48% | 394.2 → 393.8 | 0% |
| spike (200 users) | Open Deck | 3,528 / 3,547 | 0 / 0 | 4.7 → 3.9 | -18% | 20 → 12 | -40% | 196.9 → 196.8 | 0% |
| spike (200 users) | Search Deck | 3,535 / 3,550 | 0 / 0 | 6.5 → 3.9 | -40% | 27 → 12 | -56% | 197.4 → 197.1 | 0% |
| recovery (20 users) | **All** | 1,732 / 1,744 | 0 / 0 | 4.8 → 3.2 | -34% | 12 → 6 | -50% | 39.2 → 39.6 | +1% |
| recovery (20 users) | Open Deck | 866 / 873 | 0 / 0 | 4.0 → 3.2 | -20% | 9 → 6 | -33% | 19.6 → 19.8 | +1% |
| recovery (20 users) | Search Deck | 866 / 871 | 0 / 0 | 5.6 → 3.2 | -43% | 13 → 6 | -54% | 19.6 → 19.8 | +1% |

### Spike, 20 → 500 → 20 users

Runs: [`baseline/.../run02-varied-u500`](../../baseline/jmeter/spike_test/run02-varied-u500/report/index.html) and [`sse-improvements/.../run01-varied-u500`](spike_test/run01-varied-u500/report/index.html).

| Phase | Endpoint | Samples (B / I) | Errors (B / I) | Mean ms (B → I) | Δ mean | p95 ms (B → I) | Δ p95 | Throughput req/s (B → I) | Δ throughput |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| before (20 users) | **All** | 1,727 / 1,734 | 0 / 0 | 5.4 → 4.6 | -14% | 15 → 10 | -33% | 39.3 → 39.4 | 0% |
| before (20 users) | Open Deck | 866 / 868 | 0 / 0 | 4.4 → 4.7 | +6% | 12 → 9 | -25% | 19.7 → 19.7 | 0% |
| before (20 users) | Search Deck | 861 / 866 | 0 / 0 | 6.4 → 4.6 | -28% | 18 → 10 | -44% | 19.7 → 19.7 | 0% |
| spike (500 users) | **All** | 17,681 / 17,989 | 0 / 0 | 10.7 → 5.1 | -52% | 62 → 17 | -73% | 978.6 → 985.3 | +1% |
| spike (500 users) | Open Deck | 8,837 / 9,000 | 0 / 0 | 9.8 → 5.1 | -48% | 61 → 18 | -70% | 489.2 → 501.0 | +2% |
| spike (500 users) | Search Deck | 8,844 / 8,989 | 0 / 0 | 11.5 → 5.1 | -55% | 63 → 17 | -73% | 489.5 → 492.3 | +1% |
| recovery (20 users) | **All** | 1,733 / 1,744 | 0 / 0 | 4.4 → 4.3 | -2% | 10 → 10 | 0% | 39.5 → 39.4 | 0% |
| recovery (20 users) | Open Deck | 867 / 872 | 0 / 0 | 3.7 → 4.3 | +14% | 9 → 10 | +11% | 19.8 → 19.7 | 0% |
| recovery (20 users) | Search Deck | 866 / 872 | 0 / 0 | 5.0 → 4.3 | -15% | 11 → 10 | -9% | 19.7 → 19.7 | 0% |

### Soak, 100 users

Runs: [`baseline/.../run02-varied-u100`](../../baseline/jmeter/soak_test/run02-varied-u100/report/index.html) and [`sse-improvements/.../run01-varied-u100`](soak_test/run01-varied-u100/report/index.html).

| Phase | Endpoint | Samples (B / I) | Errors (B / I) | Mean ms (B → I) | Δ mean | p95 ms (B → I) | Δ p95 | Throughput req/s (B → I) | Δ throughput |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| soak (100 users) | **All** | 695,301 / 702,317 | 0 / 0 | 6.7 → 3.5 | -48% | 18 → 9 | -50% | 195.4 → 197.3 | +1% |
| soak (100 users) | Open Deck | 347,643 / 351,156 | 0 / 0 | 5.6 → 3.5 | -38% | 15 → 9 | -40% | 97.7 → 98.7 | +1% |
| soak (100 users) | Search Deck | 347,658 / 351,161 | 0 / 0 | 7.8 → 3.5 | -56% | 21 → 9 | -57% | 97.7 → 98.7 | +1% |
