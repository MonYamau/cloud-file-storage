package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.service.ResourceService;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {
    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<ResponseResourceDto> showAbout(@RequestParam(name = "path") String path, @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        ResponseResourceDto resourceDto = resourceService.findResource(personalDirectory + path);
        return new ResponseEntity<>(resourceDto, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> delete(@RequestParam(name = "path") String path, @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        resourceService.deleteResource(personalDirectory + path);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam(name = "path") String path, @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        byte[] bytes = resourceService.downloadResource(personalDirectory + path);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=download.zip").body(bytes);
    }

    @PostMapping("/move")
    public ResponseEntity<ResponseResourceDto> change(@RequestParam(name = "from") String from,
                                                      @RequestParam(name = "to") String to,
                                                      @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        ResponseResourceDto resourceDto = resourceService.changeResource(personalDirectory + from, personalDirectory + to);
        return new ResponseEntity<>(resourceDto, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResponseResourceDto>> search(@RequestParam(name = "query") String query, @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        List<ResponseResourceDto> resourceDtoList = resourceService.searchResource(personalDirectory, query);
        return new ResponseEntity<>(resourceDtoList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseResourceDto> upload(@RequestParam(name = "path") String path,
                                                      @RequestParam(name = "file") MultipartFile file,
                                                      @RequestAttribute(name = "userId") int userId) {
        String personalDirectory = resourceService.findPersonalDirectory(userId);
        ResponseResourceDto resourceDto = resourceService.uploadResource(personalDirectory + path, file);
        return new ResponseEntity<>(resourceDto, HttpStatus.CREATED);
    }
}
