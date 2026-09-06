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
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<HttpStatus> delete(RequestResourceDto requestDto) {
        resourceService.deleteResource(requestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<byte[]> download(RequestResourceDto requestDto) {
        ResponseDownloadDto responseDto = resourceService.downloadResource(requestDto);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(responseDto.filename()))
                .body(responseDto.bytes());
    }

    @Override
    public ResponseEntity<ResponseResourceDto> change(RequestMovementDto requestDto) {
        ResponseResourceDto responseDto = resourceService.changeResource(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ResponseResourceDto>> search(RequestQueryDto requestDto) {
        List<ResponseResourceDto> responseDtoList = resourceService.searchResource(requestDto);
        return new ResponseEntity<>(responseDtoList, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ResponseResourceDto>> upload(RequestUploadDto requestDto) {
        List<ResponseResourceDto> responseDtoList = resourceService.uploadResource(requestDto);
        return new ResponseEntity<>(responseDtoList, HttpStatus.CREATED);
    }
}