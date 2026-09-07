package ru.monyamau.cloudfilestorage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Запрос на полное именование/путь ресурса в хранилище")
public record RequestResourceDto(
        @Schema(description = "Именование/путь ресурса в хранилище", example = "folder1/folder2/file.txt")
        @NotBlank(message = "Именование/путь ресурса не может отсутствовать или быть пустым")
        @Pattern(regexp = "^(?:/|(?:[a-zA-Zа-яА-ЯёЁ0-9_.-]+/)*[a-zA-Zа-яА-ЯёЁ0-9_.-]*)$",
                message = "Путь к ресурсу может содержать только латинские и кириллические буквы, " +
                        "цифры и некоторые спецсимволы (/, _, ., -)")
        String path) {
}