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

Save original outputs and short English notes in the tool's directory,
optionally grouped by benchmark. For example, category mapper measurements
are stored under `baseline/jmh/category-mapper/` and
`sse-improvements/jmh/category-mapper/`. Use distinct filenames for different
runs and include a Markdown note or comparison report. The `.gitkeep` files
preserve otherwise empty directories in Git.

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

For the Git workflow, see the
[main README](../README.md#branches-and-improvements).
