package ru.monyamau.cloudfilestorage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.monyamau.cloudfilestorage.dto.request.RequestMovementDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestQueryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestResourceDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestUploadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;

import java.util.List;

@RequestMapping("/resource")
@Tag(name = "Ресурсы", description = "Управление ресурсами пользователя (файлы и директории)")
public interface ResourceApi {
    @GetMapping
    @Operation(summary = "Получить информацию о ресурсе")
    @ApiResponse(responseCode = "200", description = "Успешный запрос")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров", content = @Content)
    @ApiResponse(responseCode = "401", description = "Пользователь неавторизован", content = @Content)
    @ApiResponse(responseCode = "404", description = "Ресурс не найден", content = @Content)
    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера", content = @Content)
    ResponseEntity<ResponseResourceDto> showAbout(@Valid @ModelAttribute(name = "path") RequestResourceDto requestDto);

    @DeleteMapping
    @Operation(summary = "Удалить ресурс")
    @ApiResponse(responseCode = "204", description = "Успешное удаление ресурса", content = @Content)
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров", content = @Content)
    @ApiResponse(responseCode = "401", description = "Пользователь неавторизован", content = @Content)
    @ApiResponse(responseCode = "404", description = "Ресурс не найден", content = @Content)
    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера", content = @Content)
    ResponseEntity<HttpStatus> delete(@Valid @ModelAttribute(name = "path") RequestResourceDto requestDto);

    @GetMapping("/download")
    @Operation(summary = "Скачать ресурс из хранилища")
    @ApiResponse(responseCode = "200", description = "Успешная загрузка ресурса")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров", content = @Content)
    @ApiResponse(responseCode = "401", description = "Пользователь неавторизован", content = @Content)
    @ApiResponse(responseCode = "404", description = "Ресурс не найден", content = @Content)
    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера", content = @Content)
    ResponseEntity<byte[]> download(@Valid @ModelAttribute(name = "path") RequestResourceDto requestDto);

    @PostMapping("/move")
    @Operation(summary = "Переместить/переименовать ресурс")
    @ApiResponse(responseCode = "200", description = "Успешное изменение ресурса")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров", content = @Content)
    @ApiResponse(responseCode = "401", description = "Пользователь неавторизован", content = @Content)
    @ApiResponse(responseCode = "404", description = "Ресурс не найден", content = @Content)
    @ApiResponse(responseCode = "409", description = "Ресурс по конечному пути уже существует", content = @Content)
    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера", content = @Content)
    ResponseEntity<ResponseResourceDto> change(@Valid @ModelAttribute RequestMovementDto requestDto);

    @GetMapping("/search")
    @Operation(summary = "Найти ресурсы по их имени")
    @ApiResponse(responseCode = "200", description = "Успешный поиск ресурсов")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров", content = @Content)
    @ApiResponse(responseCode = "401", description = "Пользователь неавторизован", content = @Content)
    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера", content = @Content)
    ResponseEntity<List<ResponseResourceDto>> search(@Valid @ModelAttribute(name = "query") RequestQueryDto requestDto);

    @PostMapping
    @Operation(summary = "Сохранить ресурс в хранилище")
    @ApiResponse(responseCode = "201", description = "Успешное сохранение ресурса")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров", content = @Content)
    @ApiResponse(responseCode = "401", description = "Пользователь неавторизован", content = @Content)
    @ApiResponse(responseCode = "404", description = "Ресурс не найден", content = @Content)
    @ApiResponse(responseCode = "409", description = "Ресурс уже существует", content = @Content)
    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера", content = @Content)
    ResponseEntity<List<ResponseResourceDto>> upload(@Valid @ModelAttribute RequestUploadDto requestDto);
}
