# EnergiBridge analysis

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
