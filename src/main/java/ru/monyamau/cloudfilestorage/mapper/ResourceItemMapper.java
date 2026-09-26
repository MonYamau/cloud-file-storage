package ru.monyamau.cloudfilestorage.mapper;

import org.springframework.stereotype.Component;
import ru.monyamau.cloudfilestorage.domain.ResourceItem;
import ru.monyamau.cloudfilestorage.domain.ResourcePath;
import ru.monyamau.cloudfilestorage.domain.ResourceType;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;

@Component
public class ResourceItemMapper {
    public ResponseResourceDto toDto(ResourceItem resourceItem, String personalDirectory) {
        ResourcePath resourcePath = ResourcePath.ofObjectKey(personalDirectory, resourceItem.objectName());
        String path = resourcePath.getParentDirectoryWithoutPersonalDirectory();
        String name = resourcePath.getResourceName();
        ResourceType type = (resourceItem.isDir() || resourcePath.isDirectory())
                ? ResourceType.DIRECTORY
                : ResourceType.FILE;
        return new ResponseResourceDto(path, name, resourceItem.size(), type);
    }
}
