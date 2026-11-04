package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.entity.*;
import at.ac.tuwien.sepm.groupphase.backend.exception.CardNotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.*;
import at.ac.tuwien.sepm.groupphase.backend.repository.CardRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ProgressRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.RevisionRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.CardService;
import at.ac.tuwien.sepm.groupphase.backend.service.DeckService;
import at.ac.tuwien.sepm.groupphase.backend.service.ImageService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimpleCardService implements CardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserService userService;
    private final DeckService deckService;
    private final CardRepository cardRepository;
    private final ProgressRepository progressRepository;
    private final ImageService imageService;
    private final RevisionRepository revisionRepository;

    public SimpleCardService(
        CardRepository cardRepository,
        DeckService deckService,
        UserService userService,
        ProgressRepository progressRepository,
        ImageService imageService,
        RevisionRepository revisionRepository)
    {
        this.cardRepository = cardRepository;
        this.userService = userService;
        this.deckService = deckService;
        this.progressRepository = progressRepository;
        this.imageService = imageService;
        this.revisionRepository = revisionRepository;
    }

    @Override
    @Transactional
    public Card addCardToDeck(final Long deckId, final RevisionCreate revisionCreate) {
        LOGGER.debug("Add Card to Deck: {} {}", revisionCreate, deckId);
        final User user = userService.loadCurrentUserOrThrow();
        final Deck deck = deckService.findOneOrThrow(deckId);
        validateRevisionEdit(revisionCreate);

        // Save Card with initial revision
        final Card card = new Card();
        card.setDeck(deck);

        revisionCreate.setMessage(revisionCreate.getMessage() != null ? revisionCreate.getMessage() : "Created");
        try {
            if (revisionCreate.getImageFront() != null)
                imageService.findOneOrThrow(revisionCreate.getImageFront().getFilename());
            if (revisionCreate.getImageBack() != null)
                imageService.findOneOrThrow(revisionCreate.getImageBack().getFilename());
        } catch (final ImageNotFoundException ex) {
            throw new BadRequestException(ex.getMessage());
        }
        card.setLatestRevision(revisionCreate);
        revisionCreate.setCard(card);
        revisionCreate.setCreatedBy(user);

        return cardRepository.saveAndFlush(card);
    }

    @Override
    @Transactional
    public Page<RevisionEdit> findLatestEditRevisionsByDeckId(final Long deckId, final Pageable pageable) {
        LOGGER.debug("Find latest edit revisions id {} {}", deckId, pageable);
        deckService.findOneOrThrow(deckId);
        return cardRepository.findLatestEditRevisionsByDeck_Id(deckId, pageable);
    }

    @Override
    @Transactional
    public void addDeleteRevisionToCard(final Long cardId, final String revisionMessage) {
        LOGGER.debug("Add delete-revision to card with id {}", cardId);
        final Card card = findOneOrThrow(cardId);
        final User user = userService.loadCurrentUserOrThrow();

        final RevisionDelete revision = new RevisionDelete();
        revision.setCard(card);
        revision.setMessage(revisionMessage == null ? "Deleted" : revisionMessage);
        card.setLatestRevision(revision);
        revision.setCreatedBy(user);

        cardRepository.save(card);
    }

    @Transactional
    public Card findOneOrThrow(final Long cardId) {
        LOGGER.debug("Find card with id {}", cardId);
        final Optional<Card> card = cardRepository.findById(cardId);
        return card.orElseThrow(() -> new CardNotFoundException(("Could not find card with id " + cardId)));
    }

    @Override
    @Transactional
    public void delete(final Long cardId) {
        LOGGER.debug("Delete card with id {}", cardId);
        if (progressRepository.existsById_CardId(cardId)) {
            progressRepository.deleteById_CardId(cardId);
        }
        try {
            cardRepository.deleteById(cardId);
        } catch (final EmptyResultDataAccessException e) {
            throw new CardNotFoundException("Could not find card with id " + cardId);
        }
    }

    @Override
    @Transactional
    public Card editCardInDeck(final Long cardId, final RevisionEdit revisionEdit) {
        LOGGER.debug("Edit Card {}: {}", cardId, revisionEdit);
        final User user = userService.loadCurrentUserOrThrow();
        final Card card = findOneOrThrow(cardId);
        validateRevisionEdit(revisionEdit);

        revisionEdit.setMessage(revisionEdit.getMessage() != null ? revisionEdit.getMessage() : "Edited");
        try {
            if (revisionEdit.getImageFront() != null)
                imageService.findOneOrThrow(revisionEdit.getImageFront().getFilename());
            if (revisionEdit.getImageBack() != null)
                imageService.findOneOrThrow(revisionEdit.getImageBack().getFilename());
        } catch (final ImageNotFoundException ex) {
            throw new BadRequestException(ex.getMessage());
        }
        card.setLatestRevision(revisionEdit);
        revisionEdit.setCard(card);
        revisionEdit.setCreatedBy(user);

        return cardRepository.saveAndFlush(card);
    }

    public void validateRevisionEdit(final RevisionEdit revisionEdit) {
        if (revisionEdit.getTextFront() == null && revisionEdit.getImageFront() == null) {
            throw new BadRequestException("Front side cannot be empty");
        }
        if (revisionEdit.getTextBack() == null && revisionEdit.getImageBack() == null) {
            throw new BadRequestException("Back side cannot be empty");
        }
    }

    @Override
    @Transactional
    public Page<Revision> getRevisionsOfCard(final Long id, final Pageable pageable) {
        LOGGER.debug("Load {} revisions with offset {} from card {}", pageable.getPageSize(), pageable.getOffset(), id);
        return revisionRepository.findByCard_Id(id, pageable);
    }

    private static final int MAX_REVISIONS_COUNT = 10;

    @Override
    public List<Revision> getRevisionsByIds(final Long[] ids) {
        if (ids.length > MAX_REVISIONS_COUNT)
            throw new BadRequestException(String.format("You may not query more than %d revisions at once.", MAX_REVISIONS_COUNT));
        return revisionRepository.findByIdIn(Arrays.stream(ids).collect(Collectors.toList()));
    }

    @Override
    public Page<Revision> getRecentRevisions(final Pageable pageable) {
        return revisionRepository.findAll(pageable);
    }
}
