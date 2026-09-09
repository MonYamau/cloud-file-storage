package ru.monyamau.cloudfilestorage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.monyamau.cloudfilestorage.annotation.PatternDirectoryValidation;

@Schema(description = "Полный путь к ресурсу в хранилище")
public record RequestDirectoryDto(
        @Schema(description = "Путь директории в хранилище", example = "any_folder/документы/")
        @PatternDirectoryValidation
        String path) {
    public RequestDirectoryDto {
        if (path == null || path.isBlank()) {
            path = "/";
        }
    }
}