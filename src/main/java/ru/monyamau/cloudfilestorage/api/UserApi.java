package ru.monyamau.cloudfilestorage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.monyamau.cloudfilestorage.annotation.AuthenticationErrorApiResponse;
import ru.monyamau.cloudfilestorage.annotation.ServerErrorApiResponse;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;

@RequestMapping("/user")
@Tag(name = "Пользователи", description = "Управление пользовательской информацией")
public interface UserApi {
    @GetMapping("/me")
    @Operation(summary = "Получить текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Успешный запрос")
    @AuthenticationErrorApiResponse
    @ServerErrorApiResponse
    ResponseEntity<ResponseUserDto> showCurrentUser(HttpServletRequest request);
}
