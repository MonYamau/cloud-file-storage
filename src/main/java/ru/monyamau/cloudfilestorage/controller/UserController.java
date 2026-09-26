package ru.monyamau.cloudfilestorage.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.monyamau.cloudfilestorage.api.UserApi;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;
import ru.monyamau.cloudfilestorage.exception.AuthenticationException;
import ru.monyamau.cloudfilestorage.handler.UserContext;
import ru.monyamau.cloudfilestorage.service.AuthenticationService;
import ru.monyamau.cloudfilestorage.util.CookieUtil;

@RestController
public class UserController implements UserApi {
    private final AuthenticationService authenticationService;
    private final UserContext userContext;

    @Autowired
    public UserController(AuthenticationService authenticationService, UserContext userContext) {
        this.authenticationService = authenticationService;
        this.userContext = userContext;
    }

    @Override
    public ResponseEntity<ResponseUserDto> showCurrentUser() {
        ResponseUserDto responseDto = authenticationService.findUser(userContext.getUserId());
        return ResponseEntity.ok(responseDto);
    }
}
