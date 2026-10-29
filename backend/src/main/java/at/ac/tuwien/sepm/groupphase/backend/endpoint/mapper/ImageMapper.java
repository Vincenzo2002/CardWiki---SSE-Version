package at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.ImageDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Image;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Mapper
public abstract class ImageMapper {

    @Value("${cawi.image-served-path}")
    String imageServedPath;

    private final ConcurrentMap<String, String> urlPrefixes =
        new ConcurrentHashMap<>();

    public abstract ImageDto imageToImageDto(Image image);

    @AfterMapping
    protected void filenameToUrl(@MappingTarget ImageDto imageDto) {
        String filename = imageDto.getFilename();

        if (File.separatorChar == '/' && imageServedPath != null
            && isSimpleFilename(filename)) {

            String prefix = urlPrefixes.computeIfAbsent(
                imageServedPath, ImageMapper::buildUrlPrefix);

            imageDto.setUrl(prefix + filename);
        } else {
            imageDto.setUrl(
                Paths.get(imageServedPath, filename)
                    .toString()
                    .replace('\\', '/')
            );
        }
    }

    private static String buildUrlPrefix(String configuredPath) {
        String prefix = Paths.get(configuredPath).toString();

        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix += "/";
        }

        return prefix.replace('\\', '/');
    }

    private static boolean isSimpleFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        for (int i = 0; i < filename.length(); i++) {
            char character = filename.charAt(i);

            if (character == '/' || character == '\\'
                || character == '\0' || character > 127) {
                return false;
            }
        }

        return true;
    }
}
