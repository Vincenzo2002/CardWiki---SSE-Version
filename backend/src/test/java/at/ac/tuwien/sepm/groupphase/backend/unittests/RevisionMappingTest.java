package at.ac.tuwien.sepm.groupphase.backend.unittests;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CardSimpleDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.RevisionMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Card;
import at.ac.tuwien.sepm.groupphase.backend.entity.Image;
import at.ac.tuwien.sepm.groupphase.backend.entity.RevisionEdit;
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

public class RevisionMappingTest {

    private RevisionMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = Mappers.getMapper(RevisionMapper.class);
        ReflectionTestUtils.setField(mapper, "imageServedPath", "/images");
    }

    @ParameterizedTest(name = "prefix={0}, front={1}, back={2}")
    @MethodSource("provideRevisionMappings")
    public void givenRevisionAndPrefix_whenMapToCardDto_thenPreserveCardIdTextsAndImageUrls(
        String prefix, String frontFilename, String backFilename,
        String expectedFrontUrl, String expectedBackUrl) {
        ReflectionTestUtils.setField(mapper, "imageServedPath", prefix);
        RevisionEdit revision = givenRevision();
        revision.setImageFront(frontFilename == null ? null : imageWithFilename(frontFilename));
        revision.setImageBack(backFilename == null ? null : imageWithFilename(backFilename));

        CardSimpleDto dto = mapper.revisionEditToCardSimpleDto(revision);

        assertAll(
            () -> assertEquals(Long.valueOf(7L), dto.getId()),
            () -> assertEquals("Front text", dto.getTextFront()),
            () -> assertEquals("Back text", dto.getTextBack()),
            () -> assertEquals(expectedFrontUrl, dto.getImageFrontUrl()),
            () -> assertEquals(expectedBackUrl, dto.getImageBackUrl())
        );
    }

    private static Stream<Arguments> provideRevisionMappings() {
        return Stream.of(
            Arguments.of("/images", null, null, null, null),
            Arguments.of("/images", "front.png", null, "/images/front.png", null),
            Arguments.of("/images", null, "back.jpeg", null, "/images/back.jpeg"),
            Arguments.of("/images", "front.png", "back.jpeg", "/images/front.png", "/images/back.jpeg"),
            Arguments.of("/images/", "front.png", "back.jpeg", "/images/front.png", "/images/back.jpeg"),
            Arguments.of("/images///", "front.png", "back.jpeg", "/images/front.png", "/images/back.jpeg"),
            Arguments.of("/", "front.png", "back.jpeg", "/front.png", "/back.jpeg"),
            Arguments.of("", "front.png", "back.jpeg", "front.png", "back.jpeg"),
            Arguments.of("images", "front.png", "back.jpeg", "images/front.png", "images/back.jpeg"),
            Arguments.of("/images", "nested/front.png", "nested/back.jpeg",
                "/images/nested/front.png", "/images/nested/back.jpeg"),
            Arguments.of("/images", "/front.png", "/back.jpeg", "/images/front.png", "/images/back.jpeg"),
            Arguments.of("/images", "", "", "/images", "/images")
        );
    }

    @Test
    public void givenNullRevision_whenMapToCardDto_thenReturnNull() {
        assertNull(mapper.revisionEditToCardSimpleDto(null));
    }

    @Test
    public void givenImagesWithoutFilenames_whenMapToCardDto_thenReturnNullImageUrls() {
        RevisionEdit revision = givenRevision();
        revision.setImageFront(new Image());
        revision.setImageBack(new Image());

        CardSimpleDto dto = mapper.revisionEditToCardSimpleDto(revision);

        assertAll(
            () -> assertEquals(Long.valueOf(7L), dto.getId()),
            () -> assertNull(dto.getImageFrontUrl()),
            () -> assertNull(dto.getImageBackUrl())
        );
    }

    @Test
    public void givenRevisionWithoutCard_whenMapToCardDto_thenReturnNullCardId() {
        RevisionEdit revision = givenRevision();
        revision.setCard(null);

        CardSimpleDto dto = mapper.revisionEditToCardSimpleDto(revision);

        assertAll(
            () -> assertNull(dto.getId()),
            () -> assertEquals("Front text", dto.getTextFront()),
            () -> assertEquals("Back text", dto.getTextBack())
        );
    }

    @Test
    public void givenNullTexts_whenMapToCardDto_thenPreserveNullTexts() {
        RevisionEdit revision = givenRevision();
        revision.setTextFront(null);
        revision.setTextBack(null);
        revision.setImageFront(imageWithFilename("front.png"));

        CardSimpleDto dto = mapper.revisionEditToCardSimpleDto(revision);

        assertAll(
            () -> assertNull(dto.getTextFront()),
            () -> assertNull(dto.getTextBack()),
            () -> assertEquals("/images/front.png", dto.getImageFrontUrl()),
            () -> assertNull(dto.getImageBackUrl())
        );
    }

    @Test
    public void givenSuccessiveRevisions_whenMapToCardDtos_thenKeepTheirImageUrlsSeparate() {
        RevisionEdit first = givenRevision();
        first.setImageFront(imageWithFilename("first-front.png"));
        first.setImageBack(imageWithFilename("first-back.jpeg"));
        RevisionEdit second = givenRevision();
        second.setImageFront(imageWithFilename("second-front.png"));
        second.setImageBack(imageWithFilename("second-back.jpeg"));

        CardSimpleDto firstDto = mapper.revisionEditToCardSimpleDto(first);
        CardSimpleDto secondDto = mapper.revisionEditToCardSimpleDto(second);

        assertAll(
            () -> assertEquals("/images/first-front.png", firstDto.getImageFrontUrl()),
            () -> assertEquals("/images/first-back.jpeg", firstDto.getImageBackUrl()),
            () -> assertEquals("/images/second-front.png", secondDto.getImageFrontUrl()),
            () -> assertEquals("/images/second-back.jpeg", secondDto.getImageBackUrl())
        );
    }

    @Test
    public void givenChangedPrefix_whenMapAgain_thenUseUpdatedPrefix() {
        RevisionEdit revision = givenRevision();
        revision.setImageFront(imageWithFilename("front.png"));
        revision.setImageBack(imageWithFilename("back.jpeg"));
        CardSimpleDto firstDto = mapper.revisionEditToCardSimpleDto(revision);

        ReflectionTestUtils.setField(mapper, "imageServedPath", "/assets/");
        CardSimpleDto secondDto = mapper.revisionEditToCardSimpleDto(revision);

        assertAll(
            () -> assertEquals("/images/front.png", firstDto.getImageFrontUrl()),
            () -> assertEquals("/images/back.jpeg", firstDto.getImageBackUrl()),
            () -> assertEquals("/assets/front.png", secondDto.getImageFrontUrl()),
            () -> assertEquals("/assets/back.jpeg", secondDto.getImageBackUrl())
        );
    }

    private static RevisionEdit givenRevision() {
        Card card = new Card();
        card.setId(7L);

        RevisionEdit revision = new RevisionEdit();
        revision.setId(42L);
        revision.setCard(card);
        revision.setTextFront("Front text");
        revision.setTextBack("Back text");
        return revision;
    }

    private static Image imageWithFilename(String filename) {
        Image image = new Image();
        image.setFilename(filename);
        return image;
    }
}
