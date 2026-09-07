package ru.monyamau.cloudfilestorage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.monyamau.cloudfilestorage.domain.ResourceType;

@Schema(description = "Ответ с данными о ресурсе")
public record ResponseResourceDto(
        @Schema(description = "Полный путь к ресурсу", example = "folder1/folder2/")
        String path,
        @Schema(description = "Наименование ресурса", example = "file.txt")
        String name,
        @Schema(description = "Размер ресурса в байтах", example = "1234")
        Long size,
        @Schema(description = "Тип ресурса: DIRECTORY/FILE", example = "FILE")
        ResourceType type) {
}
