package ru.monyamau.cloudfilestorage.dto.response;

import ru.monyamau.cloudfilestorage.model.ResourceType;

public record ResponseResourceDto(String path, String name, Long size, ResourceType type) {
}
