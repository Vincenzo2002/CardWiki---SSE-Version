package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.AttemptInputDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Card;
import at.ac.tuwien.sepm.groupphase.backend.entity.Progress;
import at.ac.tuwien.sepm.groupphase.backend.entity.User;
import at.ac.tuwien.sepm.groupphase.backend.exception.BadRequestException;
import at.ac.tuwien.sepm.groupphase.backend.repository.CardRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.DeckRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ProgressRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.LearnService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class SimpleLearnService implements LearnService {

    public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ProgressRepository progressRepository;
    private final DeckRepository deckRepository;
    private final UserService userService;
    private final CardRepository cardRepository;

    @Autowired
    public SimpleLearnService(ProgressRepository progressRepository, DeckRepository deckRepository, UserService userService, CardRepository cardRepository) {
        this.progressRepository = progressRepository;
        this.deckRepository = deckRepository;
        this.userService = userService;
        this.cardRepository = cardRepository;
    }

    @Override
    public List<Card> findNextCards(final Long deckId, final boolean reverse, final Pageable pageable) {
        LOGGER.debug("Get next card for deck with id {} reverse={}", deckId, reverse);

        final User user = userService.loadCurrentUserOrThrow();

        if (!deckRepository.existsById(deckId)){
            throw new BadRequestException("deckId does not exist");
        }

        return cardRepository.findNextCards(deckId, user.getId(), reverse, pageable).getContent();
    }

    private static final int[] LEARNING_STEPS = {1, 10}; // in minutes
    private static final int GRADUATING_INTERVAL = 1; // in days
    private static final int EASY_INTERVAL = 4; // in days
    public static final double EASY_BONUS = 1.3;

    @Override
    public void saveAttempt(final AttemptInputDto attempt) {
        LOGGER.debug("Save new attempt {}", attempt);
        final User user = userService.loadCurrentUserOrThrow();
        final Card card = new Card();
        card.setId(attempt.getCardId());

        final Progress.Id id = new Progress.Id(user, card, attempt.isReverse());

        final Progress progress = progressRepository.findById(id).orElseGet(() -> new Progress(id));

        // This spaced-repetition algorithm is based on Anki's algorithm, which is based on SuperMemo 2.

        if (progress.getStatus() == Progress.Status.LEARNING){
            switch (attempt.getStatus()) {
                case EASY:
                    progress.setInterval(EASY_INTERVAL);
                    progress.setStatus(Progress.Status.REVIEWING);
                    progress.setDue(LocalDateTime.now(ZoneId.systemDefault()).plusDays(progress.getInterval()));
                    break;

                case GOOD:
                    if (progress.getInterval() >= LEARNING_STEPS[LEARNING_STEPS.length - 1]) {
                        progress.setInterval(GRADUATING_INTERVAL);
                        progress.setStatus(Progress.Status.REVIEWING);
                        progress.setDue(LocalDateTime.now(ZoneId.systemDefault()).plusDays(progress.getInterval()));
                    } else {
                        for (final int min : LEARNING_STEPS){
                            if (min > progress.getInterval()){
                                progress.setInterval(min);
                                break;
                            }
                        }
                        progress.setDue(LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(progress.getInterval()));
                    }
                    break;

                case AGAIN:
                    progress.setInterval(LEARNING_STEPS[0]);
                    progress.setDue(LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(progress.getInterval()));
                    break;
            }
        } else {
            switch (attempt.getStatus()) {
                case EASY:
                    progress.setEasinessFactor(progress.getEasinessFactor() + 0.15);
                    progress.setInterval((int) Math.ceil(progress.getInterval() * progress.getEasinessFactor() * EASY_BONUS));
                    break;

                case GOOD:
                    progress.setInterval((int) Math.ceil(progress.getInterval() * progress.getEasinessFactor()));
                    break;

                case AGAIN:
                    progress.setEasinessFactor(Math.max(progress.getEasinessFactor() - 0.2, 1.3));
                    progress.setInterval((int) Math.ceil(progress.getInterval() * 0.2));
                    break;
            }

            progress.setDue(LocalDateTime.now(ZoneId.systemDefault()).plusDays(progress.getInterval()));
        }

        try {
            progressRepository.saveAndFlush(progress);
        } catch (final DataIntegrityViolationException e){
            if (e.getCause().getClass() == ConstraintViolationException.class){
                final ConstraintViolationException cve = (ConstraintViolationException) e.getCause();
                // TODO: change contains to equals after hibernate version contains
                //  https://github.com/hibernate/hibernate-orm/pull/3417
                if (cve.getConstraintName().contains(Progress.FKNAME_CARD))
                    throw new BadRequestException("cardId not found");
                else
                    throw e;
            }
        }
    }
}
