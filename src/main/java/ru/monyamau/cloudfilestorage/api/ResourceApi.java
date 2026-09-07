package ru.monyamau.cloudfilestorage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.monyamau.cloudfilestorage.api.annotation.AuthenticationErrorApiResponse;
import ru.monyamau.cloudfilestorage.api.annotation.NotFoundErrorApiResponse;
import ru.monyamau.cloudfilestorage.api.annotation.ServerErrorApiResponse;
import ru.monyamau.cloudfilestorage.api.annotation.ValidationErrorApiResponse;
import ru.monyamau.cloudfilestorage.dto.request.RequestMovementDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestQueryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestResourceDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestUploadDto;
import ru.monyamau.cloudfilestorage.dto.response.ErrorDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;

import java.util.List;

@RequestMapping("/resource")
@Tag(name = "Ресурсы", description = "Управление ресурсами пользователя (файлы и директории)")
public interface ResourceApi {
    @GetMapping
    @Operation(summary = "Получить информацию о ресурсе")
    @ApiResponse(responseCode = "200", description = "Успешный запрос")
    @ValidationErrorApiResponse
    @AuthenticationErrorApiResponse
    @NotFoundErrorApiResponse
    @ServerErrorApiResponse
    ResponseEntity<ResponseResourceDto> showAbout(@Valid @ModelAttribute(name = "path") RequestResourceDto requestDto);

    @DeleteMapping
    @Operation(summary = "Удалить ресурс")
    @ApiResponse(responseCode = "204", description = "Успешное удаление ресурса",
            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @ValidationErrorApiResponse
    @AuthenticationErrorApiResponse
    @NotFoundErrorApiResponse
    @ServerErrorApiResponse
    ResponseEntity<Void> delete(@Valid @ModelAttribute(name = "path") RequestResourceDto requestDto);

    @GetMapping("/download")
    @Operation(summary = "Скачать ресурс из хранилища")
    @ApiResponse(responseCode = "200", description = "Успешная загрузка ресурса",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
    @ValidationErrorApiResponse
    @AuthenticationErrorApiResponse
    @NotFoundErrorApiResponse
    @ServerErrorApiResponse
    ResponseEntity<byte[]> download(@Valid @ModelAttribute(name = "path") RequestResourceDto requestDto);

    @PostMapping("/move")
    @Operation(summary = "Переместить/переименовать ресурс")
    @ApiResponse(responseCode = "200", description = "Успешное изменение ресурса")
    @ValidationErrorApiResponse
    @AuthenticationErrorApiResponse
    @NotFoundErrorApiResponse
    @ApiResponse(responseCode = "409", description = "Ресурс по конечному пути уже существует",
            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @ServerErrorApiResponse
    ResponseEntity<ResponseResourceDto> change(@Valid @ModelAttribute RequestMovementDto requestDto);

    @GetMapping("/search")
    @Operation(summary = "Найти ресурсы по их имени")
    @ApiResponse(responseCode = "200", description = "Успешный поиск ресурсов")
    @ValidationErrorApiResponse
    @AuthenticationErrorApiResponse
    @ServerErrorApiResponse
    ResponseEntity<List<ResponseResourceDto>> search(@Valid @ModelAttribute(name = "query") RequestQueryDto requestDto);

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Сохранить ресурс в хранилище")
    @ApiResponse(responseCode = "201", description = "Успешное сохранение ресурса")
    @ValidationErrorApiResponse
    @AuthenticationErrorApiResponse
    @NotFoundErrorApiResponse
    @ApiResponse(responseCode = "409", description = "Ресурс уже существует",
            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @ServerErrorApiResponse
    ResponseEntity<List<ResponseResourceDto>> upload(@Valid @ModelAttribute RequestUploadDto requestDto);
}
