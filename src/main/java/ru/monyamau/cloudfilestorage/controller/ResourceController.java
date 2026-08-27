package ru.monyamau.cloudfilestorage.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.monyamau.cloudfilestorage.dto.request.RequestMovementDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestQueryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestResourceDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestUploadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.exception.InvalidInputException;
import ru.monyamau.cloudfilestorage.service.ResourceService;

import java.util.List;

@RestController
@RequestMapping("/resource")
public class ResourceController {
    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<ResponseResourceDto> showAbout(@Valid @ModelAttribute(name = "path") RequestResourceDto requestDto) {
        ResponseResourceDto responseDto = resourceService.findResource(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> delete(@Valid @ModelAttribute(name = "path") RequestResourceDto requestDto) {
        resourceService.deleteResource(requestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@Valid @ModelAttribute(name = "path") RequestResourceDto requestDto) {
        byte[] bytes = resourceService.downloadResource(requestDto);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=download.zip").body(bytes);
    }

    @PostMapping("/move")
    public ResponseEntity<ResponseResourceDto> change(@Valid @ModelAttribute RequestMovementDto requestDto) {
        ResponseResourceDto responseDto = resourceService.changeResource(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResponseResourceDto>> search(@Valid @ModelAttribute(name = "query") RequestQueryDto requestDto) {
        List<ResponseResourceDto> responseDtoList = resourceService.searchResource(requestDto);
        return new ResponseEntity<>(responseDtoList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<List<ResponseResourceDto>> upload(@Valid @ModelAttribute RequestUploadDto requestDto) {
        if (requestDto.file().getOriginalFilename() == null || requestDto.file().getOriginalFilename().isBlank()) {
            throw new InvalidInputException("Имя файла не может отсутствовать или быть пустым");
        }
        List<ResponseResourceDto> responseDtoList = resourceService.uploadResource(requestDto);
        return new ResponseEntity<>(responseDtoList, HttpStatus.CREATED);
    }
}