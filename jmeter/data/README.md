# Varied JMeter inputs

`varied.csv` contains 15 distinct search/deck pairs, three per deck. It uses
the five decks created in order by
[`profiles/dev/DataGenerator.java`](../../backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/profiles/dev/DataGenerator.java).

| Expected ID in an initially empty dev database | Deck name | Fixture cards |
| --- | --- | --- |
| 1 | Hauptstädte Europas | Imported from `capitals.csv` |
| 2 | Mathematik Demo | Imported from `math.csv` |
| 3 | large test deck | 1,000 |
| 4 | super large test deck | 5,000 |
| 5 | 第3課 | 6 |

These are fixture expectations, not IDs verified against a live backend.
The dev generator skips initialization when users already exist. Existing
databases may have different IDs, missing decks or renamed decks: adjust the
CSV to that dataset and keep the same database for before/after comparisons.
Do not delete the database to force these IDs.

The queries exercise different substrings, result sets and Unicode input;
`large test` and `test deck` deliberately match both large decks. Each
selected ID must occur in its search's first page (`limit=10&offset=0`).
The runner verifies every distinct pair before starting JMeter, and the
measured requests check status, JSON and the pair again. No live validation
or performance measurements were run when this fixture was prepared.

The CSV is read sequentially and recycled per thread group. Its equal row
counts give each deck equal representation over a full pass; a timed run
may stop partway through a pass. Fifteen rows exercise five distinct decks,
not fifteen different decks, and do not enlarge the database. Deck card
counts describe the fixture; they do not guarantee proportional endpoint
response size or cost.

Use UTF-8 and exactly the header `search,deck_id`. Keep searches nonempty and
on one line, use positive integer IDs without leading zeros, and quote fields
containing commas or quotes using normal CSV escaping. Duplicate pairs are
allowed if you deliberately want a weighted mix. Commit the CSV with the
test tooling; each execution also records a normalized `plans/input.csv`
snapshot and its SHA-256 in `metadata.json`. Extra JTL columns record the
search and ID actually used by each sample.

See the [execution instructions](../README.md#run-the-tests)
for the original fixed-input mode, higher-user examples and result naming.
