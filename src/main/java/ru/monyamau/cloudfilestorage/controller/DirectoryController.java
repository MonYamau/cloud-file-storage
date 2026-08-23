package ru.monyamau.cloudfilestorage.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.monyamau.cloudfilestorage.dto.request.RequestDirectoryPathDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.service.ResourceService;

import java.util.List;

@RestController
@RequestMapping("/directory")
public class DirectoryController {
    private final ResourceService resourceService;

    public DirectoryController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<List<ResponseResourceDto>> showAbout(@Valid @ModelAttribute(name = "path") RequestDirectoryPathDto pathDto) {
        List<ResponseResourceDto> resourceDtoList = resourceService.findAllFromDirectory(pathDto.path());
        return new ResponseEntity<>(resourceDtoList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseResourceDto> create(@Valid @ModelAttribute(name = "path") RequestDirectoryPathDto pathDto) {
        ResponseResourceDto resourceDto = resourceService.createDirectory(pathDto.path());
        return new ResponseEntity<>(resourceDto, HttpStatus.CREATED);
    }
}
