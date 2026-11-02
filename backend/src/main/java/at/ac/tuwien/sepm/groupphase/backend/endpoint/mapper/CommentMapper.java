package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CommentInquiryDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CommentSimpleDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Comment;
import org.mapstruct.Mapper;

@Mapper
public interface CommentMapper {

    CommentSimpleDto commentToCommentSimpleDto(final Comment comment);

    Comment commentInquiryDtoToComment(final CommentInquiryDto commentInquiryDto);
}
