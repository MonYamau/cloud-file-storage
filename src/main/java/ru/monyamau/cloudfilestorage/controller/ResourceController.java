package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.model.ResourceType;

import java.util.List;

@RestController
@RequestMapping("/resource")
public class ResourceController {
    @GetMapping
    public ResponseEntity<ResponseResourceDto> showAbout(@RequestParam(name = "path") String path) {
        return new ResponseEntity<>(new ResponseResourceDto("", "", null, ResourceType.FILE), HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> delete(@RequestParam(name = "path") String path) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam(name = "path") String path) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/move")
    public ResponseEntity<ResponseResourceDto> move(@RequestParam(name = "from") String from,
                                                    @RequestParam(name = "to") String to) {
        return new ResponseEntity<>(new ResponseResourceDto("", "", null, ResourceType.FILE), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResponseResourceDto>> search(@RequestParam(name = "query") String query) {
        return new ResponseEntity<>(List.of(new ResponseResourceDto("", "", null, ResourceType.FILE)), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseResourceDto> upload(@RequestParam(name = "path") String path, MultipartFile file) {
        return new ResponseEntity<>(new ResponseResourceDto("", "", null, ResourceType.FILE), HttpStatus.CREATED);
    }
}
