# EnergiBridge analysis

## Series `baseline/stress-varied-u2000`

### Idle reference
- idle_start: 639.0 J in 310.9 s = 2.06 W (mean CPU usage 6.2 %)
- idle_end: 509.6 J in 310.9 s = 1.64 W (mean CPU usage 5.2 %)
- Reference idle power (mean of available): **1.85 W**
- Idle drift (end vs start): -20.2 % **WARNING: drift above threshold**

### Per run

| Run | Energy (J) | Duration (s) | Power (W) | CPU % | Incr. (J) | Requests OK | mJ/req (incr.) | Status |
|---|---|---|---|---|---|---|---|---|
| run01 | 3675.1 | 314.1 | 11.70 | 62.7 | 3094.9 | 267800 | 11.56 | OK |
| run02 | 3658.4 | 314.1 | 11.65 | 62.4 | 3078.3 | 271780 | 11.33 | OK |
| run03 | 3821.1 | 315.1 | 12.13 | 65.2 | 3239.0 | 268087 | 12.08 | OK |
| run04 | 3661.9 | 314.1 | 11.66 | 62.6 | 3081.8 | 269790 | 11.42 | OK |
| run05 | 3710.6 | 314.1 | 11.81 | 63.1 | 3130.5 | 269759 | 11.60 | OK |
| run06 | 3692.2 | 314.1 | 11.75 | 62.7 | 3112.0 | 269557 | 11.54 | OK |
| run07 | 3651.5 | 314.1 | 11.63 | 62.5 | 3071.4 | 271465 | 11.31 | OK |
| run08 | 3662.5 | 314.1 | 11.66 | 62.7 | 3082.3 | 266220 | 11.58 | OK |
| run09 | 3661.8 | 314.1 | 11.66 | 62.6 | 3081.6 | 264602 | 11.65 | OK |
| run10 | 3643.2 | 314.1 | 11.60 | 62.3 | 3063.1 | 264715 | 11.57 | OK |

### Summary over 10 of 10 runs (invalid runs excluded)

| Metric | Median | Mean | SD | Min | Max | CV % |
|---|---|---|---|---|---|---|
| Total CPU energy (J) | 3662.2 | 3683.8 | 52.1 | 3643.2 | 3821.1 | 1.4 |
| Incremental energy = total - idle power x duration (J) | 3082.1 | 3103.5 | 51.6 | 3063.1 | 3239.0 | 1.7 |
| Duration (s) | 314.1 | 314.2 | 0.3 | 314.1 | 315.1 | 0.1 |
| Mean power (W) | 11.66 | 11.72 | 0.15 | 11.60 | 12.13 | 1.3 |
| Successful requests | 268822 | 268378 | 2564 | 264602 | 271780 | 1.0 |
| Total energy per request (mJ) | 13.74 | 13.73 | 0.23 | 13.45 | 14.25 | 1.7 |
| Incremental energy per request (mJ) | 11.56 | 11.56 | 0.21 | 11.31 | 12.08 | 1.9 |
