package at.ac.tuwien.sepm.groupphase.backend.repository;


import at.ac.tuwien.sepm.groupphase.backend.entity.Deck;
import at.ac.tuwien.sepm.groupphase.backend.entity.Progress;
import at.ac.tuwien.sepm.groupphase.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    /**
     * Find all decks containing {@code name} (case insensitive)
     *
     * @param name the search string
     * @param pageable the paging parameters
     * @return ordered list of all decks with names containing {@code name}
     */
    @EntityGraph(attributePaths = {"createdBy", "categories"})
    Page<Deck> findByNameContainingIgnoreCase(final String name, final Pageable pageable);

    /**
     * Find a specific deck by id
     * @param deckId of the deck to find
     * @return deck found
     */
    @EntityGraph(attributePaths = {"categories"})
    Optional<Deck> findById(final Long deckId);

    /**
     * Find page of decks created by user
     *
     * @param id of the user to query
     * @param pageable pagination parameters for the query
     * @return page of decks created by user
     */
    @EntityGraph(attributePaths = {"createdBy"})
    Page<Deck> findByCreatedBy_Id(final long id, final Pageable pageable);

    /**
     * Find all decks created by user for exporting data
     *
     * @param userId of the user to query
     * @return all decks created by user
     */
    Set<Deck> findExportByCreatedBy_Id(final long userId);

    /**
     * Find favorites of user
     *
     * @param userId id of the user
     * @param pageable pagination parameters for the query
     * @return page of favorites by user
     */
    Page<Deck> findByFavoredById(final Long userId, final Pageable pageable);

    /**
     * Find all favorites of user for exporting data
     *
     * @param userId id of the user
     * @return all favorites by user
     */
    Set<Deck> findExportByFavoredBy_Id(final Long userId);

    /**
     * Check if a deck is a favorite of a user
     *
     * @param deckId id of the deck
     * @param userId id of the user
     * @return false if deck is not a favorite of the user, or if deck/user does not exist
     */
    boolean existsByIdAndFavoredById(final Long deckId, final Long userId);

    /**
     * Count how many cards a given deck contains that have the given status for the given user
     * @param user
     * @param deck
     * @param status
     * @return Count how many cards a given deck contains that have the given status for the given user
     */
    @Query("SELECT count(c)" +
        " FROM Card c" +
        " INNER JOIN RevisionEdit r ON r=c.latestRevision" + // Exclude deleted cards
        " INNER JOIN Progress p ON c = p.id.card" +
        " WHERE p.id.user = :user AND p.id.card.deck = :deck AND p.id.reverse = :reverse AND p.status = :status")
    int countProgressStatuses(@Param("user") final User user, @Param("deck") final Deck deck, @Param("reverse") final boolean reverse, @Param("status") final Progress.Status status);

    /**
     * Count cards in deck
     * @param deck
     * @return how many cards are in the deck
     */
    @Query("SELECT count(c) FROM Card c inner join RevisionEdit r on r=c.latestRevision WHERE c.deck = :deck")
    int countCards(@Param("deck") final Deck deck);

    /**
     * Return all decks learned by a user.
     * @param user
     * @param pageable pagination parameters for the query.
     * @return decks learned by user
     */
    @Query("SELECT d FROM Deck d WHERE d.id IN (SELECT DISTINCT c.deck.id FROM Card c INNER JOIN RevisionEdit r ON r = c.latestRevision INNER JOIN Progress p ON r.card = p.id.card WHERE p.id.user = :user)")
    Page<Deck> findByUserProgress(@Param("user") final User user, final Pageable pageable);
}
