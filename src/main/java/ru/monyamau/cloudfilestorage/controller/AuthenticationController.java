package ru.monyamau.cloudfilestorage.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.monyamau.cloudfilestorage.api.AuthenticationApi;
import ru.monyamau.cloudfilestorage.dto.request.RequestUserDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;
import ru.monyamau.cloudfilestorage.exception.AuthenticationException;
import ru.monyamau.cloudfilestorage.service.AuthenticationService;
import ru.monyamau.cloudfilestorage.util.CookieUtil;

import java.util.UUID;

@RestController
public class AuthenticationController implements AuthenticationApi {
    private final static int TTL_MINUTES = 30;

    private final AuthenticationService authService;

    @Autowired
    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<ResponseUserDto> signUp(RequestUserDto requestDto) {
        String sessionId = String.valueOf(UUID.randomUUID());
        ResponseUserDto userDto = authService.registerUser(sessionId, requestDto, TTL_MINUTES);
        ResponseCookie cookie = CookieUtil.create(sessionId, TTL_MINUTES);
        return ResponseEntity
                .status(HttpStatus.CREATED).
                header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userDto);
    }

    @Override
    public ResponseEntity<ResponseUserDto> signIn(RequestUserDto requestDto) {
        String sessionId = String.valueOf(UUID.randomUUID());
        ResponseUserDto userDto = authService.authenticateUser(sessionId, requestDto, TTL_MINUTES);
        ResponseCookie cookie = CookieUtil.create(sessionId, TTL_MINUTES);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userDto);
    }

    @Override
    public ResponseEntity<Void> signOut(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Cookie cookie = CookieUtil.findSessionId(cookies)
                .orElseThrow(() -> new AuthenticationException("Ошибка аутентификации: не выполнен вход пользователем"));
        authService.logoutUser(cookie.getValue());
        ResponseCookie deletedCookie = CookieUtil.delete();
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, deletedCookie.toString())
                .build();
    }
}
