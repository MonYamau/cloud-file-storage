package ru.monyamau.cloudfilestorage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.monyamau.cloudfilestorage.dto.request.RequestUserDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;

@RequestMapping("/auth")
@Tag(name = "Авторизация", description = "Управление доступом пользователя")
public interface AuthorizationApi {
    @PostMapping("/sign-up")
    @Operation(summary = "Зарегистрировать пользователя")
    @ApiResponse(responseCode = "201", description = "Успешная регистрация")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров", content = @Content)
    @ApiResponse(responseCode = "409", description = "Пользователь с данным именем уже существует", content = @Content)
    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера", content = @Content)
    ResponseEntity<ResponseUserDto> signUp(@Valid @RequestBody RequestUserDto requestDto);

    @PostMapping("/sign-in")
    @Operation(summary = "Авторизовать пользователя")
    @ApiResponse(responseCode = "200", description = "Успешная авторизация")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров", content = @Content)
    @ApiResponse(responseCode = "401", description = "Неверное имя пользователя или пароль", content = @Content)
    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера", content = @Content)
    ResponseEntity<ResponseUserDto> signIn(@Valid @RequestBody RequestUserDto requestDto);

    @PostMapping("/sign-out")
    @Operation(summary = "Деавторизовать пользователя")
    @ApiResponse(responseCode = "204", description = "Успешная деавторизация")
    @ApiResponse(responseCode = "401", description = "Пользователь неавторизован", content = @Content)
    @ApiResponse(responseCode = "500", description = "Ошибка на стороне сервера", content = @Content)
    ResponseEntity<Void> signOut(HttpServletRequest request);
}
