package at.ac.tuwien.sepm.groupphase.backend.unittests;

import at.ac.tuwien.sepm.groupphase.backend.entity.*;
import at.ac.tuwien.sepm.groupphase.backend.exception.AuthenticationRequiredException;
import at.ac.tuwien.sepm.groupphase.backend.exception.DeckNotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.CardRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.DeckRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.SimpleDeckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DeckCopyTest {

    private SimpleDeckService service;
    private DeckRepository decks;
    private CardRepository cards;
    private UserService users;
    private User currentUser;
    private User originalAuthor;
    private Deck source;
    private List<RevisionEdit> revisions;
    private List<Card> savedCards;

    @BeforeEach
    public void setup() {
        decks = mock(DeckRepository.class);
        cards = mock(CardRepository.class);
        users = mock(UserService.class);
        service = new SimpleDeckService(decks, users, null, cards, null, null);
        currentUser = new User(1L);
        originalAuthor = new User(2L);
        source = new Deck();
        source.setId(42L);
        source.setName("Source deck");
        source.setCreatedBy(originalAuthor);
        revisions = new ArrayList<>();

        when(users.loadCurrentUserOrThrow()).thenReturn(currentUser);
        when(decks.findById(anyLong())).thenReturn(Optional.of(source));
        when(decks.save(any(Deck.class))).thenAnswer(call -> call.getArgument(0));
        when(cards.findLatestEditRevisionsByDeck_Id(anyLong()))
            .thenAnswer(call -> revisions.stream());
        when(cards.saveAll(any())).thenAnswer(call -> {
            savedCards = call.getArgument(0);
            return savedCards;
        });
    }

    @ParameterizedTest(name = "copy {0} cards")
    @ValueSource(ints = {0, 1, 10, 100, 1000})
    public void givenSourceDeck_whenCopy_thenCreateIndependentCardsAndPreserveContent(int size) {
        populate(size);
        Deck destination = destination();

        Deck result = service.copy(source.getId(), destination);

        assertSame(destination, result);
        assertCopy(result, "Copied from deck 42.");
        assertSourceUnchanged();
        assertEquals(result.getCards(), new HashSet<>(savedCards));
        verify(decks).findById(42L);
        verify(decks, times(2)).save(destination);
        verify(cards).findLatestEditRevisionsByDeck_Id(42L);
        verify(cards).saveAll(savedCards);
        verify(cards).flush();
    }

    @ParameterizedTest(name = "front image={0}, back image={1}")
    @CsvSource({"false, false", "true, false", "false, true", "true, true"})
    public void givenOptionalImages_whenCopy_thenPreserveImageReferences(boolean front, boolean back) {
        populate(1);
        if (!front) revisions.get(0).setImageFront(null);
        if (!back) revisions.get(0).setImageBack(null);

        Deck result = service.copy(source.getId(), destination());

        assertCopy(result, "Copied from deck 42.");
        assertSourceUnchanged();
    }

    @ParameterizedTest(name = "source ID={0}")
    @CsvSource({
        "0, Copied from deck 0.",
        "42, Copied from deck 42.",
        "9223372036854775807, Copied from deck 9223372036854775807."
    })
    public void givenSourceId_whenCopy_thenKeepExactMessage(long id, String expectedMessage) {
        source.setId(id);
        populate(2);

        Deck result = service.copy(id, destination());

        assertCopy(result, expectedMessage);
        verify(cards).findLatestEditRevisionsByDeck_Id(id);
    }

    @Test
    public void givenCategories_whenCopy_thenPreserveSourceMembershipAndAddDestination() {
        populate(1);
        Category first = new Category(3L, "First");
        Category second = new Category(4L, "Second");
        source.getCategories().add(first);
        source.getCategories().add(second);
        first.getDecks().add(source);
        second.getDecks().add(source);

        Deck result = service.copy(source.getId(), destination());

        assertCopy(result, "Copied from deck 42.");
        assertSourceUnchanged();
        assertEquals(2, source.getCategories().size());
        for (Category category : source.getCategories()) {
            assertTrue(category.getDecks().contains(source));
            assertTrue(category.getDecks().contains(result));
            assertEquals(2, category.getDecks().size());
        }
    }

    @Test
    public void givenSuccessiveCopies_whenCopy_thenKeepDestinationGraphsSeparate() {
        populate(3);

        Deck first = service.copy(source.getId(), destination());
        Deck second = service.copy(source.getId(), destination());

        assertNotSame(first, second);
        assertCopy(first, "Copied from deck 42.");
        assertCopy(second, "Copied from deck 42.");
        for (Card card : first.getCards()) assertFalse(second.getCards().contains(card));
        assertSourceUnchanged();
    }

    @Test
    public void givenMissingUser_whenCopy_thenRejectBeforeRepositoryAccess() {
        when(users.loadCurrentUserOrThrow())
            .thenThrow(new AuthenticationRequiredException("No current user"));

        assertThrows(AuthenticationRequiredException.class, () -> service.copy(42L, destination()));

        verifyNoInteractions(decks, cards);
    }

    @Test
    public void givenMissingSource_whenCopy_thenRejectWithoutSaving() {
        when(decks.findById(42L)).thenReturn(Optional.empty());

        assertThrows(DeckNotFoundException.class, () -> service.copy(42L, destination()));

        verify(decks, never()).save(any(Deck.class));
        verifyNoInteractions(cards);
    }

    @Test
    public void givenNullDestination_whenCopy_thenRejectWithoutSaving() {
        assertThrows(NullPointerException.class, () -> service.copy(42L, null));

        verify(decks, never()).save(any(Deck.class));
        verifyNoInteractions(cards);
    }

    @Test
    public void givenNullSourceId_whenCopy_thenRejectWithoutRepositoryAccess() {
        assertThrows(NullPointerException.class, () -> service.copy(null, destination()));

        verifyNoInteractions(decks, cards);
    }

    private static Deck destination() {
        Deck deck = new Deck();
        deck.setName("Copied deck");
        return deck;
    }

    private void populate(int size) {
        for (int i = 0; i < size; i++) {
            Card card = new Card();
            card.setId((long) i + 1);
            card.setDeck(source);
            RevisionEdit revision = new RevisionEdit();
            revision.setId((long) i + 10000);
            revision.setCard(card);
            revision.setCreatedBy(originalAuthor);
            revision.setMessage("Original message");
            revision.setTextFront("Front " + i + ", accented à\nnext line");
            revision.setTextBack("Back " + i);
            Image front = new Image();
            front.setFilename("front-" + i + ".png");
            Image back = new Image();
            back.setFilename("back-" + i + ".png");
            revision.setImageFront(front);
            revision.setImageBack(back);
            card.setLatestRevision(revision);
            source.getCards().add(card);
            revisions.add(revision);
        }
    }

    private void assertCopy(Deck copy, String expectedMessage) {
        assertNotSame(source, copy);
        assertEquals("Copied deck", copy.getName());
        assertSame(currentUser, copy.getCreatedBy());
        assertEquals(source.getCategories(), copy.getCategories());
        assertNotSame(source.getCategories(), copy.getCategories());
        assertEquals(revisions.size(), copy.getCards().size());
        Map<String, RevisionEdit> originals = revisions.stream()
            .collect(Collectors.toMap(RevisionEdit::getTextFront, revision -> revision));
        for (Card card : copy.getCards()) {
            assertSame(copy, card.getDeck());
            assertFalse(source.getCards().contains(card));
            assertTrue(card.getLatestRevision() instanceof RevisionCreate);
            RevisionEdit revision = (RevisionEdit) card.getLatestRevision();
            RevisionEdit original = originals.get(revision.getTextFront());
            assertNotNull(original);
            assertNotSame(original, revision);
            assertSame(card, revision.getCard());
            assertSame(currentUser, revision.getCreatedBy());
            assertEquals(original.getTextFront(), revision.getTextFront());
            assertEquals(original.getTextBack(), revision.getTextBack());
            assertSame(original.getImageFront(), revision.getImageFront());
            assertSame(original.getImageBack(), revision.getImageBack());
            assertEquals(expectedMessage, revision.getMessage());
        }
    }

    private void assertSourceUnchanged() {
        assertEquals("Source deck", source.getName());
        assertSame(originalAuthor, source.getCreatedBy());
        assertEquals(revisions.size(), source.getCards().size());
        for (RevisionEdit revision : revisions) {
            assertSame(originalAuthor, revision.getCreatedBy());
            assertEquals("Original message", revision.getMessage());
            assertSame(source, revision.getCard().getDeck());
            assertSame(revision, revision.getCard().getLatestRevision());
        }
    }
}
