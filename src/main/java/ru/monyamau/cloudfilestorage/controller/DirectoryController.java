package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;

@RestController
@RequestMapping("/directory")
public class DirectoryController {
    @GetMapping
    public ResponseEntity<ResponseResourceDto> showAbout(@RequestParam(name="path") String path) {
        return new ResponseEntity<>(new ResponseResourceDto("", "", null, ""), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseResourceDto> create(@RequestParam(name="path") String path) {
        return new ResponseEntity<>(new ResponseResourceDto("", "", null, ""), HttpStatus.CREATED);
    }
}
