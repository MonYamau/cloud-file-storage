package ru.monyamau.cloudfilestorage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "Запрос на сохранение ресурса в хранилище")
public record RequestUploadDto(
        @Schema(description = "Путь директории загрузки в хранилище", example = "any_folder/документы/")
        @NotBlank(message = "Путь директории загрузки не может отсутствовать или быть пустым")
        @Pattern(regexp = "^(?:[a-zA-Zа-яА-ЯёЁ0-9_.-]+/)+$",
                message = "Путь директории может содержать только латинские и кириллические буквы, " +
                        "цифры и некоторые спецсимволы (/, _, ., -), а также должен оканчиваться спецсимволом (/)")
        String path,
        @Schema(description = "Список файлов для загрузки в хранилище")
        @NotNull(message = "Список файлов не может отсутствовать")
        @NotEmpty(message = "Список файлов не может быть пустым")
        List<MultipartFile> files
) {
}