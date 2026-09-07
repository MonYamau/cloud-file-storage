package ru.monyamau.cloudfilestorage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с учётными данными пользователя")
public record ResponseUserDto(
        @Schema(description = "Имя пользователя", example = "SuperUser123")
        String username) {
}
