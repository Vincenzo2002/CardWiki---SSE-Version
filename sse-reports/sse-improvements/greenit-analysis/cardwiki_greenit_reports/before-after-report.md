# CardWiki — GreenIT Before/After Report

## Purpose

This document summarises the optimisation work and GreenIT results collected
for six CardWiki pages in the local environment.

## Measurement environment

- Angular frontend compiled in production mode.
- Frontend served locally using `npx serve` on port 4200.
- Spring Boot backend running with the `dev` profile.
- Analyses performed with GreenIT and best-practice analysis enabled.

Measurements were taken in the local environment. The configuration described
here should not be interpreted as the GitHub Pages deployment configuration.

## Changes implemented

### 1. Externalising component styles

The CSS styles of the `toasts-container` and `markdown-syntax` components were
moved into `frontend/src/styles.scss`. Their `styleUrls` references were removed
from the TypeScript files, together with the component-specific CSS files that
were no longer used. After the change, the number of `style` elements observed
in the DOM decreased from 3 to 1 (the remaining element was empty).

### 2. Print stylesheet

The file `frontend/src/assets/print.css` was created and linked in
`frontend/src/index.html` with `media="print"`. The `@media print` rule previously
added to the global stylesheet was replaced with a dedicated stylesheet
recognised by GreenIT. The file was subsequently minified.

### 3. Cache-Control in the local environment

The file `frontend/serve.json` was added and copied into the build directory
served by `serve`. The rules set:

- `index.html`: `Cache-Control: no-cache`;
- JavaScript and CSS files: `Cache-Control: public, max-age=31536000, immutable`;
- resources under `assets/`: `Cache-Control: public, max-age=86400`.

The headers were checked with `curl` for all resources flagged by GreenIT and
in the Network panel of Chrome DevTools. After running GreenIT again, the
Cache-Control check was green.

### 4. Build and verification

The production build was generated with
`NODE_OPTIONS=--openssl-legacy-provider npm run build:prod`. The frontend was
served locally with `npx serve -s dist/cardwiki -l 4200`. GreenIT checks for the
five main pages and the deck details page were reviewed after the changes.

## EcoIndex comparison

| Page                  | Baseline EcoIndex | Final EcoIndex |  Change | Final DOM size |
| --------------------- | ----------------: | -------------: | ------: | -------------: |
| About                 |             90.05 |          95.00 |   +4.95 |             75 |
| Categories            |           93.13\* |          95.98 | +2.85\* |             64 |
| Decks (search)        |             90.32 |          95.18 |   +4.86 |             73 |
| Formatting Cards      |         85.83\*\* |          86.21 |   +0.38 |            329 |
| Users                 |             90.64 |          95.36 |   +4.72 |             71 |
| Super Large Test Deck |             58.44 |          63.05 |   +4.61 |          1,048 |

- _Categories: the baseline showed 0 requests, so the measurement is considered unreliable. The change is indicative only and should not be used as quantitative evidence._
- **Formatting Cards:** the initial baseline of 82.79 had 3 HTTP errors. For the comparison, it was replaced with the subsequent baseline of 85.83, which had 0 HTTP errors.
- Super Large Test Deck: only the post-change result is available.

## Other indicators and data limitations

In the final screenshots, GreenIT shows 0 HTTP requests and a page size of 0 KB
for all six pages. These values are reproduced faithfully, but they are not
comparable with the earlier baselines, which showed non-zero request counts
and page sizes. They therefore do not imply an actual reduction to zero in
traffic or transferred data.

The final DOM element counts are available in the screenshots and range from
64 to 1,048. The highest value belongs to the Super Large Test Deck page.

## Best-practice results

For all six pages, the final screenshots show green results for the displayed
GreenIT checks. The checks reviewed include:

- Expires or Cache-Control headers;
- resource compression;
- limits on the number of domains and requests;
- absence of HTTP errors;
- externalisation and minification of CSS/JS;
- absence of cookies for static resources;
- absence of redirects;
- bitmap and SVG image optimisation, where applicable;
- presence of a print stylesheet;
- a limit on the number of stylesheets;
- HTTP/2;
- use of standard fonts.

For Super Large Test Deck, the EcoIndex remains in grade C despite the green
checks. The page has 1,048 DOM elements and was not modified further.

## Conclusion

The changes resulted in green checks for all GreenIT best practices shown on
the six analysed pages. The five pages with an available baseline have a final
EcoIndex above the recorded baseline value, subject to the limitations of the
initial Categories measurement and the baseline selected for Formatting Cards.
