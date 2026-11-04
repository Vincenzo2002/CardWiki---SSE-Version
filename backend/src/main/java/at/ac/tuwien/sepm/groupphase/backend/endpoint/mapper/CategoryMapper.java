package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CategoryDetailedDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CategoryInputDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CategorySimpleDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Category;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper
public interface CategoryMapper {
    final CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Named("simpleCategory")
    CategorySimpleDto categoryToCategorySimpleDto(final Category category);

    @IterableMapping(qualifiedByName = "simpleCategory")
    List<CategorySimpleDto> categoryToCategorySimpleDto(final List<Category> categories);

    Category categorySimpleDtoToCategory(final CategorySimpleDto categorySimpleDto);

    @Mapping(source = "category.createdBy.id", target = "createdBy")
    @Mapping(source = "children", target = "children", qualifiedByName = "childrenToSimpleChildren")
    @Mapping(source = "parent", target = "parent", qualifiedByName="parentToSimpleParent")
    CategoryDetailedDto categoryToCategoryDetailedDto(final Category category);

    @Named("childrenToSimpleChildren")
    static List<CategorySimpleDto> childrenToSimpleChildren(final Set<Category> children) {
        final List<CategorySimpleDto> result = new ArrayList<>(children.size());

        for (final Category child : children) {
            result.add(INSTANCE.categoryToCategorySimpleDto(child));
        }

        return result;
    }

    @Named("parentToSimpleParent")
    static CategorySimpleDto parentToSimpleParent(final Category parent) {
        return INSTANCE.categoryToCategorySimpleDto(parent);
    }

     Category categoryInputDtoToCategory(final CategoryInputDto categoryInputDto);
}
