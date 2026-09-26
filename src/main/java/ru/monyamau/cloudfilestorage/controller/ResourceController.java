package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.monyamau.cloudfilestorage.api.ResourceApi;
import ru.monyamau.cloudfilestorage.dto.request.RequestMovementDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestQueryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestResourceDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestUploadDto;
import ru.monyamau.cloudfilestorage.dto.request.service.UploadCommand;
import ru.monyamau.cloudfilestorage.dto.response.ResponseDownloadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.handler.UserContext;
import ru.monyamau.cloudfilestorage.mapper.UploadCommandMapper;
import ru.monyamau.cloudfilestorage.service.ResourceService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class ResourceController implements ResourceApi {
    private final ResourceService resourceService;
    private final UserContext userContext;
    private final UploadCommandMapper commandMapper;

    public ResourceController(ResourceService resourceService, UserContext userContext, UploadCommandMapper commandMapper) {
        this.resourceService = resourceService;
        this.userContext = userContext;
        this.commandMapper = commandMapper;
    }

    @Override
    public ResponseEntity<ResponseResourceDto> showAbout(RequestResourceDto requestDto) {
        ResponseResourceDto responseDto = resourceService.findResource(requestDto, userContext.getUserId());
        return ResponseEntity.ok(responseDto);
    }

    @Override
    public ResponseEntity<Void> delete(RequestResourceDto requestDto) {
        resourceService.deleteResource(requestDto, userContext.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<StreamingResponseBody> download(RequestResourceDto requestDto) {
        ResponseDownloadDto responseDto = resourceService.downloadResource(requestDto, userContext.getUserId());
        StreamingResponseBody body = responseDto.performer()::accept;
        MediaType mediaType = MediaTypeFactory.getMediaType(responseDto.filename())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(responseDto.filename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(body);
    }

    @Override
    public ResponseEntity<ResponseResourceDto> change(RequestMovementDto requestDto) {
        ResponseResourceDto responseDto = resourceService.changeResource(requestDto, userContext.getUserId());
        return ResponseEntity.ok(responseDto);
    }

    @Override
    public ResponseEntity<List<ResponseResourceDto>> search(RequestQueryDto requestDto) {
        List<ResponseResourceDto> responseDtoList = resourceService.searchResource(requestDto, userContext.getUserId());
        return ResponseEntity.ok(responseDtoList);
    }

    @Override
    public ResponseEntity<List<ResponseResourceDto>> upload(RequestUploadDto requestDto) {
        UploadCommand command = commandMapper.toCommand(requestDto);
        List<ResponseResourceDto> responseDtoList = resourceService.uploadResource(command, userContext.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDtoList);
    }
}