# CardWiki spike-test results

## Test setup

We tested how deck search and opening respond to a sudden increase in users.
Each execution had 60 seconds before the peak, a 30-second peak and 60 seconds
of recovery. Peak users started within one second; users paused for 500 ms
before each request. The phases ran sequentially with new users in each phase.

The historical `run01` used 5 → 50 → 5 users. The new measurements on
5 November 2026 used 20 → 200 → 20 and 20 → 500 → 20 users, with 15 CSV pairs
across five decks. All phases reached their target user count. Both series
record an Apple A18 Pro with 8 GiB of RAM.

## Results

The table uses steady-state samples: it excludes the first 15 seconds of
the before/recovery phases and the first 11 seconds of the peak, measured
from each phase's first sample. The full peak, including its transition,
was also checked in the raw samples.

| Test | Phase | Users | Average response time | p95 | Requests per second | Errors |
| --- | --- | --- | --- | --- | --- | --- |
| run01 | Before | 5 | 10.25 ms | 19 ms | 9.70 | 0 |
| run01 | Spike | 50 | 9.13 ms | 22 ms | 97.62 | 0 |
| run01 | Recovery | 5 | 6.96 ms | 13 ms | 9.80 | 0 |
| run02-varied-u200 | Before | 20 | 4.34 ms | 11 ms | 39.31 | 0 |
| run02-varied-u200 | Spike | 200 | 5.61 ms | 23 ms | 394.21 | 0 |
| run02-varied-u200 | Recovery | 20 | 4.82 ms | 12 ms | 39.22 | 0 |
| run02-varied-u500 | Before | 20 | 5.38 ms | 15 ms | 39.29 | 0 |
| run02-varied-u500 | Spike | 500 | 10.66 ms | 62 ms | 978.64 | 0 |
| run02-varied-u500 | Recovery | 20 | 4.38 ms | 10 ms | 39.51 | 0 |

The p95 is the time within which at least 95% of measured requests completed.
Requests per second count individual HTTP requests.

## Reflections

**Both new peaks were handled without recorded errors.** The steady peak
throughput was 394.21 requests/s at 200 users and 978.64 at 500 users. The
full phases, including the rapid increase, also recorded no errors. During
the first five seconds of the 500-user peak, p95 was 46 ms; the remaining
steady peak had p95 of 62 ms.

**The larger peak increased response times.** Peak mean latency rose from
5.61 ms at 200 users to 10.66 ms at 500 users, while p95 rose from 23 to 62 ms.
The slowest response during the full 500-user peak was 300 ms. This shows
that the higher burst had a visible latency cost even though requests
continued succeeding.

**Recovery returned close to the initial behavior.** At 200 peak users,
the mean changed from 4.34 ms before the peak to 4.82 ms afterward. At 500,
it changed from 5.38 to 4.38 ms, and p95 fell from 15 to 10 ms. Recovery
throughput was about 39 requests/s at 20 users in both new runs. These
steady recovery values do not determine the exact time needed to recover.

**run01 is a historical reference.** It used fewer users and fixed input,
so its values cannot isolate a software improvement. Each new configuration
was executed once on a shared backend/generator host. Repetitions would help
confirm the response to bursts; these short peaks do not establish
long-term stability or the maximum traffic the system can support.

## Original results

The table uses `summary.json`, checked against the original JTL samples.
The ZIP dashboards were read directly; the original files were left unchanged.
No new tests were run.

- [run01](run01/summary.json) ([metadata](run01/metadata.json))
- [run02-varied-u200](run02-varied-u200/summary.json) ([metadata](run02-varied-u200/metadata.json)) ([report ZIP](run02-varied-u200/report.zip))
- [run02-varied-u500](run02-varied-u500/summary.json) ([metadata](run02-varied-u500/metadata.json)) ([report ZIP](run02-varied-u500/report.zip))
