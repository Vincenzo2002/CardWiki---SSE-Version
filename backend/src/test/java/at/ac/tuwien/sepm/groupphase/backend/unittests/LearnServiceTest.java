package at.ac.tuwien.sepm.groupphase.backend.unittests;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestDataGenerator;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.AttemptInputDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Card;
import at.ac.tuwien.sepm.groupphase.backend.entity.Progress;
import at.ac.tuwien.sepm.groupphase.backend.entity.User;
import at.ac.tuwien.sepm.groupphase.backend.exception.BadRequestException;
import at.ac.tuwien.sepm.groupphase.backend.repository.DeckRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ProgressRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.LearnService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class LearnServiceTest extends TestDataGenerator {
    @MockBean
    private ProgressRepository progressRepository;

    @MockBean
    private DeckRepository deckRepository;

    @MockBean
    private UserService userService;

    @Autowired
    private LearnService learnService;

    @Captor
    ArgumentCaptor<Progress> argumentCaptor;

    @Test
    public void givenNothing_whenFindNextNonExistentDeck_thenThrowBadRequestException() {
        Mockito.when(deckRepository.existsById(any())).thenReturn(false);

        assertThrows(
            BadRequestException.class,
            () -> learnService.findNextCards(1L, false, Pageable.unpaged())
        );
    }

    @Test
    public void givenNothing_whenEASY() {
        Mockito.when(progressRepository.findById(any())).thenReturn(Optional.empty());

        assertLearnResult(AttemptInputDto.Status.EASY, Progress.Status.REVIEWING, 4, 2.5);
    }

    @Test
    public void givenNothing_whenGOOD() {
        Mockito.when(progressRepository.findById(any())).thenReturn(Optional.empty());

        assertLearnResult(AttemptInputDto.Status.GOOD, Progress.Status.LEARNING, 10, 2.5);
    }

    @Test
    public void givenNothing_whenAGAIN() {
        Mockito.when(progressRepository.findById(any())).thenReturn(Optional.empty());

        assertLearnResult(AttemptInputDto.Status.AGAIN, Progress.Status.LEARNING, 1, 2.5);
    }

    @Test
    public void givenLearning_whenGOOD() {
        Progress progress = new Progress();
        progress.setStatus(Progress.Status.LEARNING);
        progress.setInterval(10);
        progress.setEasinessFactor(2.5);

        Mockito.when(progressRepository.findById(any())).thenReturn(Optional.of(progress));

        assertLearnResult(AttemptInputDto.Status.GOOD, Progress.Status.REVIEWING, 1, 2.5);
    }

    @Test
    public void givenReviewing_whenEASY() {
        Progress progress = new Progress();
        progress.setStatus(Progress.Status.REVIEWING);
        progress.setInterval(4);
        progress.setEasinessFactor(2.5);

        Mockito.when(progressRepository.findById(any())).thenReturn(Optional.of(progress));

        assertLearnResult(AttemptInputDto.Status.EASY, Progress.Status.REVIEWING, 14, 2.65);
    }

    @Test
    public void givenReviewing_whenGOOD() {
        Progress progress = new Progress();
        progress.setStatus(Progress.Status.REVIEWING);
        progress.setInterval(4);
        progress.setEasinessFactor(2.5);

        Mockito.when(progressRepository.findById(any())).thenReturn(Optional.of(progress));

        assertLearnResult(AttemptInputDto.Status.GOOD, Progress.Status.REVIEWING, 10, 2.5);
    }

    @Test
    public void givenReviewing_whenAGAIN() {
        Progress progress = new Progress();
        progress.setStatus(Progress.Status.REVIEWING);
        progress.setInterval(4);
        progress.setEasinessFactor(2.5);

        Mockito.when(progressRepository.findById(any())).thenReturn(Optional.of(progress));

        assertLearnResult(AttemptInputDto.Status.AGAIN, Progress.Status.REVIEWING, 1, 2.3);
    }

    @ParameterizedTest(name = "new progress: answer={0}, reverse={1}")
    @CsvSource({
        "AGAIN, false, LEARNING, 1",
        "AGAIN, true, LEARNING, 1",
        "GOOD, false, LEARNING, 10",
        "GOOD, true, LEARNING, 10",
        "EASY, false, REVIEWING, 4",
        "EASY, true, REVIEWING, 4"
    })
    public void givenMissingProgress_whenSaveAttempt_thenCreateProgressWithCorrectIdentityAndSchedule(
        AttemptInputDto.Status answer, boolean reverse, Progress.Status expectedStatus,
        int expectedInterval) {
        Mockito.when(progressRepository.findById(any())).thenReturn(Optional.empty());

        assertSavedAttempt(new User(1L), attempt(answer, reverse), null,
            expectedStatus, expectedInterval, 2.5);
    }

    @ParameterizedTest(name = "existing {0}: interval={1}, ease={2}, answer={3}, reverse={7}")
    @CsvSource({
        "LEARNING, 1, 2.5, AGAIN, LEARNING, 1, 2.5, false",
        "LEARNING, 1, 2.5, AGAIN, LEARNING, 1, 2.5, true",
        "LEARNING, 1, 2.5, GOOD, LEARNING, 10, 2.5, false",
        "LEARNING, 1, 2.5, GOOD, LEARNING, 10, 2.5, true",
        "LEARNING, 1, 2.5, EASY, REVIEWING, 4, 2.5, false",
        "LEARNING, 1, 2.5, EASY, REVIEWING, 4, 2.5, true",
        "LEARNING, 10, 2.5, AGAIN, LEARNING, 1, 2.5, false",
        "LEARNING, 10, 2.5, AGAIN, LEARNING, 1, 2.5, true",
        "LEARNING, 10, 2.5, GOOD, REVIEWING, 1, 2.5, false",
        "LEARNING, 10, 2.5, GOOD, REVIEWING, 1, 2.5, true",
        "LEARNING, 10, 2.5, EASY, REVIEWING, 4, 2.5, false",
        "LEARNING, 10, 2.5, EASY, REVIEWING, 4, 2.5, true",
        "REVIEWING, 4, 2.5, AGAIN, REVIEWING, 1, 2.3, false",
        "REVIEWING, 4, 2.5, AGAIN, REVIEWING, 1, 2.3, true",
        "REVIEWING, 4, 2.5, GOOD, REVIEWING, 10, 2.5, false",
        "REVIEWING, 4, 2.5, GOOD, REVIEWING, 10, 2.5, true",
        "REVIEWING, 4, 2.5, EASY, REVIEWING, 14, 2.65, false",
        "REVIEWING, 4, 2.5, EASY, REVIEWING, 14, 2.65, true",
        "REVIEWING, 4, 1.3, AGAIN, REVIEWING, 1, 1.3, false",
        "REVIEWING, 4, 1.3, AGAIN, REVIEWING, 1, 1.3, true"
    })
    public void givenExistingProgress_whenSaveAttempt_thenUpdateSameProgressAndPreserveIdentity(
        Progress.Status initialStatus, int initialInterval, double initialEF,
        AttemptInputDto.Status answer, Progress.Status expectedStatus,
        int expectedInterval, double expectedEF, boolean reverse) {
        User user = new User(1L);
        Card card = new Card();
        card.setId(7L);
        Progress existing = new Progress(new Progress.Id(user, card, reverse));
        existing.setStatus(initialStatus);
        existing.setInterval(initialInterval);
        existing.setEasinessFactor(initialEF);
        existing.setDue(LocalDateTime.of(2000, 1, 1, 0, 0));
        Mockito.when(progressRepository.findById(any())).thenReturn(Optional.of(existing));

        assertSavedAttempt(user, attempt(answer, reverse), existing,
            expectedStatus, expectedInterval, expectedEF);
    }

    private static AttemptInputDto attempt(AttemptInputDto.Status answer, boolean reverse) {
        AttemptInputDto attempt = new AttemptInputDto();
        attempt.setCardId(7L);
        attempt.setStatus(answer);
        attempt.setReverse(reverse);
        return attempt;
    }

    private void assertSavedAttempt(User user, AttemptInputDto attempt, Progress existing,
                                    Progress.Status expectedStatus, int expectedInterval,
                                    double expectedEF) {
        Mockito.when(userService.loadCurrentUserOrThrow()).thenReturn(user);
        Progress.Id originalId = existing == null ? null : existing.getId();
        LocalDateTime before = LocalDateTime.now();

        learnService.saveAttempt(attempt);

        LocalDateTime after = LocalDateTime.now();
        ArgumentCaptor<Progress.Id> idCaptor = ArgumentCaptor.forClass(Progress.Id.class);
        Mockito.verify(progressRepository).findById(idCaptor.capture());
        Mockito.verify(progressRepository).saveAndFlush(argumentCaptor.capture());
        Progress saved = argumentCaptor.getValue();
        Progress.Id lookupId = idCaptor.getValue();

        assertAll(
            () -> assertSame(user, lookupId.getUser()),
            () -> assertEquals(attempt.getCardId(), lookupId.getCard().getId()),
            () -> assertEquals(attempt.isReverse(), lookupId.isReverse()),
            () -> assertEquals(expectedStatus, saved.getStatus()),
            () -> assertEquals(expectedInterval, saved.getInterval()),
            () -> assertEquals(expectedEF, saved.getEasinessFactor(), 0.000000001)
        );
        if (existing == null) {
            assertSame(lookupId, saved.getId());
        } else {
            assertSame(existing, saved);
            assertSame(originalId, saved.getId());
        }

        LocalDateTime minimumDue = expectedStatus == Progress.Status.LEARNING
            ? before.plusMinutes(expectedInterval) : before.plusDays(expectedInterval);
        LocalDateTime maximumDue = expectedStatus == Progress.Status.LEARNING
            ? after.plusMinutes(expectedInterval) : after.plusDays(expectedInterval);
        assertNotNull(saved.getDue());
        assertFalse(saved.getDue().isBefore(minimumDue));
        assertFalse(saved.getDue().isAfter(maximumDue));
    }

    private void assertLearnResult(AttemptInputDto.Status result, Progress.Status expectedStatus, int expectedInterval, double expectedEF) {
        User user = getSampleUser();
        AttemptInputDto attempt = getSampleAttempt();
        attempt.setStatus(result);

        Mockito.when(userService.loadCurrentUserOrThrow()).thenReturn(user);

        learnService.saveAttempt(attempt);

        Mockito.verify(progressRepository).saveAndFlush(argumentCaptor.capture());
        assertEquals(expectedStatus, argumentCaptor.getValue().getStatus());
        assertEquals(expectedInterval, argumentCaptor.getValue().getInterval());
        assertEquals(expectedEF, argumentCaptor.getValue().getEasinessFactor());
    }
}
