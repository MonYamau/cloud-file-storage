package ru.monyamau.cloudfilestorage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.monyamau.cloudfilestorage.api.annotation.AuthenticationErrorApiResponse;
import ru.monyamau.cloudfilestorage.api.annotation.ServerErrorApiResponse;
import ru.monyamau.cloudfilestorage.api.annotation.ValidationErrorApiResponse;
import ru.monyamau.cloudfilestorage.dto.request.RequestDirectoryDto;
import ru.monyamau.cloudfilestorage.dto.response.ErrorDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;

import java.util.List;

@RequestMapping("/directory")
@Tag(name = "Директории", description = "Управление директориями пользователя")
public interface DirectoryApi {
    @GetMapping
    @Operation(summary = "Получить информацию о ресурсах из директории")
    @ApiResponse(responseCode = "200", description = "Успешный запрос")
    @ValidationErrorApiResponse
    @AuthenticationErrorApiResponse
    @ApiResponse(responseCode = "404", description = "Директория не найдена",
            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @ServerErrorApiResponse
    ResponseEntity<List<ResponseResourceDto>> showAbout(@Valid @ModelAttribute(name = "path") RequestDirectoryDto requestDto);

    @PostMapping
    @Operation(summary = "Создать директорию")
    @ApiResponse(responseCode = "201", description = "Успешное создание директории")
    @ValidationErrorApiResponse
    @AuthenticationErrorApiResponse
    @ApiResponse(responseCode = "404", description = "Родительская директория не найдена",
            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @ApiResponse(responseCode = "409", description = "Создаваемая директория уже существует",
            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @ServerErrorApiResponse
    ResponseEntity<ResponseResourceDto> create(@Valid @ModelAttribute(name = "path") RequestDirectoryDto requestDto);
}
