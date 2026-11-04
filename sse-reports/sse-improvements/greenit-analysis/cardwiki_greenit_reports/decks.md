# GreenIT Report — Decks

## Analysed page

- **URL:** `http://localhost:4200/search`
- **Environment:** Angular production build, frontend served locally with `npx serve`; Spring Boot backend running.
- **Final result:** all GreenIT best-practice checks are green.

## Final results

| Indicator                      |     Result |
| ------------------------------ | ---------: |
| EcoIndex                       |      95.18 |
| EcoIndex grade                 |          A |
| Estimated water consumption    |    1.64 cl |
| Estimated GHG emissions        | 1.10 gCO₂e |
| DOM size                       |         73 |
| HTTP requests shown by GreenIT |          0 |
| Page size shown by GreenIT     |       0 KB |

> Note: GreenIT shows 0 requests and 0 KB in the final screenshot. These values
> are reported as displayed by the tool and are not interpreted as evidence of
> an absence of network transfers.

## Common changes applied

- Moved the styles of the affected components into the global `styles.scss` file, removing the component-specific CSS files and their `styleUrls` references.
- Added a dedicated print stylesheet (`assets/print.css`), linked from `index.html` with `media="print"` and minified.
- Configured local `Cache-Control` headers through `serve.json`: `index.html` uses `no-cache`, hashed JS/CSS assets use `public, max-age=31536000, immutable`, and assets under `assets/` use `public, max-age=86400`.
- Checked the headers with `curl` and Chrome DevTools.

## Best practices

All checks shown in the final screenshot are green, including Cache-Control,
compression, external and minified CSS/JS, absence of HTTP errors, a print
stylesheet and HTTP/2 usage, as reported by GreenIT.

## Baseline and comparison

- **Baseline EcoIndex:** 90.32.
- **Final EcoIndex:** 95.18.
- **Change:** +4.86 points.

The initial baseline had 16 requests and a DOM size of 86. The final measurement
shows a DOM size of 73. Final request counts and page size are reported as
displayed by GreenIT (0 and 0 KB), so they are not directly comparable with the
baseline.
