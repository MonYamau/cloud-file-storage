package ru.monyamau.cloudfilestorage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.monyamau.cloudfilestorage.annotation.PatternResourceValidation;

@Schema(description = "Запрос на полное именование/путь ресурса в хранилище")
public record RequestResourceDto(
        @Schema(description = "Именование/путь ресурса в хранилище", example = "folder1/folder2/file.txt")
        @PatternResourceValidation
        String path) {
    public RequestResourceDto {
        if (path == null || path.isBlank()) {
            path = "/";
        }
    }
}