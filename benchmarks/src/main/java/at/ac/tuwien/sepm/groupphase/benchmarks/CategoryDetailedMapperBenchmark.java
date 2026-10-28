package at.ac.tuwien.sepm.groupphase.benchmarks;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CategoryDetailedDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.CategoryMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Category;
import at.ac.tuwien.sepm.groupphase.backend.entity.User;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class CategoryDetailedMapperBenchmark {

    @Param({"0", "10", "100", "1000"})
    public int childrenCount;

    private CategoryMapper mapper;
    private Category category;

    @Setup
    public void setup() {
        mapper = CategoryMapper.INSTANCE;
        LocalDateTime timestamp = LocalDateTime.of(2020, 1, 1, 0, 0);

        category = new Category(2L, "Main category");
        category.setCreatedBy(new User(1L));
        category.setParent(new Category(1L, "Parent category"));
        category.setCreatedAt(timestamp);
        category.setUpdatedAt(timestamp);

        // Keep decks empty and all other mapped fields fixed across input sizes.
        for (int i = 0; i < childrenCount; i++) {
            Category child = new Category((long) i + 3, "Child category " + i);
            child.setParent(category);
        }
    }

    @Benchmark
    public CategoryDetailedDto mapCategoryDetails() {
        return mapper.categoryToCategoryDetailedDto(category);
    }
}
