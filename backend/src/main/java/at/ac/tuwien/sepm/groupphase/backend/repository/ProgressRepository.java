package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Progress;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProgressRepository extends JpaRepository<Progress,Progress.Id> {

    void deleteById_UserId(final Long userId);

    void deleteById_CardId(final Long cardId);

    void deleteById_UserIdAndId_Card_Deck_IdAndId_Reverse(final Long userId, final Long deckId, final boolean reverse);

    boolean existsById_CardId(final Long cardId);

    /**
     * Find all progress entries of user for exporting data
     *
     * @param userId id of the user
     * @return all progress entries by user
     */
    @EntityGraph(attributePaths = {"id.card.latestRevision.card.deck"})
    Set<Progress> findExportById_UserId(final Long userId);
}
