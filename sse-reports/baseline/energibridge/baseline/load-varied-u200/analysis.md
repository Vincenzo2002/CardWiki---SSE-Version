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
