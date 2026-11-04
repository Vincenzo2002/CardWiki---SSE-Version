# GreenIT Report — Super Large Test Deck

## Analysed page

- **Name:** Super Large Test Deck
- **URL:** `http://localhost:4200/decks/4`
- **Environment:** Angular production build, frontend served locally with `npx serve`; Spring Boot backend running.
- **Measurement type:** post-change only. No initial baseline is available.

## Final results

| Indicator                      |     Result |
| ------------------------------ | ---------: |
| EcoIndex                       |      63.05 |
| EcoIndex grade                 |          C |
| Estimated water consumption    |    2.61 cl |
| Estimated GHG emissions        | 1.74 gCO₂e |
| DOM size                       |      1,048 |
| HTTP requests shown by GreenIT |          0 |
| Page size shown by GreenIT     |       0 KB |

> Note: GreenIT shows 0 requests and 0 KB in the final screenshot. These values
> are reported as displayed by the tool and are not interpreted as evidence of
> an absence of network transfers.

## Best practices

All checks shown in the post-change screenshot are green. The details for
“Externalize css and js” report 2 inline stylesheet(s) and inline JavaScript(s),
although the check remains green in the report.

## Observations

The EcoIndex is in grade C. The page has 1,048 DOM elements, considerably more
than the other analysed pages (64 to 329 DOM elements). This high DOM count is a
possible factor for further investigation, but no causal analysis was performed
and no page-specific changes were made, as requested.

## Baseline and comparison

- **Baseline EcoIndex:** 58.44.
- **Final EcoIndex:** 63.05.
- **Change:** +4.61 points.

The initial baseline had 16 requests and a DOM size of 1,058. The final
measurement shows a DOM size of 1,048. Final request counts and page size are
reported as displayed by GreenIT (0 and 0 KB), so they are not directly
comparable with the baseline.
