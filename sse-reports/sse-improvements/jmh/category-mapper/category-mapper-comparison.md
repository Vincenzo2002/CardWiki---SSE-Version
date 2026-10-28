# Category mapper optimization with JMH

Date: **2026-10-28**.

We used JMH to compare category mapping before and after a small optimization.
The detailed mapper became faster in both repeated measurements. The simple
mapper showed no clear performance change.

## 1. What we changed

The method
[`childrenToSimpleChildren`](../../../../backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/endpoint/mapper/CategoryMapper.java)
converts a set of child categories into a list of DTOs. Originally, it first
copied the categories into a temporary list:

```java
return INSTANCE.categoryToCategorySimpleDto(new ArrayList<>(children));
```

We replaced that step with a loop that creates the DTO list directly:

```java
List<CategorySimpleDto> result = new ArrayList<>(children.size());

for (Category child : children) {
    result.add(INSTANCE.categoryToCategorySimpleDto(child));
}

return result;
```

This avoids the temporary category list. The DTOs are still created using
the existing single-category mapper.

## 2. How we tested it

We used two benchmarks:

- **Simple mapper:** converts a list of 10, 100, or 1000 categories into DTOs
  containing ID and name.
- **Detailed mapper:** converts one category with 0, 10, 100, or 1000 children
  into a detailed DTO. The parent, author, and dates stay fixed; decks are empty.

Inputs are prepared in `@Setup`, outside the timed operation. The benchmarks
return the result to JMH and run in memory, without Spring or a database.

| Setting | Value |
| --- | --- |
| JMH / JDK | 1.37 / 11.0.32.1 |
| Machine | Apple M3, 16 GiB RAM, macOS 27.0.1, arm64 |
| Mode | Average time, in nanoseconds per operation (`ns/op`) |
| Threads | 1 |
| JVM forks | 2 separate JVM processes per input case |
| Warmup per fork | 3 iterations of 1 second, excluded from the result |
| Measurement per fork | 5 iterations of 1 second |

The recorded configuration is the same before and after the change. We ran
the detailed benchmark twice per version and the simple benchmark once per
version, keeping the benchmark definitions unchanged.

After the change, **all 368 JUnit tests passed**, with no failures, errors, or
skipped tests. This includes the existing category mapping tests.

## 3. Detailed mapper results

The table averages the two run scores for each version. Individual results
and JMH error estimates are available in the linked JSON files.

Each operation converts **one whole category, including all its children**.
Lower times are better.

| Children | Before (ns/op) | After (ns/op) | Time reduction |
| --- | ---: | ---: | ---: |
| 0 | 13.36 | 11.13 | 16.64% |
| 10 | 88.39 | 40.14 | 54.58% |
| 100 | 791.20 | 310.89 | 60.71% |
| 1000 | 8429.75 | 7218.60 | 14.37% |

Time reduction is calculated from the unrounded values as:

```text
100 × (before − after) / before
```

Both optimized runs were faster for every input size. Their JMH confidence
intervals were below those of both baseline runs, supporting a repeatable
improvement in this experiment.

The largest percentage reduction was about **61% with 100 children**. With
1000 children, the saving was about **1.21 microseconds per conversion**.

Original results:

- Before: [run 01](../../../baseline/jmh/category-mapper/category-detailed-mapper-run01.json),
  [run 02](../../../baseline/jmh/category-mapper/category-detailed-mapper-run02.json).
- After: [run 01](category-detailed-mapper-run01.json),
  [run 02](category-detailed-mapper-run02.json).

## 4. Simple mapper results

Each operation converts **the whole input list**. There is one run per version.

| Categories | Before (ns/op) | After (ns/op) |
| --- | ---: | ---: |
| 10 | 28.75 | 28.78 |
| 100 | 267.32 | 263.66 |
| 1000 | 3071.56 | 3062.41 |

The differences are small, and the JMH confidence intervals overlap. These
results do not show a clear improvement or regression.

The simple mapper does not call the modified method. The two benchmarks
measure different operations, so their absolute times are not directly comparable.

Original results: [before](../../../baseline/jmh/category-mapper/category-mapper-run01.json),
[after](category-mapper-run01.json).

## 5. How to repeat the experiment

Select the intended code version and use JDK 11. From the repository root,
build the project and run the detailed benchmark:

```sh
mvn clean package

mkdir -p sse-reports/sse-improvements/jmh/category-mapper
set -o pipefail

java -jar benchmarks/target/benchmarks.jar \
  'CategoryDetailedMapperBenchmark.mapCategoryDetails' \
  -t 1 -foe true -rf json \
  -rff sse-reports/sse-improvements/jmh/category-mapper/category-detailed-mapper-run03.json \
  2>&1 | tee sse-reports/sse-improvements/jmh/category-mapper/category-detailed-mapper-run03.log
```

The benchmark annotations supply the settings listed above. Use a new filename
for each run. For the simple benchmark, select
`CategoryMapperBenchmark.mapCategories` and change the output filenames.

For the baseline, restore the original implementation, rebuild, and save the
results under `sse-reports/baseline/jmh/category-mapper/`. The output directory does not select
the code version. See the [benchmark guide](../../../../benchmarks/README.md) for
JDK setup and further commands.

## 6. Conclusion and limitations

Removing the temporary list reduced detailed category mapping time for our
test inputs. The improvement appeared in both repeated runs, while the simple
mapping path stayed compatible with unchanged performance.

These results concern one in-memory operation on one machine. They do not
show how much faster the full application becomes, and they do not measure
memory allocation or energy consumption. Background workload and power
conditions were not recorded, and we did not alternate the two versions
between measurements.

The base checkout was `7f3462b10b868990cb518d2448826aff041101b9`; the optimization
was a local change when measured. The JSON files do not record the exact source
commit or JAR identity, which limits independent reproduction of these runs.
