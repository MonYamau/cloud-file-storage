package ru.monyamau.cloudfilestorage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Запрос на переименование/перемещение ресурса")
public record RequestMovementDto(
        @Schema(description = "Именование/путь ресурса до изменения", example = "a/b/с/image.png")
        @NotBlank(message = "Актуальный путь/имя ресурса не может отсутствовать или быть пустым")
        @Pattern(regexp = "^(?:[a-zA-Zа-яА-ЯёЁ0-9_.-]+/)*[a-zA-Zа-яА-ЯёЁ0-9_.-]*$",
                message = "Путь к ресурсу может содержать только латинские и кириллические буквы, " +
                        "цифры и некоторые спецсимволы (/, _, ., -)")
        String from,
        @Schema(description = "Именование/путь ресурса после изменения", example = "a/b/с/changed_image.png")
        @NotBlank(message = "Новый путь/имя ресурса не может отсутствовать или быть пустым")
        @Pattern(regexp = "^(?:[a-zA-Zа-яА-ЯёЁ0-9_.-]+/)*[a-zA-Zа-яА-ЯёЁ0-9_.-]*$",
                message = "Путь к ресурсу может содержать только латинские и кириллические буквы, " +
                        "цифры и некоторые спецсимволы (/, _, ., -)")
        String to) {
}
