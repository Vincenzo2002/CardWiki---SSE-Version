# CardWiki JMeter tests

These tests simulate users searching for and opening decks. Use JDK 11,
Maven, Python 3 and JMeter 5.6.3 or a compatible newer version.

## Start the backend

From the repository root, open a terminal and run:

```sh
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Wait until the backend is running on `http://localhost:8080`. The frontend
is not needed. See the [backend setup](../backend/README.md) for JDK setup.

In a second terminal, from the repository root:

```sh
APP_COMMIT=$(git rev-parse HEAD)
DATASET="Database dev; input from jmeter/data/varied.csv"
```

Use the SHA of the backend actually running. The command above is correct
if both terminals use the same checkout and the branch/code has not changed
after startup. Update the dataset description to match your database.

## Input data

[`data/varied.csv`](data/varied.csv) contains 15 search/deck pairs across
five decks. The runner reads a new pair each loop and restarts the CSV when
it reaches the end. Before starting JMeter, it checks that the searches and
IDs match the running database. If they do not, update the CSV using the
[fixture notes](data/README.md).

Without `--data-file`, tests use the original fixed search `Haupt` and deck `1`.

## Run the tests

First, check the setup with a short 15-second run. Its results are saved in
`/tmp/cardwiki-checks`, separately from project measurements:

```sh
bash jmeter/run_jmeter.sh load baseline varied-smoke01 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" \
  --data-file jmeter/data/varied.csv \
  --purpose validation --output-root /tmp/cardwiki-checks \
  --users 2 --ramp-up 1 --duration 15 --warmup 0
```

Then run the following commands **one at a time**, inspecting results and
letting the backend recover between tests. Each request has a 500 ms pause;
the stated durations include ramp-up and exclude report-generation time.

```sh
# Load: 50 users, about 5 minutes
bash jmeter/run_jmeter.sh load baseline run02-varied-u050 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" \
  --data-file jmeter/data/varied.csv --users 50

# Load: 200 users, about 5 minutes
bash jmeter/run_jmeter.sh load baseline run02-varied-u200 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" \
  --data-file jmeter/data/varied.csv --users 200

# Load: 500 users, about 5 minutes
bash jmeter/run_jmeter.sh load baseline run02-varied-u500 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" \
  --data-file jmeter/data/varied.csv --users 500

# Stress: 20 -> 2,000 -> 20 users, about 5 minutes
bash jmeter/run_jmeter.sh stress baseline run02-varied-u2000 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" \
  --data-file jmeter/data/varied.csv --base-users 20 --users 2000

# Spike: 20 -> 200 -> 20 users, about 2.5 minutes
bash jmeter/run_jmeter.sh spike baseline run02-varied-u200 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" \
  --data-file jmeter/data/varied.csv --base-users 20 --users 200

# Spike: 20 -> 500 -> 20 users, about 2.5 minutes
bash jmeter/run_jmeter.sh spike baseline run02-varied-u500 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" \
  --data-file jmeter/data/varied.csv --base-users 20 --users 500

# Soak: 100 users, about 1 hour
bash jmeter/run_jmeter.sh soak baseline run02-varied-u100 \
  --app-commit "$APP_COMMIT" --dataset "$DATASET" \
  --data-file jmeter/data/varied.csv --users 100
```

Keep the computer awake during the soak test. On macOS, prefix its command
with `caffeinate -i`. To repeat any test, choose a new run ID, such as
`run03-varied-u050`: existing output directories are never overwritten.
Keep the historical `run01` directories unchanged.

## Results

Each test saves its files under:

```text
sse-reports/baseline/jmeter/<scenario>_test/<run-id>/
```

- `summary.json`: response times, p95, requests per second, errors and concurrency.
- `report/index.html`: dashboard with charts and detailed results.
- `results.jtl`: individual request samples.
- `metadata.json` and `plans/`: configuration, environment and input copies.

Open the HTML dashboard in a browser. Check errors and active users as well
as response times: completing the test does not automatically mean its
performance is acceptable. The p95 is the response time within which at
least 95% of measured requests finish.

For optimized-version measurements, start the optimized backend, update
`APP_COMMIT` and replace `baseline` with `sse-improvements` in the commands.
Keep the same database, CSV, workload settings and hardware. This argument
only selects where results are saved; it does not switch the running backend.

For other options, use `bash jmeter/run_jmeter.sh --help`.
