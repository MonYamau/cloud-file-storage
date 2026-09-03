package ru.monyamau.cloudfilestorage.mapper;

import org.mapstruct.Mapper;
import ru.monyamau.cloudfilestorage.domain.ResourceItem;
import ru.monyamau.cloudfilestorage.domain.ResourceType;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;

@Mapper(componentModel = "spring")
public interface ResourceItemMapper {
    default ResponseResourceDto toDto(ResourceItem resourceItem) {
        String[] resources = resourceItem.objectName().split("/");
        String path = collectPathWithoutPersonalDirectory(resources);
        String name = resources[resources.length - 1];
        if (resourceItem.objectName().endsWith("/") || resourceItem.isDir()) {
            return new ResponseResourceDto(path, name, null, ResourceType.DIRECTORY);
        }
        return new ResponseResourceDto(path, name, resourceItem.size(), ResourceType.FILE);
    }

    private String collectPathWithoutPersonalDirectory(String[] resources) {
        StringBuilder path = new StringBuilder();
        for (int i = 1; i < resources.length - 1; i++) {
            path.append(resources[i]).append("/");
        }
        return path.toString();
    }
}
