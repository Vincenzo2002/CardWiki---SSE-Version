package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Revision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RevisionRepository extends JpaRepository<Revision, Long> {
    /**
     * Find all revisions
     * @param pageable
     * @return page of revisions
     */
    @Override
    @EntityGraph(attributePaths = {"card", "card.deck", "createdBy"})
    Page<Revision> findAll(final Pageable pageable);

    /**
     * Find all revisions created by user
     *
     * @param userId of the user to query
     * @param pageable pagination parameters for the query
     * @return ordered page of all revisions created by user
     */
    @EntityGraph(attributePaths = {"card", "card.deck", "createdBy"})
    Page<Revision> findByCreatedBy_Id(final long userId, final Pageable pageable);

    /**
     * Find all revisions created by user and preload for exporting data
     *
     * @param userId of the user to query
     * @return all revisions created by user
     */
    @EntityGraph(attributePaths = {"card", "card.deck", "createdBy"})
    Set<Revision> findExportByCreatedBy_Id(final long userId);

    /**
     * Find all revisions of a card
     *
     * @param id of the card
     * @param pageable pagination parameters for the query
     * @return ordered list of all revisions of the card
     */
    @EntityGraph(attributePaths = {"createdBy"})
    Page<Revision> findByCard_Id(final long id, final Pageable pageable);

    /**
     * Find all revisions for a deck
     *
     * @param id of the deck
     * @param pageable pagination parameters for the query
     * @return ordered list of all revisions of the deck
     */
    @EntityGraph(attributePaths = {"createdBy"})
    Page<Revision> findByCard_Deck_Id(final long id, final Pageable pageable);

    /**
     * Find revisions by ids
     *
     * @param ids of revisions
     * @return requested revisions
     */
    @EntityGraph(attributePaths = {"createdBy"})
    List<Revision> findByIdIn(final List<Long> ids);
}
