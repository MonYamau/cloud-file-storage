package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.monyamau.cloudfilestorage.api.ResourceApi;
import ru.monyamau.cloudfilestorage.dto.request.RequestMovementDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestQueryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestResourceDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestUploadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseDownloadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.service.ResourceService;

import java.util.List;

@RestController
public class ResourceController implements ResourceApi {
    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public ResponseEntity<ResponseResourceDto> showAbout(RequestResourceDto requestDto) {
        ResponseResourceDto responseDto = resourceService.findResource(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Override
    public ResponseEntity<Void> delete(RequestResourceDto requestDto) {
        resourceService.deleteResource(requestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<byte[]> download(RequestResourceDto requestDto) {
        ResponseDownloadDto responseDto = resourceService.downloadResource(requestDto);
        MediaType mediaType = MediaType.valueOf(responseDto.contentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(responseDto.filename()))
                .body(responseDto.bytes());
    }

    @Override
    public ResponseEntity<ResponseResourceDto> change(RequestMovementDto requestDto) {
        ResponseResourceDto responseDto = resourceService.changeResource(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Override
    public ResponseEntity<List<ResponseResourceDto>> search(RequestQueryDto requestDto) {
        List<ResponseResourceDto> responseDtoList = resourceService.searchResource(requestDto);
        return ResponseEntity.ok(responseDtoList);
    }

    @Override
    public ResponseEntity<List<ResponseResourceDto>> upload(RequestUploadDto requestDto) {
        List<ResponseResourceDto> responseDtoList = resourceService.uploadResource(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDtoList);
    }
}