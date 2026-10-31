package at.ac.tuwien.sepm.groupphase.backend.unittests;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.ImageDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.ImageMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ImageMappingTest {

    private ImageMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = Mappers.getMapper(ImageMapper.class);
        ReflectionTestUtils.setField(mapper, "imageServedPath", "/images");
    }

    @ParameterizedTest(name = "prefix={0}, filename={1}, url={2}")
    @MethodSource("provideImageMappings")
    public void givenImageAndPrefix_whenMapToDto_thenPreserveFilenameAndBuildUrl(
        String prefix, String filename, String expectedUrl) {
        ReflectionTestUtils.setField(mapper, "imageServedPath", prefix);
        Image image = new Image();
        image.setFilename(filename);

        ImageDto dto = mapper.imageToImageDto(image);

        assertAll(
            () -> assertEquals(filename, dto.getFilename()),
            () -> assertEquals(expectedUrl, dto.getUrl()),
            () -> assertEquals(filename, image.getFilename())
        );
    }

    private static Stream<Arguments> provideImageMappings() {
        return Stream.of(
            Arguments.of("/images", "example.png", "/images/example.png"),
            Arguments.of("/images", "example.jpeg", "/images/example.jpeg"),
            Arguments.of("/images/", "example.png", "/images/example.png"),
            Arguments.of("/images///", "example.png", "/images/example.png"),
            Arguments.of("/", "example.png", "/example.png"),
            Arguments.of("", "example.png", "example.png"),
            Arguments.of("images", "example.png", "images/example.png"),
            Arguments.of("/images", "nested/example.png", "/images/nested/example.png"),
            Arguments.of("/images", "/example.png", "/images/example.png"),
            Arguments.of("/images", "", "/images")
        );
    }

    @Test
    public void givenNullImage_whenMapToDto_thenReturnNull() {
        assertNull(mapper.imageToImageDto(null));
    }

    @Test
    public void givenImageWithoutFilename_whenMapToDto_thenRejectNullFilename() {
        Image image = new Image();

        assertThrows(NullPointerException.class, () -> mapper.imageToImageDto(image));
    }
}
