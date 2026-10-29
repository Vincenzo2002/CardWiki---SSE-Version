# CardWiki Baseline Performance Tests (JMeter)

## Purpose

This directory contains Apache JMeter test plans, raw results (`.jtl`), and HTML reports used to measure baseline CardWiki performance under increasing configured user loads.

The test scenarios cover two API operations:

- **Search Deck:** `GET /api/v1/decks?name=Haupt&limit=10&offset=0`
- **Open Deck:** `GET /api/v1/decks/1`

## Test scenarios

| Scenario | Configured users | Ramp-up | Loop count | Requests per run |
|---|---:|---:|---:|---:|
| Low load | 5 | 10 s | 10 | 100 |
| Medium load | 10 | 20 s | 10 | 200 |
| High load | 20 | 40 s | 10 | 400 |

Each loop executes one Search Deck request and one Open Deck request. Verify these parameters against the corresponding `.jmx` files before submission.

## Repository and environment

- **Repository:** https://github.com/Vincenzo2002/CardWiki---SSE-Version
- **Commit under test:** 4cd6c67347602b3532b6d22a513632098c1d7456
- **Dataset/version:** Default Dataset from the project
- **Operating system and hardware:** macOS 26.6.2
- **Java version:** Eclipse Temurin OpenJDK 11.0.32.1
- **Apache JMeter version:** 5.6.3
- **Backend:** `http://localhost:8080`
- **Search term:** `Haupt`
- **Deck ID:** `1`
- **Repetitions:** Three runs per configured load level.

## Running a test

From the repository root, use the corresponding test plan and a unique output filename. Example:

```bash
jmeter -n \
  -t sse-reports/baseline/jmeter/cardwiki_baseline_5p.jmx \
  -l sse-reports/baseline/jmeter/results/run1-5p.jtl
```

Replace the JMX filename when testing a different load level. Do not reuse a JTL filename for another run.

## Generating an HTML report from existing results

This command generates a report from a saved JTL file and does not rerun the test:

```bash
jmeter -g \
  sse-reports/baseline/jmeter/results/run1-5p.jtl \
  -o sse-reports/baseline/jmeter/reports/run1-5p
```

The output directory must not already exist or must be empty.

## Metrics

The following metrics are used to compare runs:

- Average response time (ms)
- 95th percentile response time (p95, ms)
- Throughput (requests/s)
- Error rate (%)

## Measurement limitation

The supplied JTL files contain distinct thread names consistent with the configured user counts and 20 samples per thread. However, `grpThreads` and `allThreads` were recorded as `1` in the sample rows, and the HTML Active Threads Over Time charts showed one active thread. This discrepancy should be disclosed when interpreting concurrency.