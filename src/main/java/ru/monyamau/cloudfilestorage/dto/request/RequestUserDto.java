package ru.monyamau.cloudfilestorage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RequestUserDto(
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Size(min = 6, max = 40, message = "Имя пользователя должно содержать от 6 до 40 символов")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Имя пользователя может содержать только латинские буквы и цифры")
        String username,
        @NotBlank(message = "Пароль пользователя не может быть пустым")
        @Size(min = 8, max = 72, message = "Пароль должен содержать от 8 до 72 символов")
        @Pattern(regexp = "^[a-zA-Z0-9/@#$%*.!?&~_-]+$",
                message = "Пароль может содержать только латинские буквы, цифры " +
                        "и некоторые спецсимволы (/, @, #, $, %, *, ., !, ?, &, ~, _, -)")
        String password) {
}