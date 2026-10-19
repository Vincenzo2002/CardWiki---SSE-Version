package at.ac.tuwien.sepm.groupphase.benchmarks;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CategorySimpleDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.CategoryMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Category;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class CategoryMapperBenchmark {

    @Param({"10", "100", "1000"})
    public int size;

    private CategoryMapper mapper;
    private List<Category> categories;

    @Setup
    public void setup() {
        mapper = CategoryMapper.INSTANCE;
        categories = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            categories.add(new Category((long) i + 1, "Category " + i));
        }
    }

    @Benchmark
    public List<CategorySimpleDto> mapCategories() {
        return mapper.categoryToCategorySimpleDto(categories);
    }
}
