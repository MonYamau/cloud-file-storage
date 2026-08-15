package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.model.ResourceType;

@RestController
@RequestMapping("/api/directory")
public class DirectoryController {
    @GetMapping
    public ResponseEntity<ResponseResourceDto> showAbout(@RequestParam(name = "path") String path) {
        return new ResponseEntity<>(new ResponseResourceDto("", "", null, ResourceType.DIRECTORY), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseResourceDto> create(@RequestParam(name = "path") String path) {
        return new ResponseEntity<>(new ResponseResourceDto("", "", null, ResourceType.DIRECTORY), HttpStatus.CREATED);
    }
}
