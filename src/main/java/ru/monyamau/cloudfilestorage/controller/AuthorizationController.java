package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.monyamau.cloudfilestorage.dto.request.RequestUserDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;

@RestController
@RequestMapping("/auth")
public class AuthorizationController {
    @PostMapping("/sign-up")
    public ResponseEntity<ResponseUserDto> signUp(@RequestBody RequestUserDto requestDto) {
        return new ResponseEntity<>(new ResponseUserDto(""), HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ResponseUserDto> signIn(@RequestBody RequestUserDto requestDto) {
        return new ResponseEntity<>(new ResponseUserDto(""), HttpStatus.OK);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<HttpStatus> signOut() {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
