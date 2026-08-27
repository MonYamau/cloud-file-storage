package ru.monyamau.cloudfilestorage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.multipart.MultipartFile;

public record RequestUploadDto(
        @NotBlank(message = "Путь к директории загрузки не может отсутствовать или быть пустым")
        @Pattern(regexp = "^(?:[a-zA-Zа-яА-ЯёЁ0-9_.-]+/)+$",
                message = "Путь к директории может содержать только латинские и кириллические буквы, " +
                        "цифры и некоторые спецсимволы (/, _, ., -), а также должен оканчиваться спецсимволом (/)")
        String path,
        @NotNull(message = "Файл не может отсутствовать")
        MultipartFile file
        ) {
}