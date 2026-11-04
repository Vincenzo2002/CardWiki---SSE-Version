# GreenIT Report — Formatting Cards

## Analysed page

- **URL:** `http://localhost:4200/help/markdown-syntax#markdown`
- **Environment:** Angular production build, frontend served locally with `npx serve`; Spring Boot backend running.
- **Final result:** all GreenIT best-practice checks are green.

## Final results

| Indicator                      |     Result |
| ------------------------------ | ---------: |
| EcoIndex                       |      86.21 |
| EcoIndex grade                 |          A |
| Estimated water consumption    |    1.91 cl |
| Estimated GHG emissions        | 1.28 gCO₂e |
| DOM size                       |        329 |
| HTTP requests shown by GreenIT |          0 |
| Page size shown by GreenIT     |       0 KB |

> Note: GreenIT shows 0 requests and 0 KB in the final screenshot. These values
> are reported as displayed by the tool and are not interpreted as evidence of
> an absence of network transfers.

## Page-specific and common changes

- Moved the styles of `toasts-container` and `markdown-syntax` into the global `styles.scss` file.
- Removed their `styleUrls` references from the components and the component-specific CSS files that were no longer used.
- Added a dedicated print stylesheet (`assets/print.css`), linked from `index.html` with `media="print"` and minified.
- Configured local `Cache-Control` headers through `serve.json`: `index.html` uses `no-cache`, hashed JS/CSS assets use `public, max-age=31536000, immutable`, and assets under `assets/` use `public, max-age=86400`.
- Checked the headers with `curl` and Chrome DevTools.

## Best practices

All checks shown in the final screenshot are green, including Cache-Control,
compression, external and minified CSS/JS, absence of HTTP errors, a print
stylesheet and HTTP/2 usage, as reported by GreenIT.

## Baseline and comparison

- **More reliable initial baseline:** EcoIndex 85.83 (the first measurement, 82.79, had 3 HTTP errors).
- **Final EcoIndex:** 86.21.
- **Change:** +0.38 points.

The more reliable baseline recorded 7 requests and a DOM size of 333; a later
baseline measurement reported 10 requests, 330 DOM elements and 440 KB. Final
measurements show a DOM size of 329. GreenIT displays final request counts and
page size as 0, so they are not directly comparable with the baseline values.
