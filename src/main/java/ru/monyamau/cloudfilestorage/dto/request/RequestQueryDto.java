package ru.monyamau.cloudfilestorage.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RequestQueryDto(
        @NotBlank(message = "Запрос не может отсутствовать или быть пустым")
        String query
) {
}