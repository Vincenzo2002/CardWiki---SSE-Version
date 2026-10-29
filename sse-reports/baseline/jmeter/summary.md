# CardWiki Baseline JMeter Results

## Scope

The baseline CardWiki application was tested with JMeter using two API operations:

- **Search Deck:** `GET /api/v1/decks?name=Haupt&limit=10&offset=0`
- **Open Deck:** `GET /api/v1/decks/1`

Three runs were analyzed for each configured load level: 5, 10, and 20 users.

## Results by run

| Configured users | Run | Samples | Average (ms) | p95 (ms) | Throughput (req/s) | Error rate |
|---:|---|---:|---:|---:|---:|---:|
| 5 | Run 1 | 100 | 5.44 | 16.10 | 12.330 | 0% |
| 5 | Run 2 | 100 | 4.26 | 15.00 | 12.367 | 0% |
| 5 | Run 3 | 100 | 2.76 | 4.15 | 12.436 | 0% |
| 10 | Run 1 | 200 | 2.49 | 7.15 | 11.074 | 0% |
| 10 | Run 2 | 200 | 2.59 | 9.00 | 11.075 | 0% |
| 10 | Run 3 | 200 | 2.59 | 9.00 | 11.063 | 0% |
| 20 | Run 1 | 400 | 1.91 | 8.00 | 10.502 | 0% |
| 20 | Run 2 | 400 | 2.52 | 9.00 | 10.511 | 0% |
| 20 | Run 3 | 400 | 2.43 | 9.00 | 10.502 | 0% |

## Summary by load level

| Configured users | Samples per run | Mean run average (ms) | Mean run p95 (ms)* | Mean throughput (req/s) | Error rate |
|---:|---:|---:|---:|---:|---:|
| 5 | 100 | 4.15 | 11.75 | 12.378 | 0% |
| 10 | 200 | 2.55 | 8.38 | 11.071 | 0% |
| 20 | 400 | 2.29 | 8.67 | 10.505 | 0% |

\* Arithmetic mean of the three per-run p95 values, not a pooled p95.

## Preliminary findings

- All recorded requests succeeded with HTTP 200; the observed error rate was 0% in all nine runs.
- Mean throughput decreased from approximately 12.38 requests/s at 5 configured users to 11.07 at 10 users and 10.50 at 20 users.
- Average response times remained low and did not increase monotonically with load. This should not be interpreted as evidence that higher load improves performance.
- Search Deck was slower on average than Open Deck in the supplied runs.

## Measurement limitation

The JTL files contain distinct thread names and sample counts consistent with the configured user counts: 100, 200, and 400 requests per run. However, `grpThreads` and `allThreads` were recorded as `1` for every sample, and the HTML Active Threads Over Time charts showed one active thread. Therefore, the files do not conclusively demonstrate simultaneous concurrency. This limitation should be disclosed when interpreting the results.

## Conclusion

All supplied runs completed with a recorded error rate of zero. Throughput declined as the configured load increased, while average response times stayed low and did not rise monotonically. Interpret the results with the active-thread reporting limitation in mind.

## Reproducibility information

- **Commit:** `4cd6c67347602b3532b6d22a513632098c1d7456`
- **Dataset:** Default project dataset, unchanged.
- **Operating system:** macOS 26.6.2
- **Java:** Eclipse Temurin OpenJDK 11.0.32.1
- **Apache JMeter:** 5.6.3
- **Load configurations:** 5 users / 10-second ramp-up / 10 loops; 10 users / 20-second ramp-up / 10 loops; 20 users / 40-second ramp-up / 10 loops.
- **Repetitions:** Three per load level.

