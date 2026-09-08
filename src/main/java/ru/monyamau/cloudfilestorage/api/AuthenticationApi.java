package ru.monyamau.cloudfilestorage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.monyamau.cloudfilestorage.api.annotation.AuthenticationErrorApiResponse;
import ru.monyamau.cloudfilestorage.api.annotation.ServerErrorApiResponse;
import ru.monyamau.cloudfilestorage.api.annotation.ValidationErrorApiResponse;
import ru.monyamau.cloudfilestorage.dto.request.RequestUserDto;
import ru.monyamau.cloudfilestorage.dto.response.ErrorDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;

@RequestMapping("/auth")
@Tag(name = "Авторизация", description = "Управление доступом пользователя")
public interface AuthenticationApi {
    @PostMapping("/sign-up")
    @Operation(summary = "Зарегистрировать пользователя")
    @ApiResponse(responseCode = "201", description = "Успешная регистрация")
    @ValidationErrorApiResponse
    @ApiResponse(responseCode = "409", description = "Пользователь с данным именем уже существует",
            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @ServerErrorApiResponse
    @SecurityRequirements
    ResponseEntity<ResponseUserDto> signUp(@Valid @RequestBody RequestUserDto requestDto);

    @PostMapping("/sign-in")
    @Operation(summary = "Авторизовать пользователя")
    @ApiResponse(responseCode = "200", description = "Успешная авторизация")
    @ValidationErrorApiResponse
    @ApiResponse(responseCode = "401", description = "Неверное имя пользователя или пароль",
            content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    @ServerErrorApiResponse
    @SecurityRequirements
    ResponseEntity<ResponseUserDto> signIn(@Valid @RequestBody RequestUserDto requestDto);

    @PostMapping("/sign-out")
    @Operation(summary = "Деавторизовать пользователя")
    @ApiResponse(responseCode = "204", description = "Успешная деавторизация")
    @AuthenticationErrorApiResponse
    @ServerErrorApiResponse
    ResponseEntity<Void> signOut(HttpServletRequest request);
}
