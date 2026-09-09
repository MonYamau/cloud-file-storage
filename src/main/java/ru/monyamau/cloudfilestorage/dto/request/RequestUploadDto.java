package ru.monyamau.cloudfilestorage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.annotation.PatternDirectoryValidation;

import java.util.List;

@Schema(description = "Запрос на сохранение ресурса в хранилище")
public record RequestUploadDto(
        @Schema(description = "Путь директории загрузки в хранилище", example = "any_folder/документы/")
        @PatternDirectoryValidation
        String path,
        @Schema(description = "Список файлов для загрузки в хранилище")
        @NotNull(message = "Список файлов не может отсутствовать")
        @NotEmpty(message = "Список файлов не может быть пустым")
        List<MultipartFile> object) {
    public RequestUploadDto {
        if (path == null || path.isBlank()) {
            path = "/";
        }
    }
}