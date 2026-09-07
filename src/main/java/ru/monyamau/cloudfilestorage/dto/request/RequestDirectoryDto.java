package ru.monyamau.cloudfilestorage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Полный путь к ресурсу в хранилище")
public record RequestDirectoryDto(
        @Schema(description = "Путь директории в хранилище", example = "any_folder/документы/")
        @NotBlank(message = "Путь директории не может отсутствовать или быть пустым")
        @Pattern(regexp = "^(?:/|(?:[a-zA-Zа-яА-ЯёЁ0-9_.-]+/)+)$",
                message = "Путь директории может содержать только латинские и кириллические буквы, " +
                        "цифры и некоторые спецсимволы (/, _, ., -), а также должен оканчиваться спецсимволом (/)")
        String path
) {
}