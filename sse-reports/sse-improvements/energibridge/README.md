# EnergiBridge – Improved version

Energy measurements of CardWiki after the improvements from issue #29, compared
against the baseline scenario 2 (varied input, higher load) documented in
[`../../baseline/energibridge/README.md`](../../baseline/energibridge/README.md).

**Summary.** With the same machine, workload, data, database, tool configuration
and number of runs, the improved version uses **36.6 % less CPU energy** in
`load` and **44.4 % less energy per request** in `stress`. Both changes are well
within the Blue Angel criterion (≤ +10 %). API responses are unchanged.

## Findings in the baseline

The measured workload searches for decks (`GET /api/v1/decks?name=…`) and opens
one (`GET /api/v1/decks/{id}`). Reviewing that path and the baseline results
showed three inefficiencies:

1. **One log line per request.** Every endpoint logged each access at `INFO`,
   to the console and to `./log/backend.log`. Together with finding 2, the
   baseline wrote ≈ 1.5 lines (≈ 260 bytes) per request: ≈ 400,000 lines (≈ 70 MB)
   per `stress` run, with log rotation and gzip compression every few minutes.
   The terminal showing the backend output (`WindowsTerminal`, `OpenConsole`)
   and the window compositor (`dwm`) used 63–79 s of CPU time per run (`*.procs.txt`).
2. **In-memory pagination of the search.** `DeckRepository.findByNameContainingIgnoreCase`
   fetch-joined the `categories` collection while paging. Hibernate therefore
   loaded every matching deck and paged in memory, logging
   `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!`
   as a `WARN` on every search. The query also joined the `users` table,
   although the response only needs the creator's id.
3. **Read-write transactions for read-only lookups.** `findOneOrThrow` and
   `searchByName` used default transactions, so Hibernate performed dirty
   checking and flushing on every lookup.

## Changes

Branch `perf/debug-access-logs`, created from `master` (`8c7d10e`):

| Commit | Change |
|---|---|
| `fa3796c` | Endpoint access logs use `DEBUG` instead of `INFO` (42 calls in 7 endpoints). They are off with the default root level `INFO` and can be re-enabled per package. Error logs in `GlobalExceptionHandler` are unchanged. This also stops logging the user's token at `INFO` in `UserEndpoint`. |
| `9d4291c` | The deck search fetches no collection, so `LIMIT`/`OFFSET` run in SQL and the `users` join disappears. `SimpleDeckService` initializes the categories inside the transaction, and `@BatchSize(size = 100)` on `Deck.categories` loads them for the whole page in one query. `findOneOrThrow` and `searchByName` use `@Transactional(readOnly = true)`. |
| `c3402d3` | `energibridge_run.ps1` accepts custom labels (`-Label improved`). Measurement tooling only. |

**Functional check.** The backend was started from `master` and from the branch,
and the responses of the 15 CSV searches, a paged search (`name=e&limit=2&offset=1`)
and decks 1–5 were compared: all 21 responses are **identical**, including the
categories. For those 21 requests, `master` wrote 37 log lines and the branch wrote
none. The backend test suite passes except two tests (`ImageMappingTest`,
`RevisionMappingTest`) that fail on Windows on `master` as well, with
`InvalidPathException: UNC path is missing sharename`; they are unrelated to
these changes.

## Measurement

Same procedure and configuration as the baseline scenario 2:
[`energibridge_run.ps1`](../../baseline/energibridge/energibridge_run.ps1)
with `-Label improved`, 10 runs per plan, calm gate ≤ 2.5 W, EnergiBridge 0.0.7
at 1000 ms, same laptop (AMD Ryzen 5 7520U, Windows 11, Balanced plan, mains power),
same CSV (SHA-256 recorded in `environment.txt`) and the same persistent H2
database. The backend was started natively with `mvn spring-boot:run` (not Docker),
as in the baseline.

| Item | Value |
|---|---|
| Commit measured | `9d4291c86a845450ef9118174a714c1280b28a8b` (`perf/debug-access-logs`) |
| Dates | 2026-11-12: `load` from 01:52, `stress` from 07:43 CET |
| Results | `improved/load-varied-u200/`, `improved/stress-varied-u2000/` |

All 20 runs were valid, with no failed requests. The calm gate held back
`load/idle_start` three times (4.3–8.5 W) until background activity stopped. Every
other measurement started after the first cool-down.

## Results

Medians over 10 runs. Confidence intervals (bootstrap) and p-values (permutation
test) come from `analyze_energibridge.py --compare`; full tables are in each
folder's `comparison.md`.

### `load` – 200 users, 5 minutes

| Metric | Baseline | Improved | Change | 95 % CI |
|---|---|---|---|---|
| Total CPU energy per run | 1,597.3 J | **1,012.7 J** | **−36.6 %** | [−37.4, −35.0] |
| Mean power | 5.24 W | 3.32 W | −36.6 % | [−37.4, −35.0] |
| Peak 30 s power | 9.0–9.8 W | 6.5–7.6 W | | |
| Successful requests | 115,188 | 114,648 | −0.5 % | |
| Total energy per request | 13.87 mJ | **8.83 mJ** | **−36.3 %** | [−37.1, −34.7] |
| Incremental energy per request | 9.64 mJ | 4.25 mJ | −55.9 % | [−56.9, −53.6] |
| Mean CPU usage | 27 % | 21 % | | |
| CPU time per run: `java` / terminal + `dwm` | 372 s / 63 s | 304 s / 4 s | | |
| CV of total energy | 1.1 % | 2.2 % | | |

`load` is limited by think time, so both versions process the same number of
requests (≈ 383 per second). The whole difference is energy saved for the same work.

### `stress` – 20 → 2,000 → 20 users

| Metric | Baseline | Improved | Change | 95 % CI |
|---|---|---|---|---|
| Total CPU energy per run | 3,662.2 J | 3,574.4 J | −2.4 % | [−3.3, −2.2] |
| Mean power | 11.66 W | 11.03 W | −5.4 % | [−6.2, −5.2] |
| Peak 30 s power | 17.9–18.0 W | 17.2–17.4 W | | |
| Successful requests | 268,822 | **468,042** | **+74 %** | |
| Throughput in the 2,000-user phase | ≈ 1,450–1,500 req/s | ≈ 2,450–2,570 req/s | | |
| Mean response time in the 2,000-user phase | ≈ 850 ms | ≈ 270 ms | | |
| Total energy per request | 13.74 mJ | **7.64 mJ** | **−44.4 %** | [−44.8, −43.7] |
| Incremental energy per request | 11.56 mJ | 6.49 mJ | −43.8 % | [−44.3, −43.1] |
| CPU time per run: `java` / terminal + `dwm` | 1,087 s / 79 s | 1,108 s / 5 s | | |
| CV of total energy | 1.4 % | 0.4 % | | |

As in the baseline, the laptop is saturated and runs close to its power limit, so
energy per run barely changes. The saved CPU time is used to serve 74 % more
requests, with response times about three times shorter. Energy per request is the
relevant metric here, as stated in the baseline README.

## Interpretation

- **Logging was the main cost.** The terminal and `dwm` CPU time per run fell from
  63–79 s to 4–5 s, and the backend's own CPU time in `load` fell by 18 %.
  Formatting, writing, rotating and compressing ≈ 1.5 log lines per request, and
  rendering them in a terminal, cost more than the database work of these simple
  requests.
- **The query and transaction changes remove avoidable work** (in-memory paging, an
  unnecessary join, dirty checking) and the last per-request `WARN` line. With only
  five decks their direct database saving is small, but it grows with the number of
  decks, because the old search loaded every matching deck to return one page.
- **Attribution.** The improved version is `master` plus the changes above, so it
  also contains the team's earlier optimizations (#20, #24, #25). On the measured
  path, those mainly add `final` modifiers and do not change behaviour or queries,
  and the process logs show where the energy was saved. Still, without a separate
  series of `master` before these changes, the saving cannot be split exactly
  between this PR and the earlier ones.

## Limitations

The limitations of the baseline README apply: CPU package energy only, the whole
system is measured (JMeter included), Windows-only RAPL driver flagged by Defender,
single machine. In addition:

- **Run duration in `stress`.** Each EnergiBridge window covers the whole JMeter
  process. In the improved series it lasts ≈ 324 s instead of ≈ 314 s (+3 %) with
  identical plan phases, probably because JMeter needs longer to stop after
  processing more requests. This slightly *increases* the measured energy per run
  of the improved version, so the reported saving is conservative.
- **`stress/run09`** processed fewer requests (432,684; the others 464,876–474,394),
  with a temporary rise in response times. Its energy is in line with the other
  runs, and it is kept as valid.
- **Idle drift.** The reference idle power is 1.72 W (`load`) and 1.65 W (`stress`)
  against 1.60 W and 1.85 W in the baseline. This only affects the *incremental*
  metrics. Total energy and total energy per request do not depend on it.
- **Logging in production.** Access logs are no longer written by default. If
  access logging is needed, a dedicated access log (e.g. Tomcat's) or `DEBUG` for
  the endpoint package can be enabled explicitly.

## Files

```
sse-improvements/energibridge/
├── README.md                    this report
└── improved/
    ├── load-varied-u200/
    └── stress-varied-u2000/
        ├── environment.txt      machine, versions, plan parameters, commit
        ├── analysis.md          series report
        ├── comparison.md        baseline vs improved (CI, p-values, Blue Angel check)
        ├── idle_start.* idle_end.* runNN.*   EnergiBridge CSVs, summaries, process logs
        ├── input.csv  cooldown.txt  quality.txt
        └── jtl.zip              JMeter results (stress: jtl-part1.zip, jtl-part2.zip)
```

Extract the `.jtl` archives into the series folder before re-running the analysis:

```bash
python ../../baseline/energibridge/analyze_energibridge.py ../../baseline/energibridge/baseline/load-varied-u200 --compare improved/load-varied-u200
python ../../baseline/energibridge/analyze_energibridge.py ../../baseline/energibridge/baseline/stress-varied-u2000 --compare improved/stress-varied-u2000
```
