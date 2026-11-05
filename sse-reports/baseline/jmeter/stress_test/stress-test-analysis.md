# CardWiki stress-test results

## Test setup

We tested deck search and opening before, during and after a high load.
Each execution had one minute before stress, three minutes under stress
and one minute of recovery, with a 500 ms pause before each request.

The historical `run01` used 5 → 50 → 5 users and a fixed search/deck pair.
The new measurement on 5 November 2026 used 20 → 2,000 → 20 users and 15 CSV
pairs across five decks. All phases reached their target user count. Both
series record an Apple A18 Pro with 8 GiB of RAM.

## Results

The table excludes ramp-up and the following 10-second warmup in each
phase: the first 15 seconds of before/recovery and 20 seconds of stress,
measured from each phase's first sample. Only samples at the configured
user count are included.

| Test | Phase | Users | Average response time | p95 | Requests per second | Errors |
| --- | --- | --- | --- | --- | --- | --- |
| run01 | Before | 5 | 7.92 ms | 15 ms | 9.76 | 0 |
| run01 | Stress | 50 | 7.20 ms | 19 ms | 97.63 | 0 |
| run01 | Recovery | 5 | 5.95 ms | 12 ms | 9.84 | 0 |
| run02-varied-u2000 | Before | 20 | 5.77 ms | 15 ms | 39.35 | 0 |
| run02-varied-u2000 | Stress | 2,000 | 519.04 ms | 1212 ms | 1,950.48 | 0 |
| run02-varied-u2000 | Recovery | 20 | 4.23 ms | 10 ms | 39.33 | 0 |

The p95 means that at least 95% of measured requests completed within that
time. Requests per second count individual HTTP requests.

## Reflections

**The new stress phase caused a clear slowdown.** Mean response time rose
from 5.77 ms before stress to 519.04 ms at 2,000 users. The p95 increased
from 15 to 1,212 ms, meaning that a significant part of the traffic waited
around a second or more. The slowest steady-state response was 2,368 ms.

**Requests continued succeeding, but became much slower.** No HTTP or
response-validation errors were recorded, while throughput reached about
1,950 requests/s. Zero errors therefore does not mean that performance was
unaffected. Search and opening both slowed: their steady means were
523.97 ms and 514.10 ms respectively.

**The backend recovered after the load decreased.** At 20 users, mean
latency returned to 4.23 ms and p95 to 10 ms, compared with 5.77 and 15 ms
before stress. Throughput returned to about 39 requests/s. This shows no
lasting slowdown in the observed recovery interval, but does not measure
the exact recovery time.

**The result indicates pressure at high load, without locating the precise
limit or its cause.** Backend and JMeter ran on the same machine, and no
CPU, memory, GC or database-connection measurements were supplied. The
slowdown could involve either process; one run cannot identify the bottleneck.

The historical `run01` used far fewer users and fixed input, so it is not a
matching before/after optimization comparison. Repeating the new test and
adding intermediate user levels would help find where the slowdown begins.
The three-minute stress phase also does not establish long-term stability.

## Original results

The table uses `summary.json`, checked against the original JTL samples.
The ZIP dashboards were read directly; the original files were left unchanged.
No new tests were run.

- [run01](run01/summary.json) ([metadata](run01/metadata.json))
- [run02-varied-u2000](run02-varied-u2000/summary.json) ([metadata](run02-varied-u2000/metadata.json)) ([report ZIP](run02-varied-u2000/report.zip))

The ZIP dashboard's percentile values differ from the JTL in this run.
The table uses the exact p95 verified from raw samples, matching the saved
summary, rather than the dashboard's percentile estimates.
