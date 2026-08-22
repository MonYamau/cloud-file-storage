package ru.monyamau.cloudfilestorage.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;
import ru.monyamau.cloudfilestorage.service.AuthorizationService;
import ru.monyamau.cloudfilestorage.util.CookieUtil;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final AuthorizationService authorizationService;

    @Autowired
    public UserController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseUserDto> showCurrentUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Cookie cookie = CookieUtil.findSessionId(cookies).orElseThrow(RuntimeException::new);
        ResponseUserDto userDto = authorizationService.findUser(UUID.fromString(cookie.getValue()));
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }
}
