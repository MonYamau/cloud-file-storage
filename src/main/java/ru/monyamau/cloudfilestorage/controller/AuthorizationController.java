package ru.monyamau.cloudfilestorage.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.monyamau.cloudfilestorage.dto.request.RequestUserDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;
import ru.monyamau.cloudfilestorage.service.AuthorizationService;
import ru.monyamau.cloudfilestorage.util.CookieUtil;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthorizationController {
    private final static int TTL_MINUTES = 30;
    private final AuthorizationService authService;

    @Autowired
    public AuthorizationController(AuthorizationService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<ResponseUserDto> signUp(@RequestBody RequestUserDto requestDto) {
        UUID uuid = UUID.randomUUID();
        String username = authService.registerUser(uuid, requestDto, TTL_MINUTES);
        ResponseCookie cookie = CookieUtil.create(String.valueOf(uuid));
        return ResponseEntity
                .status(HttpStatus.CREATED).
                header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ResponseUserDto(username));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ResponseUserDto> signIn(@RequestBody RequestUserDto requestDto) {
        UUID uuid = UUID.randomUUID();
        String username = authService.authorizeUser(uuid, requestDto, TTL_MINUTES);
        ResponseCookie cookie = CookieUtil.create(String.valueOf(uuid));
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ResponseUserDto(username));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<HttpStatus> signOut(HttpServletRequest request) {
        Cookie cookie = CookieUtil.findSessionId(request);
        authService.logoutUser(cookie.getValue());
        ResponseCookie deletedCookie = CookieUtil.delete();
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, deletedCookie.toString())
                .build();
    }
}
