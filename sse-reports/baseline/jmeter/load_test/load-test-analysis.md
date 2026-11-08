# CardWiki load-test results

## Test setup

We tested deck search and opening with JMeter. Each test lasted five minutes,
with a 10-second ramp-up and a 500 ms pause before each request. The table
excludes the initial 20 seconds and uses samples at the configured user count.

The historical `run01` used 20 users and a fixed search/deck pair. The new
measurements, recorded on 5 November 2026, used 50, 200 and 500 users with
15 CSV pairs across five decks. All runs reached their configured user count.
The metadata records an Apple A18 Pro with 8 GiB of RAM for both series.

## Results

| Test | Users | Average response time | p95 | Requests per second | Errors |
| --- | --- | --- | --- | --- | --- |
| run01 | 20 | 7.03 ms | 17 ms | 39.10 | 0 |
| run02-varied-u050 | 50 | 19.97 ms | 72 ms | 95.34 | 0 |
| run02-varied-u200 | 200 | 31.16 ms | 151 ms | 373.89 | 0 |
| run02-varied-u500 | 500 | 7.98 ms | 31 ms | 977.48 | 0 |

The p95 means that at least 95% of the measured requests completed within
that time. Requests per second count individual HTTP requests; each
search-and-open cycle contains two requests.

## Reflections

**Throughput increased with the number of users.** In the new tests,
it rose from 95.34 requests/s at 50 users to 977.48 at 500 users. No HTTP or
response-validation errors were recorded, and the target user counts were
maintained after startup.

**Response times did not increase consistently with load.** The 200-user
run had the highest mean (31.16 ms) and p95 (151 ms), while the 500-user run
had a lower mean of 7.98 ms and p95 of 31 ms. We cannot conclude that adding
users improves performance: each level was executed only once, and factors
such as JVM warming or background activity may have affected the results.
Repeating the same configurations would help explain this difference.

**The average hides some slower responses.** At 200 users, the slowest
steady-state response was 1,375 ms, compared with 890 ms at 50 users and
831 ms at 500 users. Search was slower than opening a deck in all new runs;
at 500 users, their means were 9.03 ms and 6.94 ms respectively.

**run01 is a historical reference, not an optimization comparison.**
Although the recorded hardware is the same model, the user count, selected
data, test date and workload changed. These baseline results do not isolate
a software improvement.

The tests cover short, read-only activity with pauses between requests.
They do not identify maximum capacity or establish long-term stability.
Backend and JMeter shared the same host. Opening a deck returns its metadata,
not all the cards it contains.

## Original results

The table uses `summary.json`, checked against the original JTL samples.
The ZIP dashboards were read directly; the original files were left unchanged.
No new tests were run.

- [run01](run01/summary.json) ([metadata](run01/metadata.json))
- [run02-varied-u050](run02-varied-u050/summary.json) ([metadata](run02-varied-u050/metadata.json)) ([report ZIP](run02-varied-u050/report.zip))
- [run02-varied-u200](run02-varied-u200/summary.json) ([metadata](run02-varied-u200/metadata.json)) ([report ZIP](run02-varied-u200/report.zip))
- [run02-varied-u500](run02-varied-u500/summary.json) ([metadata](run02-varied-u500/metadata.json)) ([report ZIP](run02-varied-u500/report.zip))

Some dashboard percentiles differ from the raw samples. For example, the
200-user ZIP shows a whole-run p95 of 15 ms, while the JTL and summary give
144 ms. The table above uses the independently verified steady-state p95
of 151 ms; dashboard percentile values were not substituted for it.
