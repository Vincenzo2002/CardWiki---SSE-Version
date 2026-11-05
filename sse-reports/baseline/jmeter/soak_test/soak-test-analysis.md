# CardWiki soak-test results

## Test setup

We tested deck search and opening under a sustained load. Both runs were
configured for one hour, with a 30-second ramp-up and a 500 ms pause before
each request. The main table excludes the initial 40 seconds and uses
samples at the target user count.

The historical `run01` used 20 users and a fixed search/deck pair. The new
run, recorded on 5 November 2026, used 100 users and 15 CSV pairs across five
decks. Both series record an Apple A18 Pro with 8 GiB of RAM. The old run
contains a long sampling gap, discussed below.

## Results

| Test | Users | Average response time | p95 | Requests per second | Errors |
| --- | --- | --- | --- | --- | --- |
| run01 | 20 | 5.55 ms | 11 ms | 26.45 | 0 |
| run02-varied-u100 | 100 | 6.72 ms | 18 ms | 195.36 | 0 |

The p95 is the time within which at least 95% of the measured requests
completed. To check whether performance changed during the new run, we also
compared its first 15 minutes after warmup with its last 15 minutes:

| Part of the new run | Average response time | p95 | Requests per second |
| --- | --- | --- | --- |
| First 15 minutes after warmup | 5.05 ms | 15 ms | 196.18 |
| Last 15 minutes | 11.97 ms | 35 ms | 193.61 |

## Reflections

**The new run sustained 100 users for about one hour without recorded
errors.** It contains 700,396 requests overall, with steady-state throughput
of 195.36 requests/s. The raw samples show no long interruption: the largest
gap between request starts was below one second, and the interior samples
maintained the target user count.

**Responses became slower toward the end.** The mean rose from 5.05 ms
in the early window to 11.97 ms in the final window, and p95 increased from
15 to 35 ms. Throughput changed only slightly, from 196.18 to 193.61
requests/s. This indicates a late-run slowdown worth checking in another
execution, even though requests continued succeeding.

**These results do not prove a memory leak.** No matching CPU, memory or GC
measurements were supplied, and JMeter shared the backend's machine. A
resource trace and a repeated run would help explain the slowdown. The
slowest steady-state response was 1,089 ms, so the low mean does not describe
every request.

**The historical run has a continuity problem.** Its JTL contains a gap of
about 17 minutes without samples. Its reported throughput of 26.45 requests/s
includes that gap, so it should not be treated as evidence of a continuous
one-hour load or directly scaled against the new result. The cause of the
gap is unknown; zero recorded request errors does not detect missing traffic.

The input data and user count also changed between the runs. The new test
supports an observation of one-hour behavior for these read-only operations,
but does not establish stability for longer periods or other features.

## Original results

The table uses `summary.json`, checked against the original JTL samples.
The ZIP dashboards were read directly; the original files were left unchanged.
No new tests were run.

- [run01](run01/summary.json) ([metadata](run01/metadata.json))
- [run02-varied-u100](run02-varied-u100/summary.json) ([metadata](run02-varied-u100/metadata.json)) ([report ZIP](run02-varied-u100/report.zip))
