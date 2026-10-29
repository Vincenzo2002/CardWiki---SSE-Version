package at.ac.tuwien.sepm.groupphase.benchmarks;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.ImageDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.ImageMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Image;
import org.mapstruct.factory.Mappers;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class ImageMapperBenchmark {

    @Param({"10", "100", "1000"})
    public int size;

    private ImageMapper mapper;
    private List<Image> images;

    @Setup
    public void setup() throws ReflectiveOperationException {
        mapper = Mappers.getMapper(ImageMapper.class);

        // Configure the URL prefix without starting Spring.
        Field servedPath = ImageMapper.class.getDeclaredField("imageServedPath");
        servedPath.setAccessible(true);
        servedPath.set(mapper, "/images");

        images = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            Image image = new Image();
            image.setFilename(String.format("%064x.png", i));
            images.add(image);
        }
    }

    @Benchmark
    public List<ImageDto> mapImages() {
        List<ImageDto> result = new ArrayList<>(size);

        for (Image image : images) {
            result.add(mapper.imageToImageDto(image));
        }

        return result;
    }
}
