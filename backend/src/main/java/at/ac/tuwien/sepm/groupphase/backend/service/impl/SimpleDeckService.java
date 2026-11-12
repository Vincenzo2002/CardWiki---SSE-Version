package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DeckProgressDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DeckProgressDetailsDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.*;
import at.ac.tuwien.sepm.groupphase.backend.exception.BadRequestException;
import at.ac.tuwien.sepm.groupphase.backend.exception.DeckNotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.CardRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.DeckRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ProgressRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.RevisionRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.CategoryService;
import at.ac.tuwien.sepm.groupphase.backend.service.DeckService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimpleDeckService implements DeckService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final DeckRepository deckRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final CardRepository cardRepository;
    private final RevisionRepository revisionRepository;
    private final ProgressRepository progressRepository;

    public SimpleDeckService(
        DeckRepository deckRepository,
        UserService userService,
        CategoryService categoryService,
        CardRepository cardRepository,
        RevisionRepository revisionRepository,
        ProgressRepository progressRepository)
    {
        this.deckRepository = deckRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.cardRepository = cardRepository;
        this.revisionRepository = revisionRepository;
        this.progressRepository = progressRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public Deck findOneOrThrow(final Long id) {
        LOGGER.debug("Find deck with id {}", id);
        Objects.requireNonNull(id, "id argument must not be null");
        final Optional<Deck> deck = deckRepository.findById(id);
        return deck.orElseThrow(() -> new DeckNotFoundException(String.format("Could not find card deck with id %s", id)));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Deck> searchByName(final String name, final Pageable pageable) {
        LOGGER.debug("Search card decks for name {} {}", name, pageable);
        Objects.requireNonNull(name, "name argument must not be null");
        final Page<Deck> decks = deckRepository.findByNameContainingIgnoreCase(name, pageable);
        // The mapper reads the categories after the transaction; @BatchSize loads them for the whole page at once
        decks.forEach(deck -> Hibernate.initialize(deck.getCategories()));
        return decks;
    }

    @Transactional
    @Override
    public Deck create(final Deck deck) {
        LOGGER.debug("Create new deck {}", deck);
        Objects.requireNonNull(deck, "deck argument must not be null");
        final User user = userService.loadCurrentUserOrThrow();
        deck.setCreatedBy(user);
        deck.setCreatedBy(user);
        return deckRepository.save(deck);
    }

    @Transactional
    @Override
    public Deck update(final Long id, final Deck deckUpdate) {
        LOGGER.debug("Update deck with id: {}", id);
        final Deck deck = findOneOrThrow(id);

        if (deckUpdate.getName() != null) {
            deck.setName(deckUpdate.getName());
        }

        final Set<Category> categories = deck.getCategories();

        if (deckUpdate.getCategories() != null) {
            //add deck to new categories
            for (Category category : deckUpdate.getCategories()) {
                category = categoryService.findOneOrThrow(category.getId());
                category.getDecks().add(deck);
                categories.remove(category);
            }
            //remove deck from removed categories
            for (Category category : categories) {
                category = categoryService.findOneOrThrow(category.getId());
                category.getDecks().remove(deck);
            }
            deck.setCategories(deckUpdate.getCategories());
        }

        return deckRepository.save(deck);
    }

    @Transactional
    @Override
    public Deck copy(final Long id, final Deck deckCopy) {
        LOGGER.debug("Copy deck with id: {}", id);
        final User currentUser = userService.loadCurrentUserOrThrow();
        final Deck source = findOneOrThrow(id);

        final Deck deck = create(deckCopy);
        deck.setCategories(new HashSet<>(source.getCategories()));
        for (final Category category : source.getCategories()) {
            category.getDecks().add(deck);
        }
        deckRepository.save(deck);
        final String copyMessage = "Copied from deck " + id + ".";
        final List<Card> cards = cardRepository.findLatestEditRevisionsByDeck_Id(id).map((final RevisionEdit sourceRevision) -> {
            final Card card = new Card();
            card.setDeck(deck);
            final RevisionCreate rev = new RevisionCreate();
            rev.setMessage(copyMessage);
            rev.setCreatedBy(currentUser);
            rev.setCard(card);
            rev.setTextFront(sourceRevision.getTextFront());
            rev.setTextBack(sourceRevision.getTextBack());
            rev.setImageFront(sourceRevision.getImageFront());
            rev.setImageBack(sourceRevision.getImageBack());
            card.setLatestRevision(rev);
            return card;
        }).collect(Collectors.toList());

        cardRepository.saveAll(cards);
        cardRepository.flush();
        deck.getCards().addAll(cards);
        return deck;
    }

    @Transactional
    @Override
    public void delete(final Long id) {
        LOGGER.debug("Delete deck with id {}", id);
        final Deck deck = findOneOrThrow(id);
        for (final Category category : deck.getCategories())
            category.getDecks().remove(deck);
        deckRepository.deleteById(id);
	}

    @Transactional
    @Override
    public Page<Revision> getRevisions(final Long id, final Pageable pageable) {
        LOGGER.debug("Load {} revisions with offset {} from deck {}", pageable.getPageSize(), pageable.getOffset(), id);
        return revisionRepository.findByCard_Deck_Id(id, pageable);
    }

    @Override
    @Transactional
    public Page<DeckProgressDetailsDto> getLearnedDecksWithStatus(final Pageable pageable) {
        LOGGER.debug("Get learned decks");
        final User user = userService.loadCurrentUserOrThrow();
        return deckRepository.findByUserProgress(user, pageable)
            .map((final Deck deck) -> getDeckProgressDetails(deck, user));
    }

    @Transactional
    public DeckProgressDetailsDto getDeckProgressDetails(final Deck deck, final User user) {
        final DeckProgressDetailsDto deckProgressDetailsDto = new DeckProgressDetailsDto();
        deckProgressDetailsDto.setDeckId(deck.getId());
        deckProgressDetailsDto.setDeckName(deck.getName());
        deckProgressDetailsDto.setNormal(getDeckProgress(deck, user, false));
        deckProgressDetailsDto.setReverse(getDeckProgress(deck, user, true));
        return deckProgressDetailsDto;
    }

    /**
     * @return deck progress or null if no progress has been made
     */
    @Transactional
    public DeckProgressDto getDeckProgress(final Deck deck, final User user, final boolean reverse) {
        final int totalCount = deckRepository.countCards(deck);
        final int dueCount = (int) cardRepository.dueCardsCount(deck.getId(), user.getId(), reverse);
        final int learningCount = deckRepository.countProgressStatuses(user, deck, reverse, Progress.Status.LEARNING);
        final int reviewCount = deckRepository.countProgressStatuses(user, deck, reverse, Progress.Status.REVIEWING);
        final int newCount = totalCount - (learningCount + reviewCount);

        final DeckProgressDto dto = new DeckProgressDto();
        dto.setTotalCount(totalCount);
        dto.setNewCount(newCount);
        dto.setLearningCount(learningCount);
        dto.setDueCount(dueCount);
        return (newCount == totalCount) ? null : dto;
    }

    @Override
    @Transactional
    public void deleteUserProgress(final Long deckId, final boolean reverse) {
        LOGGER.debug("Delete progress for deck with id {} reverse={}", deckId, reverse);
        final Long userId = userService.loadCurrentUserOrThrow().getId();
        findOneOrThrow(deckId);
        progressRepository.deleteById_UserIdAndId_Card_Deck_IdAndId_Reverse(userId, deckId, reverse);
    }

    @Override
    @Transactional
    public void createCsvData(final PrintWriter pw, final Long deckId) throws IOException {
        LOGGER.debug("Write deck with id {} to file.", deckId);
        try (final CSVPrinter printer = new CSVPrinter(pw, CSVFormat.DEFAULT)) {
            cardRepository.findLatestEditRevisionsByDeck_Id(deckId)
                .forEach((final RevisionEdit revisionEdit) -> {
                    try {
                        printer.printRecord(revisionEdit.getTextFront(), revisionEdit.getTextBack());
                    } catch (final IOException ex) {
                        // Wrapping as unchecked exception to get it outside of lambda expression
                        throw new UncheckedIOException(ex);
                    }
                });
        } catch (final UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    @Override
    @Transactional
    public Deck addCards(final Long deckId, final MultipartFile file) throws IOException {
        LOGGER.debug("Importing file into deck {}: {}", deckId, file);

        // fetch information
        final Deck deck = findOneOrThrow(deckId);
        final User user = userService.loadCurrentUserOrThrow();

        // parse csv file
        final Reader in = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        final CSVParser csvParser = new CSVParser(in, CSVFormat.DEFAULT.withTrim().withIgnoreSurroundingSpaces());

        // parse into revisions and validate
        final List<RevisionCreate> parsedRevisions = csvParser.getRecords().stream()
            .map((final CSVRecord csvRecord) -> {
                if (csvRecord.size() != 2) {
                    throw new BadRequestException("Every row of the csv file must have exactly 2 columns");
                }
                final RevisionCreate revisionCreate = new RevisionCreate();
                revisionCreate.setTextFront(csvRecord.get(0));
                revisionCreate.setTextBack(csvRecord.get(1));

                if (revisionCreate.getTextFront().trim().isEmpty())
                    throw new BadRequestException("Front side may not be empty");
                if (revisionCreate.getTextBack().trim().isEmpty())
                    throw new BadRequestException("Back side may not be empty");

                return revisionCreate;
            }).collect(Collectors.toList());
        final List<String> parsedFrontTexts = parsedRevisions.stream().map(RevisionEdit::getTextFront).collect(Collectors.toList());

        // get front texts which already exist in deck
        final Set<String> existingFrontTexts = cardRepository.filterExistingFrontTexts(deckId, parsedFrontTexts);

        // Add new cards to deck
        final List<Card> newCards = parsedRevisions.stream()
            .filter((final RevisionCreate r) -> !existingFrontTexts.contains(r.getTextFront()))
            .map((final RevisionCreate r) -> {
                r.setMessage("Imported");
                r.setCreatedBy(user);
                user.getRevisions().add(r);

                final Card card = new Card();
                card.setLatestRevision(r);
                card.getRevisions().add(r);
                r.setCard(card);
                card.setDeck(deck);
                deck.getCards().add(card);

                return card;
            }).collect(Collectors.toList());

        cardRepository.saveAll(newCards);
        return deckRepository.saveAndFlush(deck);
    }
}
