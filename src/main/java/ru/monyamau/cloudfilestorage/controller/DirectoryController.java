package ru.monyamau.cloudfilestorage.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.monyamau.cloudfilestorage.dto.request.RequestDirectoryDto;
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
    public ResponseEntity<List<ResponseResourceDto>> showAbout(@Valid @ModelAttribute(name = "path") RequestDirectoryDto requestDto) {
        List<ResponseResourceDto> responseDtoList = resourceService.findAllFromDirectory(requestDto);
        return new ResponseEntity<>(responseDtoList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseResourceDto> create(@Valid @ModelAttribute(name = "path") RequestDirectoryDto requestDto) {
        ResponseResourceDto responseDto = resourceService.createDirectory(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}
