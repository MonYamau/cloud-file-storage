package ru.monyamau.cloudfilestorage.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.dto.request.RequestResourcePathDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
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
    public ResponseEntity<ResponseResourceDto> showAbout(@Valid @RequestParam(name = "path") RequestResourcePathDto pathDto, @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        ResponseResourceDto resourceDto = resourceService.findResource(personalDirectory + pathDto.path());
        return new ResponseEntity<>(resourceDto, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> delete(@Valid @RequestParam(name = "path") RequestResourcePathDto pathDto, @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        resourceService.deleteResource(personalDirectory + pathDto.path());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@Valid @RequestParam(name = "path") RequestResourcePathDto pathDto, @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        byte[] bytes = resourceService.downloadResource(personalDirectory + pathDto.path());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=download.zip").body(bytes);
    }

    @PostMapping("/move")
    public ResponseEntity<ResponseResourceDto> change(@Valid @RequestParam(name = "from") RequestResourcePathDto fromPathDto,
                                                      @Valid @RequestParam(name = "to") RequestResourcePathDto toPathDto,
                                                      @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        ResponseResourceDto resourceDto = resourceService.changeResource(
                personalDirectory + fromPathDto.path(), personalDirectory + toPathDto.path());
        return new ResponseEntity<>(resourceDto, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResponseResourceDto>> search(@RequestParam(name = "query") String query, @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        List<ResponseResourceDto> resourceDtoList = resourceService.searchResource(personalDirectory, query);
        return new ResponseEntity<>(resourceDtoList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseResourceDto> upload(@Valid @RequestParam(name = "path") RequestResourcePathDto pathDto,
                                                      @RequestParam(name = "file") MultipartFile file,
                                                      @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        ResponseResourceDto resourceDto = resourceService.uploadResource(personalDirectory + pathDto.path(), file);
        return new ResponseEntity<>(resourceDto, HttpStatus.CREATED);
    }
}
