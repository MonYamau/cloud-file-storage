package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @GetMapping("/me")
    public ResponseEntity<ResponseUserDto> showCurrentUser() {
        return new ResponseEntity<>(new ResponseUserDto(""), HttpStatus.OK);
    }
}
