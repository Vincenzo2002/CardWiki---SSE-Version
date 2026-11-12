# EnergiBridge analysis

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
