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

## Series `improved/stress-varied-u2000`

### Idle reference
- idle_start: 483.0 J in 310.9 s = 1.55 W (mean CPU usage 2.9 %)
- idle_end: 546.0 J in 310.9 s = 1.76 W (mean CPU usage 3.6 %)
- Reference idle power (mean of available): **1.65 W**
- Idle drift (end vs start): +13.0 %

### Per run

| Run | Energy (J) | Duration (s) | Power (W) | CPU % | Incr. (J) | Requests OK | mJ/req (incr.) | Status |
|---|---|---|---|---|---|---|---|---|
| run01 | 3584.3 | 324.5 | 11.04 | 61.2 | 3047.3 | 472178 | 6.45 | OK |
| run02 | 3583.5 | 325.1 | 11.02 | 61.5 | 3045.6 | 467419 | 6.52 | OK |
| run03 | 3573.3 | 324.5 | 11.01 | 61.4 | 3036.3 | 474394 | 6.40 | OK |
| run04 | 3578.0 | 323.5 | 11.06 | 61.2 | 3042.7 | 468846 | 6.49 | OK |
| run05 | 3580.1 | 323.3 | 11.07 | 61.5 | 3045.0 | 468664 | 6.50 | OK |
| run06 | 3542.1 | 322.2 | 10.99 | 61.1 | 3009.0 | 466770 | 6.45 | OK |
| run07 | 3554.3 | 322.3 | 11.03 | 61.2 | 3020.9 | 464876 | 6.50 | OK |
| run08 | 3571.4 | 323.7 | 11.03 | 61.3 | 3035.8 | 469407 | 6.47 | OK |
| run09 | 3551.8 | 323.3 | 10.99 | 61.2 | 3016.9 | 432684 | 6.97 | OK |
| run10 | 3575.4 | 323.7 | 11.05 | 61.4 | 3039.8 | 466880 | 6.51 | OK |

### Summary over 10 of 10 runs (invalid runs excluded)

| Metric | Median | Mean | SD | Min | Max | CV % |
|---|---|---|---|---|---|---|
| Total CPU energy (J) | 3574.4 | 3569.4 | 14.7 | 3542.1 | 3584.3 | 0.4 |
| Incremental energy = total - idle power x duration (J) | 3038.1 | 3033.9 | 13.5 | 3009.0 | 3047.3 | 0.4 |
| Duration (s) | 323.6 | 323.6 | 0.9 | 322.2 | 325.1 | 0.3 |
| Mean power (W) | 11.03 | 11.03 | 0.03 | 10.99 | 11.07 | 0.2 |
| Successful requests | 468042 | 465212 | 11756 | 432684 | 474394 | 2.5 |
| Total energy per request (mJ) | 7.64 | 7.68 | 0.19 | 7.53 | 8.21 | 2.5 |
| Incremental energy per request (mJ) | 6.49 | 6.53 | 0.16 | 6.40 | 6.97 | 2.5 |

## Comparison: `baseline/stress-varied-u2000` (A, reference) vs `improved/stress-varied-u2000` (B)

Valid runs: A=10, B=10. Differences are B relative to A (negative = B uses less).

Idle power: A=1.85 W, B=1.65 W (-10.4 %)

| Metric | Median A | Median B | Change % | 95 % CI (bootstrap) | p (permutation) | Cohen d | Verdict |
|---|---|---|---|---|---|---|---|
| Total CPU energy (J) | 3662.2 | 3574.4 | -2.4 | [-3.3, -2.2] | 0.000 | -2.99 | B uses less |
| Incremental energy = total - idle power x duration (J) | 3082.1 | 3038.1 | -1.4 | [-2.5, -1.2] | 0.000 | -1.85 | B uses less |
| Duration (s) | 314.1 | 323.6 | +3.0 | [+2.8, +3.3] | 0.000 | +13.56 | B uses MORE |
| Mean power (W) | 11.66 | 11.03 | -5.4 | [-6.2, -5.2] | 0.001 | -6.26 | B uses less |
| Total energy per request (mJ) | 13.74 | 7.64 | -44.4 | [-44.8, -43.7] | 0.001 | -28.84 | B uses less |
| Incremental energy per request (mJ) | 11.56 | 6.49 | -43.8 | [-44.3, -43.1] | 0.001 | -26.53 | B uses less |

Blue Angel criterion (total energy must not increase by more than 10 % vs the reference): change = -2.4 % -> within the limit.

Notes: the comparison is only valid if both series use the same plan, workload, machine and conditions, and the same number of runs. Only CPU energy is measured.
