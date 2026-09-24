package ru.monyamau.cloudfilestorage.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.monyamau.cloudfilestorage.domain.ResourceType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Schema(description = "Ответ с данными о ресурсе")
public record ResponseResourceDto(
        @Schema(description = "Полный путь к ресурсу", example = "folder1/folder2/")
        String path,
        @Schema(description = "Наименование ресурса", example = "file.txt")
        String name,
        @Schema(description = "Размер ресурса в байтах", example = "1234")
        @JsonInclude(NON_NULL)
        Long size,
        @Schema(description = "Тип ресурса: DIRECTORY/FILE", example = "FILE")
        ResourceType type) {
}