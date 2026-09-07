package ru.monyamau.cloudfilestorage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на поиск ресурса по имени")
public record RequestQueryDto(
        @Schema(description = "Строка, которая ожидается в имени ресурса", example = "summer_2016")
        @NotBlank(message = "Запрос не может отсутствовать или быть пустым")
        String query
) {
}