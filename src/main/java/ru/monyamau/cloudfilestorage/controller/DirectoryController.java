package ru.monyamau.cloudfilestorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.monyamau.cloudfilestorage.api.DirectoryApi;
import ru.monyamau.cloudfilestorage.dto.request.RequestDirectoryDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.service.ResourceService;

import java.util.List;

@RestController
public class DirectoryController implements DirectoryApi {
    private final ResourceService resourceService;

    public DirectoryController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public ResponseEntity<List<ResponseResourceDto>> showAbout(RequestDirectoryDto requestDto) {
        List<ResponseResourceDto> responseDtoList = resourceService.findAllFromDirectory(requestDto);
        return new ResponseEntity<>(responseDtoList, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseResourceDto> create(RequestDirectoryDto requestDto) {
        ResponseResourceDto responseDto = resourceService.createDirectory(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}
