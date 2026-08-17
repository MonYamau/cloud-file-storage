package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.service.ResourceService;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
public class DirectoryController {
    private final ResourceService resourceService;

    public DirectoryController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<List<ResponseResourceDto>> showAbout(@RequestParam(name = "path") String path, @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        List<ResponseResourceDto> resourceDtoList = resourceService.findAllFromDirectory(personalDirectory + path);
        return new ResponseEntity<>(resourceDtoList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseResourceDto> create(@RequestParam(name = "path") String path, @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        ResponseResourceDto resourceDto = resourceService.createDirectory(personalDirectory + path);
        return new ResponseEntity<>(resourceDto, HttpStatus.CREATED);
    }
}
