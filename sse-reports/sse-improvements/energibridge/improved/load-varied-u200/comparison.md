# EnergiBridge analysis

## Series `baseline/load-varied-u200`

### Idle reference
- idle_start: 507.7 J in 311.0 s = 1.63 W (mean CPU usage 4.8 %)
- idle_end: 486.7 J in 310.9 s = 1.57 W (mean CPU usage 4.6 %)
- Reference idle power (mean of available): **1.60 W**
- Idle drift (end vs start): -4.1 %

### Per run

| Run | Energy (J) | Duration (s) | Power (W) | CPU % | Incr. (J) | Requests OK | mJ/req (incr.) | Status |
|---|---|---|---|---|---|---|---|---|
| run01 | 1596.5 | 304.9 | 5.24 | 27.2 | 1108.9 | 115192 | 9.63 | OK |
| run02 | 1590.5 | 304.9 | 5.22 | 27.2 | 1103.0 | 115181 | 9.58 | OK |
| run03 | 1612.8 | 304.9 | 5.29 | 27.4 | 1125.3 | 115185 | 9.77 | OK |
| run04 | 1598.1 | 304.9 | 5.24 | 27.1 | 1110.5 | 115150 | 9.64 | OK |
| run05 | 1636.2 | 304.9 | 5.37 | 27.4 | 1148.7 | 115237 | 9.97 | OK |
| run06 | 1606.2 | 304.9 | 5.27 | 27.2 | 1118.8 | 115159 | 9.71 | OK |
| run07 | 1593.7 | 304.9 | 5.23 | 27.4 | 1106.2 | 115216 | 9.60 | OK |
| run08 | 1575.1 | 304.9 | 5.17 | 27.1 | 1087.6 | 115195 | 9.44 | OK |
| run09 | 1599.8 | 304.9 | 5.25 | 27.3 | 1112.3 | 115192 | 9.66 | OK |
| run10 | 1573.0 | 304.9 | 5.16 | 27.1 | 1085.5 | 115179 | 9.42 | OK |

### Summary over 10 of 10 runs (invalid runs excluded)

| Metric | Median | Mean | SD | Min | Max | CV % |
|---|---|---|---|---|---|---|
| Total CPU energy (J) | 1597.3 | 1598.2 | 18.2 | 1573.0 | 1636.2 | 1.1 |
| Incremental energy = total - idle power x duration (J) | 1109.7 | 1110.7 | 18.2 | 1085.5 | 1148.7 | 1.6 |
| Duration (s) | 304.9 | 304.9 | 0.0 | 304.9 | 304.9 | 0.0 |
| Mean power (W) | 5.24 | 5.24 | 0.06 | 5.16 | 5.37 | 1.1 |
| Successful requests | 115188 | 115189 | 25 | 115150 | 115237 | 0.0 |
| Total energy per request (mJ) | 13.87 | 13.87 | 0.16 | 13.66 | 14.20 | 1.1 |
| Incremental energy per request (mJ) | 9.64 | 9.64 | 0.16 | 9.42 | 9.97 | 1.6 |

## Series `improved/load-varied-u200`

### Idle reference
- idle_start: 553.2 J in 310.9 s = 1.78 W (mean CPU usage 3.9 %)
- idle_end: 518.1 J in 310.9 s = 1.67 W (mean CPU usage 3.4 %)
- Reference idle power (mean of available): **1.72 W**
- Idle drift (end vs start): -6.3 %

### Per run

| Run | Energy (J) | Duration (s) | Power (W) | CPU % | Incr. (J) | Requests OK | mJ/req (incr.) | Status |
|---|---|---|---|---|---|---|---|---|
| run01 | 1022.3 | 305.0 | 3.35 | 21.0 | 496.9 | 114626 | 4.33 | OK |
| run02 | 1058.6 | 306.0 | 3.46 | 21.2 | 531.5 | 114664 | 4.64 | OK |
| run03 | 998.0 | 305.0 | 3.27 | 20.8 | 472.6 | 114646 | 4.12 | OK |
| run04 | 1024.5 | 304.9 | 3.36 | 20.8 | 499.2 | 114679 | 4.35 | OK |
| run05 | 1002.8 | 304.9 | 3.29 | 20.3 | 477.4 | 114636 | 4.16 | OK |
| run06 | 1016.3 | 304.9 | 3.33 | 20.7 | 491.0 | 114650 | 4.28 | OK |
| run07 | 1056.4 | 305.0 | 3.46 | 21.2 | 531.0 | 114654 | 4.63 | OK |
| run08 | 995.1 | 305.0 | 3.26 | 20.8 | 469.7 | 114643 | 4.10 | OK |
| run09 | 1009.1 | 305.0 | 3.31 | 20.7 | 483.7 | 114655 | 4.22 | OK |
| run10 | 1006.5 | 305.0 | 3.30 | 20.9 | 481.1 | 114642 | 4.20 | OK |

### Summary over 10 of 10 runs (invalid runs excluded)

| Metric | Median | Mean | SD | Min | Max | CV % |
|---|---|---|---|---|---|---|
| Total CPU energy (J) | 1012.7 | 1019.0 | 22.5 | 995.1 | 1058.6 | 2.2 |
| Incremental energy = total - idle power x duration (J) | 487.4 | 493.4 | 22.1 | 469.7 | 531.5 | 4.5 |
| Duration (s) | 305.0 | 305.1 | 0.3 | 304.9 | 306.0 | 0.1 |
| Mean power (W) | 3.32 | 3.34 | 0.07 | 3.26 | 3.46 | 2.1 |
| Successful requests | 114648 | 114650 | 15 | 114626 | 114679 | 0.0 |
| Total energy per request (mJ) | 8.83 | 8.89 | 0.20 | 8.68 | 9.23 | 2.2 |
| Incremental energy per request (mJ) | 4.25 | 4.30 | 0.19 | 4.10 | 4.64 | 4.5 |

## Comparison: `baseline/load-varied-u200` (A, reference) vs `improved/load-varied-u200` (B)

Valid runs: A=10, B=10. Differences are B relative to A (negative = B uses less).

Idle power: A=1.60 W, B=1.72 W (+7.8 %)

| Metric | Median A | Median B | Change % | 95 % CI (bootstrap) | p (permutation) | Cohen d | Verdict |
|---|---|---|---|---|---|---|---|
| Total CPU energy (J) | 1597.3 | 1012.7 | -36.6 | [-37.4, -35.0] | 0.000 | -28.34 | B uses less |
| Incremental energy = total - idle power x duration (J) | 1109.7 | 487.4 | -56.1 | [-57.1, -53.8] | 0.000 | -30.48 | B uses less |
| Duration (s) | 304.9 | 305.0 | +0.0 | [+0.0, +0.0] | 0.001 | +0.60 | B uses MORE |
| Mean power (W) | 5.24 | 3.32 | -36.6 | [-37.4, -35.0] | 0.000 | -28.88 | B uses less |
| Total energy per request (mJ) | 13.87 | 8.83 | -36.3 | [-37.1, -34.7] | 0.000 | -28.16 | B uses less |
| Incremental energy per request (mJ) | 9.64 | 4.25 | -55.9 | [-56.9, -53.6] | 0.000 | -30.36 | B uses less |

Blue Angel criterion (total energy must not increase by more than 10 % vs the reference): change = -36.6 % -> within the limit.

Notes: the comparison is only valid if both series use the same plan, workload, machine and conditions, and the same number of runs. Only CPU energy is measured.
