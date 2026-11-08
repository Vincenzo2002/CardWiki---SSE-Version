# EnergiBridge – Baseline energy measurements

Software-based CPU energy measurements of the **baseline** version of CardWiki
under the team's JMeter workloads (`load` and `stress`), taken with
[EnergiBridge](https://github.com/tdurieux/EnergiBridge). These results are the
reference for the Blue Angel criterion: an update must not increase energy
consumption by more than 10 % compared to this baseline.

Two scenarios were measured. Both remain valid; the improved version must be
compared against the scenario measured with the same configuration.

| Scenario | Input | `load` | `stress` | Folders |
|---|---|---|---|---|
| 1 – fixed input | search `Haupt`, deck `1` | 20 users | 5 → 50 → 5 users | `baseline/load/`, `baseline/stress/` |
| 2 – varied input, higher load | 15 search/deck pairs from [`jmeter/data/varied.csv`](../../../jmeter/data/varied.csv) | 200 users | 20 → 2,000 → 20 users | `baseline/load-varied-u200/`, `baseline/stress-varied-u2000/` |

Scenario 2 aligns the energy measurements with the updated JMeter tests (PR #27)
and is the one the team uses to compare the baseline and optimized versions.

## Analysed version

| Item | Value |
|---|---|
| Branch measured | `feature/energibridge-baseline` (created from `baseline`, no application code changes) |
| Commit | Scenario 1: `7f3462b10b868990cb518d2448826aff041101b9`. Scenario 2: `8c2524db839080a28e09847b3d63a123dc6518ef`, which only adds reports and test tooling (`backend/` and `frontend/` identical to `7f3462b`) |
| Application code | Identical to commit `4cd6c67` used in the team's JMeter reports |
| Backend profile | `dev` (persistent H2 file database, not regenerated between series) |
| Measurement date | Scenario 1: 2026-11-03 (stress 10:25, load 13:43 CET). Scenario 2: 2026-11-08 (load 16:22, stress 17:36 CET) |

## Measurement environment

| Item | Value |
|---|---|
| Machine | Laptop, AMD Ryzen 5 7520U with Radeon Graphics |
| OS | Windows 11 Home 10.0.26300 |
| Power scheme | Balanced (`381b4222-f694-41f0-9685-ff5bb260df2e`) |
| EnergiBridge | 0.0.7, sampling interval 1000 ms |
| Java | OpenJDK 11.0.32.1 |
| JMeter | 5.6.3, non-GUI mode, running on the same machine |

Conditions during every series:

- All other applications closed (only the backend, JMeter and the terminal running the script).
- OneDrive synchronisation and Windows Update paused; sleep and screen-off disabled, adaptive brightness off.
- Within each scenario, the same backend process served both series (no restart between `load` and `stress`).
- Scenario 2: laptop on mains power with the battery fully charged.

The exact values recorded by the script are in each series' `environment.txt`.

## Procedure

Each series is driven by [`energibridge_run.ps1`](energibridge_run.ps1), run
from the repository root in an **administrator** PowerShell (EnergiBridge needs it
for the RAPL driver):

```powershell
# Scenario 2 (default): varied CSV input, load 200 users, stress 20 -> 2000 -> 20 users
.\sse-reports\baseline\energibridge\energibridge_run.ps1 -Plan load
.\sse-reports\baseline\energibridge\energibridge_run.ps1 -Plan stress
# Scenario 1: original fixed input and user counts, with the original quality filter
.\sse-reports\baseline\energibridge\energibridge_run.ps1 -Plan load -Data "" -MaxBinWatts 5.5
```

Add `-Label improved` for the optimized version (output under
`sse-reports/sse-improvements/energibridge/improved/`) and `-Smoke -Runs 1` for a
short setup check (output in a `*_smoke` folder, not committed).

1. Check that the EnergiBridge driver service (`rapl`) is running and the backend answers on `http://localhost:8080`.
2. Scenario 2: check that every CSV search returns its deck on the first result page and that the deck can be opened (same check as `jmeter/run_jmeter.py`). The CSV is copied to the series folder as `input.csv` and its SHA-256 is recorded in `environment.txt`.
3. One unmeasured JMeter warm-up run (JIT, caches, DB pages).
4. `idle_start`: system idle measurement lasting as long as one run (310 s).
5. 10 measured runs. EnergiBridge wraps the whole JMeter process (`energibridge -i 1000 --summary -o runNN.csv -- jmeter ...`).
6. `idle_end`: second idle measurement, to detect drift.

Workloads (JMeter plans from `jmeter/`, `think_ms=500`):

| Scenario | Plan | Properties | Workload duration |
|---|---|---|---|
| 1 | `load` | 20 users, ramp-up 10 s, 300 s; `search=Haupt deck_id=1` | 300 s |
| 1 | `stress` | 5 users for 60 s → 50 users for 180 s → 5 users for 60 s (ramp-ups 5 s / 10 s); `search=Haupt deck_id=1` | 300 s |
| 2 | `load` | 200 users, ramp-up 10 s, 300 s; `data_mode=csv` | 300 s |
| 2 | `stress` | 20 users for 60 s → 2,000 users for 180 s → 20 users for 60 s (ramp-ups 5 s / 10 s); `data_mode=csv` | 300 s |

In scenario 2, each JMeter thread reads the next CSV pair on every iteration
(recycled at the end of the file), and the `.jtl` records the search and deck
actually used by each sample.

**Quality control.** Every measurement records the CPU time consumed by the 8
busiest processes (`*.procs.txt`), so that background interference can be
checked afterwards. The rest of the quality control differs between scenarios:

- *Scenario 1 – in-run filter.* After each measurement the script computes the
  average CPU power in consecutive 30 s windows. If any window exceeds 5.5 W (a
  sign of background activity, as seen in discarded earlier attempts with 8–12 W
  bursts), the measurement is moved to `rejected/` and repeated, up to 3 attempts.
  All 10 runs and both idle measurements were accepted on the first attempt.
- *Scenario 2 – calm gate.* With 200 and 2,000 users the CPU legitimately reaches
  9–18 W, so a fixed in-run threshold would reject valid runs. Instead, the 30 s
  cool-down before each measurement is itself measured, and the measurement only
  starts when its mean power is ≤ 2.5 W (idle is ≈ 1.6 W). Otherwise the script
  waits and measures another cool-down, up to 10 times (`cooldown.txt`). The peak
  30 s power of every accepted measurement is logged in `quality.txt`. The gate
  delayed `stress/run04` three times (2.7–4.1 W) before it started; every other
  measurement started after the first cool-down.

`rejected/` stayed empty in all series and is therefore not in the repository.

## Analysis

[`analyze_energibridge.py`](analyze_energibridge.py) (Python standard library only):

- Energy = last − first value of the cumulative counter `CPU_ENERGY (J)` (not a sum of power samples), cross-checked against EnergiBridge's `--summary` output.
- Incremental energy = total energy − reference idle power × duration. The reference idle power is the mean of `idle_start` and `idle_end`.
- Successful requests are counted from the run's `.jtl` within the EnergiBridge time window.
- A run is invalid if the counter resets, the CSV is truncated, `--summary` is missing, or more than 0.1 % of requests fail.

```bash
python analyze_energibridge.py baseline/load-varied-u200 --out baseline/load-varied-u200/analysis.md
python analyze_energibridge.py baseline/stress-varied-u2000 --out baseline/stress-varied-u2000/analysis.md
python analyze_energibridge.py baseline/stress-varied-u2000 --profile idle_start,idle_end   # power per 30 s
```

> The `.jtl` files are stored compressed as `jtl.zip` in each series folder
> (uncompressed: ≈ 25 MB per scenario 1 series, 171 MB for `load-varied-u200`,
> 422 MB for `stress-varied-u2000`). **Extract `jtl.zip` into the series folder
> before re-running the analysis**, otherwise every run is reported as invalid
> (`no .jtl file`).

## Results – scenario 2 (varied input, higher load)

Medians over 10 of 10 valid runs, no failed requests. Full tables:
[`baseline/load-varied-u200/analysis.md`](baseline/load-varied-u200/analysis.md),
[`baseline/stress-varied-u2000/analysis.md`](baseline/stress-varied-u2000/analysis.md).

| Metric | `load` (200 users) | `stress` (20 → 2,000 → 20 users) |
|---|---|---|
| Total CPU energy per run (J) | **1,597.3** (CV 1.1 %) | **3,662.2** (CV 1.4 %) |
| Mean power (W) | 5.24 | 11.66 |
| Peak 30 s power (W) | 9.0–9.8 | 17.9–18.0 |
| Mean CPU usage | 27 % | 63 % |
| Reference idle power (W) | 1.60 | 1.85 (see below) |
| Idle drift (`idle_end` vs `idle_start`) | −4.1 % | −20.2 % |
| Incremental energy (J) | 1,109.7 (CV 1.6 %) | 3,082.1 (CV 1.7 %) |
| Successful requests per run | 115,188 (CV < 0.1 %) | 268,822 (CV 1.0 %) |
| Total energy per request (mJ) | **13.87** | **13.74** |
| Incremental energy per request (mJ) | 9.64 | 11.56 |

Observations:

- Both series are very stable (CV ≤ 1.4 % on total energy), so differences far below the 10 % Blue Angel margin can be detected.
- **`load` is limited by think time, not by the CPU.** Mean response time is ≈ 5 ms and throughput stays at ≈ 383 requests/s, close to the maximum allowed by 200 users with a 500 ms pause. The number of requests is practically fixed, so an optimization should show up directly as lower energy per run.
- **`stress` saturates the laptop.** During the 2,000-user phase, mean response time is ≈ 850 ms (max ≈ 3.6 s), with ≈ 1,450–1,500 requests/s. In every run, the peak 30 s power is 17.9–18.0 W, which appears to be the CPU's sustained power limit. Energy per run is therefore capped. An optimization would mainly show up as more requests per run and lower **energy per request**, so that is the metric to compare for `stress`.
- **Idle drift in `stress`.** `idle_start` contains a burst of background activity between 30 s and 90 s (up to 4.5 W, with `MsMpEng` (Microsoft Defender) and `servicehost` as top CPU consumers in `idle_start.procs.txt`). The rest of the measurement is at ≈ 1.55 W, like `idle_end` (1.64 W). The burst only affects the reference idle power used for *incremental* energy: using `idle_end` alone would raise the incremental energy by ≈ 2 %. Total energy and energy per request are not affected.
- In both series, `java` (backend and JMeter) accounts for almost all CPU time during runs.

## Results – scenario 1 (fixed input)

Medians over 10 of 10 valid runs. Full tables: [`baseline/stress/analysis.md`](baseline/stress/analysis.md), [`baseline/load/analysis.md`](baseline/load/analysis.md).

| Metric | `stress` | `load` |
|---|---|---|
| Total CPU energy per run (J) | **767.5** (CV 3.0 %) | **657.0** (CV 3.4 %) |
| Mean power (W) | 2.52 | 2.16 |
| Reference idle power (W) | 1.64 | 1.56 |
| Idle drift (`idle_end` vs `idle_start`) | +7.0 % | −1.8 % |
| Incremental energy (J) | 266.9 (CV 8.5 %) | 182.2 (CV 12.2 %) |
| Successful requests per run | 18 138 | 11 500 |
| Total energy per request (mJ) | **42.32** | **57.04** |
| Incremental energy per request (mJ) | 14.71 | 15.82 |

Observations:

- Both series are stable (CV ≈ 3 % on total energy), well below the 10 % Blue Angel margin, so a later comparison can detect differences of that size.
- Idle power accounts for roughly two thirds of the total energy of a run. The incremental energy is the part attributable to the workload, but its relative dispersion is higher because it is a difference of two measured values.
- `stress` processes about 58 % more requests than `load` for only 17 % more energy, so the energy per request is lower under higher load (better amortisation of the idle baseline).
- In the process logs, `java` (backend and JMeter) is the largest CPU consumer during runs. The remaining CPU time comes from usual Windows services (`svchost`, `dwm`, `MsMpEng`...).

## Scenario 1 vs scenario 2

Both scenarios measure the **same baseline code**. This comparison is not an
optimization result. It shows how the baseline's energy consumption scales with
the workload. The scenarios differ in user counts, input data and date (see the
tables above), so differences cannot be attributed to a single factor.

| Metric (median per run) | `load` S1 → S2 | Change | `stress` S1 → S2 | Change |
|---|---|---|---|---|
| Users (main phase) | 20 → 200 | ×10 | 50 → 2,000 | ×40 |
| Successful requests | 11,500 → 115,188 | ×10.0 | 18,138 → 268,822 | ×14.8 |
| Total CPU energy (J) | 657.0 → 1,597.3 | ×2.4 | 767.5 → 3,662.2 | ×4.8 |
| Mean power (W) | 2.16 → 5.24 | ×2.4 | 2.52 → 11.66 | ×4.6 |
| Mean CPU usage | ≈ 7 % → 27 % | | ≈ 8 % → 63 % | |
| Idle share of total energy | 72 % → 31 % | | 65 % → 16 % | |
| Total energy per request (mJ) | 57.04 → 13.87 | −76 % | 42.32 → 13.74 | −68 % |
| Incremental energy per request (mJ) | 15.82 → 9.64 | −39 % | 14.71 → 11.56 | −21 % |
| CV of total energy | 3.4 % → 1.1 % | | 3.0 % → 1.4 % | |

Observations:

- **Energy grows much less than the work done.** Ten times more requests in `load` cost only 2.4 times more energy, and 14.8 times more requests in `stress` cost 4.8 times more. Energy per request therefore falls by 68–76 %.
- **Most of that gain is the idle baseline being shared by more requests.** In scenario 1, idle power accounts for 65–72 % of each run's energy. In scenario 2, it accounts for only 16–31 %.
- **The workload's own energy per request also falls**, by 39 % in `load` and 21 % in `stress`. A plausible explanation, not verified here, is that a busier JVM and CPU process each request more efficiently (fewer wake-ups from idle states, warmer caches).
- **Saturation stops this trend.** In scenario 2, `stress` costs more incremental energy per request than `load` (11.56 vs 9.64 mJ). With 2,000 users the CPU reaches its power limit (≈ 18 W), response times rise to ≈ 850 ms, and the backend and JMeter compete for the CPU.
- **Scenario 2 is also statistically tighter** (CV 1.1–1.4 % instead of 3.0–3.4 %). The workload's signal is much larger than background noise, and the calm gate keeps background activity out of the runs. This makes scenario 2 better suited to detect small differences between the baseline and the improved version.

## Comparing with the improved version

Use the same machine, scenario, CSV, database, number of runs and script
(`-Label improved`, output in `sse-reports/sse-improvements/energibridge/improved/`).
Reference values: total energy per run for `load`, and energy per request for
both plans, especially `stress`. Then compare:

```bash
python analyze_energibridge.py baseline/load-varied-u200 --compare ../../sse-improvements/energibridge/improved/load-varied-u200
python analyze_energibridge.py baseline/stress-varied-u2000 --compare ../../sse-improvements/energibridge/improved/stress-varied-u2000
```

## Files

```
energibridge/
├── README.md
├── energibridge_run.ps1        measurement script (Windows PowerShell)
├── analyze_energibridge.py     analysis script
└── baseline/
    ├── load/  stress/                              scenario 1
    ├── load-varied-u200/  stress-varied-u2000/     scenario 2
    │   ├── environment.txt     machine, versions, plan parameters, commit
    │   ├── analysis.md         generated report
    │   ├── idle_start.* idle_end.*
    │   ├── runNN.csv           EnergiBridge samples (1 s)
    │   ├── runNN.summary.txt   EnergiBridge --summary output
    │   ├── runNN.procs.txt     CPU time of the 8 busiest processes
    │   ├── jtl.zip             JMeter results (runNN.jtl, warmup.jtl)
    │   ├── input.csv           scenario 2: copy of the CSV used
    │   ├── cooldown.txt        scenario 2: calm gate measurements
    │   └── quality.txt         scenario 2: peak 30 s power per measurement
```

## Limitations

- **Platform.** EnergiBridge on Windows reads the CPU energy counters through the `LibreHardwareMonitor.sys` (WinRing0) kernel driver. Microsoft Defender flags it as `VulnerableDriver:WinNT/Winring0`, so it was only installed for the measurements and must be removed afterwards. The course material covers EnergiBridge on macOS and Linux only; the Windows setup is not part of it.
- **CPU energy only.** Only the CPU package counter (`CPU_ENERGY`) is measured. Display, memory, disk, network and the rest of the platform are not included.
- **Whole-system measurement.** EnergiBridge measures the whole machine, not the application. JMeter runs on the same machine, so its energy is included; with 2,000 JMeter threads, the load generator and the backend compete for the same CPU. Background activity is mitigated (closed apps, paused services, quality control) but not eliminated, as the `idle_start` burst of the scenario 2 `stress` series shows.
- **Power limit under stress.** In scenario 2 `stress`, the CPU runs at its sustained power limit (≈ 18 W) during the 2,000-user phase, so energy per run cannot grow further and response times reflect saturation. Compare energy per request rather than energy per run for this plan.
- **Sampling interval.** The default 200 ms interval made EnergiBridge crash under load, so 1000 ms is used. Totals come from the cumulative counter and are unaffected; only the time resolution is coarser.
- **Persistent database.** The `dev` H2 database is a persistent file and grows slightly between runs. It was not reset between series, and the same must apply to the improved series for comparability.
- **Single machine, single day.** The results are valid as a relative reference on this laptop. They are not absolute energy figures for a production deployment.

## Discarded attempts (not included)

- A first `stress` series showed CV ≈ 17 % with 8–12 W bursts caused by background activity. This motivated the 30 s quality control described above.
- A `stress` attempt was aborted after a reboot because idle power was ≈ 10 W.
