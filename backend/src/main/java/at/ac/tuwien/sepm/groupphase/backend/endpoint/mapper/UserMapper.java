package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.entity.Progress;
import at.ac.tuwien.sepm.groupphase.backend.entity.Revision;
import at.ac.tuwien.sepm.groupphase.backend.entity.RevisionEdit;
import at.ac.tuwien.sepm.groupphase.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.InheritConfiguration;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Mapper(uses = ImageMapper.class)
public interface UserMapper {
    User userInputDtoToUser(final UserInputDto userInputDto);
    UserDetailsDto userToUserDetailsDto(final User user);
    User userUpdateDtoToUser(final UserUpdateDto userUpdateDto);

    @Mapping(source = "user", target = "user")
    @Mapping(source = "revisions", target = "revisions", qualifiedByName = "revisionsToRevisionExportDtoList")
    UserExportDto userToUserExportDto(final User user);

    @Mapping(source = "progress.id.reverse", target = "reverse")
    @Mapping(source = "progress.id.card.id", target = "cardId")
    @Mapping(source = "progress.id.card.latestRevision", target = "latestRevision", qualifiedByName = "revisionToRevisionExportDto")
    ProgressExportDto progressToProgressExportDto(final Progress progress);

    @Mapping(source = "revision.card.id", target = "cardId")
    @Mapping(source = "revision.card.deck", target = "deck")
    RevisionExportDto _revisionToRevisionExportDto(final Revision revision);

    @InheritConfiguration(name = "_revisionToRevisionExportDto")
    RevisionExportDto _revisionEditToRevisionExportDto(final RevisionEdit revision);

    @Named("revisionToRevisionExportDto")
    // Using Object to prevent ambigous method error
    default RevisionExportDto _anyRevisionToRevisionExportDto(final Object revision) {
        if (revision == null) {
            return null;
        }

        if (revision instanceof RevisionEdit) {
            return _revisionEditToRevisionExportDto((RevisionEdit) revision);
        } else if (revision instanceof Revision) {
            return _revisionToRevisionExportDto((Revision) revision);
        } else {
            throw new RuntimeException("_revisionToRevisionExportDto may only be called with revisions");
        }
    }

    @Named("revisionsToRevisionExportDtoList")
    default List<RevisionExportDto> revisionSetToRevisionExportDtoList(final Set<Revision> set) {
        if (set == null) {
            return null;
        }

        final List<RevisionExportDto> list = new ArrayList<RevisionExportDto>(set.size());
        for (final Revision revision : set) {
            list.add(_anyRevisionToRevisionExportDto(revision));
        }

        return list;
    }
}
