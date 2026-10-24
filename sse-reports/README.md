# SSE reports

The `baseline/` and `sse-improvements/` directories store results before and
after optimization. Each contains a directory for every tool:

```text
baseline/                  sse-improvements/
  jmh/                       jmh/
  jmeter/                    jmeter/
  energibridge/              energibridge/
  creedengo/                 creedengo/
  websitecarbon/             websitecarbon/
  ecoindex/                  ecoindex/
  greenit-analysis/          greenit-analysis/
  fossa/                     fossa/
  guido/                     guido/
```

These names identify result sets, not Git branches. The permanent branches are
`master` and `baseline`; measurements of improved versions on temporary branches
or `master` are stored under `sse-improvements/`.

Save original outputs and short English notes directly in the tool's directory.
Use distinct names for different runs, such as
`baseline/jmh/2026-10-24-category-mapper.json` and a `.md` note with the same name.
No measurements have been archived yet: `.gitkeep` files only preserve empty
directories in Git and can be removed when the first results are added.

For each run, record the date, executed commit, tool version, command or
procedure, environment, input data, and parameters. Use the same tests and
equivalent conditions for both versions. For a website analysis, also record
the URL, page, and deployed application version. Do not overwrite earlier
measurements or treat a shorter execution time as evidence of lower energy
consumption without an energy measurement or an explicitly identified estimate.

Baseline reports can be committed to `baseline` with the tests, then merged into
a temporary branch from `master` and submitted through a pull request. Each
optimization is developed on its own branch from `master`; its results accompany
the pull request into `master`. Do not merge application optimizations into
`baseline`.

The final comparison will be summarized in the
[main README](../README.md#tools-and-results).
