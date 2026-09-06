package ru.monyamau.cloudfilestorage.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.monyamau.cloudfilestorage.api.UserApi;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;
import ru.monyamau.cloudfilestorage.exception.AuthenticationException;
import ru.monyamau.cloudfilestorage.service.AuthorizationService;
import ru.monyamau.cloudfilestorage.util.CookieUtil;

import java.util.UUID;

@RestController
public class UserController implements UserApi {
    private final AuthorizationService authorizationService;

    @Autowired
    public UserController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public ResponseEntity<ResponseUserDto> showCurrentUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Cookie cookie = CookieUtil.findSessionId(cookies)
                .orElseThrow(() -> new AuthenticationException("Ошибка аутентификации: не выполнен вход пользователем"));
        ResponseUserDto responseDto = authorizationService.findUser(UUID.fromString(cookie.getValue()));
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
