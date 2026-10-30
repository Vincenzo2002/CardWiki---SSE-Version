# CardWiki API performance tests

These plans replace the short, fixed-loop experiments in PR #18 with timed
workloads. They exercise the same read-only API operations:

- `GET /api/v1/decks?name=Haupt&limit=10&offset=0` (Search Deck).
- `GET /api/v1/decks/1` (Open Deck).

`workload.jmx` is a shared Test Fragment, included by all four plans. Each
virtual user repeats both requests until its Thread Group's duration ends.
There are no additional JMeter plugins. Python 3 and JMeter 5.6.3 or a
compatible newer version are required; the runner supports macOS and Linux.

## Profiles

| Plan | Main phase defaults | Other phases |
| --- | --- | --- |
| `load_test.jmx` | 20 users, 10 s ramp-up, 300 s total | None |
| `stress_test.jmx` | 50 users, 10 s ramp-up, 180 s total | Before and recovery: 5 users, 5 s ramp-up, 60 s each |
| `spike_test.jmx` | 50 users, 1 s ramp-up, 30 s total | Before and recovery: 5 users, 5 s ramp-up, 60 s each |
| `soak_test.jmx` | 20 users, 30 s ramp-up, 3,600 s total | None |

Durations **include ramp-up**. Before, main and recovery groups run sequentially;
in-flight requests can delay the transition by up to the response timeout.
The next group starts new virtual users. These read-only tests do not preserve
authenticated sessions across phases.

The default think time is **500 ms before each request**, approximately one
second per two-request loop plus response times. Keep it identical when
comparing application versions. Defaults are starting points for calibration,
not known traffic requirements or proof of overload. Performance testing is
the umbrella category; a fifth `performance_test.jmx` is not necessary.

## Prepare the backend and fixture

Start the intended backend version yourself using the
[backend instructions](../backend/README.md#how-to-run-it), with JDK 11 and a
known dataset. Do not delete an existing database to make the test work.
Ensure the selected search has results and the selected deck exists; change
`--search` and `--deck-id` if needed. Only the backend on port 8080 is needed.

Record the **full commit of the running application**, not automatically the
commit of the test checkout. The runner stores it as user-declared because
the backend does not expose a version endpoint. For example, in the checkout
used to start the backend:

```sh
git rev-parse HEAD
```

Run the commands below from the repository root. Replace the sample SHA and
dataset description with your actual values:

```sh
APP_COMMIT=0123456789abcdef0123456789abcdef01234567
DATASET='dev dataset, unchanged; Haupt search and deck 1 verified'

# Short installation/fixture check; not a performance measurement.
bash jmeter/run_jmeter.sh load baseline smoke01 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" \
  --purpose validation --users 2 --ramp-up 1 --duration 15 --warmup 0

# Measured load: repeat with 5, 10 and 20 users and a new run ID each time.
bash jmeter/run_jmeter.sh load baseline users5-run01 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" --users 5
bash jmeter/run_jmeter.sh load baseline users10-run01 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" --users 10
bash jmeter/run_jmeter.sh load baseline users20-run01 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" --users 20
```

The CLI validates positive user counts, phase durations, the input fixture and
unique output paths. It sends two preflight requests before measurement.
Every measured response must be HTTP 200 and JSON: search must return a
nonempty page of deck objects, and open must return the configured deck ID
and a name. Redirects are not treated as successful API responses.

Only run a `baseline` measurement against the unoptimized application.
Follow the [existing Git workflow](../README.md#branches-and-improvements)
when bringing the same tests to an optimized version. For that version use
`sse-improvements` as the second argument, with identical workload settings.

## Stress, spike and soak

After confirming the load test's actual concurrency, choose and document a
normal load. Adjust `--base-users` to that value for stress and spike.

```sh
# One stress level with before/after observations.
bash jmeter/run_jmeter.sh stress baseline users50-run01 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" --base-users 5 --users 50

# Abrupt rise to 50 users, then return to 5 users.
bash jmeter/run_jmeter.sh spike baseline peak50-run01 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" --base-users 5 --users 50

# Sustained load for one hour. Choose a load already shown to be sustainable.
bash jmeter/run_jmeter.sh soak baseline users20-run01 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" --users 20
```

For stress testing, increase the main-phase users across separate runs, for
example 20, 50, 100 and 200, adapting the sequence to observed behavior. Inspect
each result and recovery before increasing again. Fifty configured users
alone do not demonstrate saturation. Define the error/latency or resource
criterion used to identify the limit, and distinguish backend saturation
from a saturated load generator. Use the same settings for the before/after
comparison, rather than recalibrating each implementation independently.

For spike testing, compare before, peak and recovery time series, including
the transition; steady-state aggregates alone do not describe a spike.
For soak testing, inspect trends over time. A short validation run cannot
establish endurance or absence of leaks. Monitor backend CPU, memory, GC and
database connections alongside JMeter; JMeter alone does not measure them.
Ideally use a separate load-generator machine. If both processes share a
machine, record that limitation and keep the setup consistent.

## Outputs and interpretation

Each run gets a new directory, and existing runs are never overwritten:

```text
sse-reports/<baseline|sse-improvements>/jmeter/<scenario>_test/<run-id>/
  plans/                 exact JMX snapshots used by this execution
  metadata.json          declared app commit, plan hashes, command, environment
  results.jtl            original samples, thread counts and assertion failures
  jmeter.log             JMeter execution log
  console.log
  summary.json           per-phase and per-endpoint metrics
  report/                generated HTML dashboard (open index.html)
  report-generation.log
  report-console.log
```

`summary.json` separates complete-phase results from steady samples. Steady
samples start after ramp-up plus `--warmup` seconds (default 10), measured
conservatively from the first recorded sample in each phase, and must record
the target `grpThreads`. The runner checks this independently for every
phase. It exits with status 2 if target concurrency and steady samples cannot
be confirmed. Inspect the active-thread time series to confirm the target is
maintained, not merely reached briefly.

The p95 uses the nearest-rank method. Throughput is samples divided by the
observed interval from the first sample start to the last sample end in the
selected subset. An overall average mixes ramps, peaks and recovery; compare
matching phases and endpoints. Errors include failed content assertions.
JMeter process completion is **not** an SLA pass: stress runs can complete
successfully while recording application errors. Nonzero JMeter execution
or report-generation failures return status 1 and retain available artifacts.

Repeat short measured scenarios three times using `run01`, `run02`, and
`run03`; record repetitions for the longer soak test too. Let the system
recover consistently between runs. Compare the same dataset, request mix,
think time, application logging settings, hardware and JVM settings. Preserve
original outputs; do not infer lower energy consumption from shorter latency.
Use an explicit energy measurement for an energy claim.

The runner automatically uses CLI mode, CSV samples and no GUI listeners.
Connection and response timeouts are 5 and 10 seconds. Use `--help` for all
settings. To validate the tooling without storing synthetic artifacts as
project measurements, set `--purpose validation --output-root /tmp/cardwiki-checks`.
Keep synthetic validation artifacts separate from actual CardWiki results.

## Completing PR #18

1. Keep the current fixed-loop plans and nine results as historical preliminary
   measurements. Their configured users do not prove simultaneous concurrency;
   do not rename them into load/stress/spike/soak results.
2. Bring this `jmeter/` directory into the PR branch. The existing PR artifacts
   are not present in the local JMH checkout where this suite was prepared.
   If reorganizing them under `preliminary/`, update their README links too.
3. Run the corrected profiles against the actual baseline application and
   record fresh artifacts under the structure above. Add a `summary.md` with
   concurrency evidence, per-scenario findings, saturation/recovery criteria
   and limits. Never invent results for an unexecuted profile.
4. Reuse these plans for improvements. The short original runs cannot serve
   as a directly comparable baseline for the new timed workloads.

References: [JMeter Thread Groups and timers](https://jmeter.apache.org/usermanual/test_plan.html),
[JMeter best practices](https://jmeter.apache.org/usermanual/best-practices.html),
and the [reference repository's test organization](https://github.com/redyz13/SSE-backend/tree/main/jmeter).
