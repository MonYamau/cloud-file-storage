package ru.monyamau.cloudfilestorage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.monyamau.cloudfilestorage.annotation.PatternDirectoryValidation;

@Schema(description = "Полный путь к ресурсу в хранилище")
public record RequestDirectoryDto(
        @Schema(description = "Путь директории в хранилище", example = "any_folder/документы/")
        @PatternDirectoryValidation
        @NotNull(message = "Путь к ресурсу не может отсутствовать")
        String path) {
}