package at.ac.tuwien.sepm.groupphase.benchmarks;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CardSimpleDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.RevisionMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Card;
import at.ac.tuwien.sepm.groupphase.backend.entity.Image;
import at.ac.tuwien.sepm.groupphase.backend.entity.RevisionEdit;
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
public class RevisionMapperBenchmark {

    @Param({"10", "100", "1000"})
    public int size;

    @Param({"0", "1", "2"})
    public int imagesPerRevision;

    private RevisionMapper mapper;
    private List<RevisionEdit> revisions;

    @Setup
    public void setup() throws ReflectiveOperationException {
        mapper = Mappers.getMapper(RevisionMapper.class);
        Field servedPath = RevisionMapper.class.getDeclaredField("imageServedPath");
        servedPath.setAccessible(true);
        servedPath.set(mapper, "/images");

        revisions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Card card = new Card();
            card.setId((long) i + 1);

            RevisionEdit revision = new RevisionEdit();
            revision.setId((long) i + 10000);
            revision.setCard(card);
            revision.setTextFront("Front text");
            revision.setTextBack("Back text");

            if (imagesPerRevision >= 1) {
                revision.setImageFront(createImage(i * 2));
            }
            if (imagesPerRevision == 2) {
                revision.setImageBack(createImage(i * 2 + 1));
            }
            revisions.add(revision);
        }
    }

    private static Image createImage(int number) {
        Image image = new Image();
        image.setFilename(String.format("%064x.png", number));
        return image;
    }

    @Benchmark
    public List<CardSimpleDto> mapRevisions() {
        List<CardSimpleDto> result = new ArrayList<>(size);
        for (RevisionEdit revision : revisions) {
            result.add(mapper.revisionEditToCardSimpleDto(revision));
        }
        return result;
    }
}
