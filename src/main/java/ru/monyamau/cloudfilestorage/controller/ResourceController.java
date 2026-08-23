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
    public ResponseEntity<ResponseResourceDto> showAbout(@Valid @ModelAttribute(name = "path") RequestResourcePathDto pathDto) {
        ResponseResourceDto resourceDto = resourceService.findResource(pathDto.path());
        return new ResponseEntity<>(resourceDto, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> delete(@Valid @ModelAttribute(name = "path") RequestResourcePathDto pathDto) {
        resourceService.deleteResource(pathDto.path());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@Valid @ModelAttribute(name = "path") RequestResourcePathDto pathDto) {
        byte[] bytes = resourceService.downloadResource(pathDto.path());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=download.zip").body(bytes);
    }

    @PostMapping("/move")
    public ResponseEntity<ResponseResourceDto> change(@Valid @ModelAttribute(name = "from") RequestResourcePathDto fromPathDto,
                                                      @Valid @ModelAttribute(name = "to") RequestResourcePathDto toPathDto) {
        ResponseResourceDto resourceDto = resourceService.changeResource(fromPathDto.path(), toPathDto.path());
        return new ResponseEntity<>(resourceDto, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResponseResourceDto>> search(@RequestParam(name = "query") String query) {
        List<ResponseResourceDto> resourceDtoList = resourceService.searchResource(query);
        return new ResponseEntity<>(resourceDtoList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseResourceDto> upload(@Valid @ModelAttribute(name = "path") RequestResourcePathDto pathDto,
                                                      @RequestParam(name = "file") MultipartFile file) {
        ResponseResourceDto resourceDto = resourceService.uploadResource(pathDto.path(), file);
        return new ResponseEntity<>(resourceDto, HttpStatus.CREATED);
    }
}
