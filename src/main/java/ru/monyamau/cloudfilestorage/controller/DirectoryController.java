package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.monyamau.cloudfilestorage.api.DirectoryApi;
import ru.monyamau.cloudfilestorage.dto.request.RequestDirectoryDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.handler.UserContext;
import ru.monyamau.cloudfilestorage.service.ResourceService;

import java.util.List;

@RestController
public class DirectoryController implements DirectoryApi {
    private final ResourceService resourceService;
    private final UserContext userContext;

    public DirectoryController(ResourceService resourceService, UserContext userContext) {
        this.resourceService = resourceService;
        this.userContext = userContext;
    }

    @Override
    public ResponseEntity<List<ResponseResourceDto>> showAbout(RequestDirectoryDto requestDto) {
        List<ResponseResourceDto> responseDtoList = resourceService.findAllFromDirectory(requestDto, userContext.getUserId());
        return ResponseEntity.ok(responseDtoList);
    }

    @Override
    public ResponseEntity<ResponseResourceDto> create(RequestDirectoryDto requestDto) {
        ResponseResourceDto responseDto = resourceService.createDirectory(requestDto, userContext.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
