package ru.monyamau.cloudfilestorage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RequestMovementDto(
        @NotBlank(message = "Актуальный путь к ресурсу не может отсутствовать или быть пустым")
        @Pattern(regexp = "^(?:[a-zA-Zа-яА-ЯёЁ0-9_.-]+/)*[a-zA-Zа-яА-ЯёЁ0-9_.-]*$",
                message = "Путь к ресурсу может содержать только латинские и кириллические буквы, " +
                        "цифры и некоторые спецсимволы (/, _, ., -)")
        String from,
        @NotBlank(message = "Новый путь к ресурсу не может отсутствовать или быть пустым")
        @Pattern(regexp = "^(?:[a-zA-Zа-яА-ЯёЁ0-9_.-]+/)*[a-zA-Zа-яА-ЯёЁ0-9_.-]*$",
                message = "Путь к ресурсу может содержать только латинские и кириллические буквы, " +
                        "цифры и некоторые спецсимволы (/, _, ., -)")
        String to) {
}
