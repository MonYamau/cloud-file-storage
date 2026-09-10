package ru.monyamau.cloudfilestorage.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.monyamau.cloudfilestorage.BaseContextTest;
import ru.monyamau.cloudfilestorage.domain.ResourceType;
import ru.monyamau.cloudfilestorage.dto.request.RequestDirectoryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestResourceDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestUploadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.handler.UserContext;
import ru.monyamau.cloudfilestorage.infrastructure.ResourceStorage;

import java.util.List;

@WebAppConfiguration
public class ResourceServiceTest extends BaseContextTest {
    @Autowired
    UserContext userContext;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private ResourceStorage resourceStorage;

    @Test
    @DisplayName("Директория должна успешно создаться в хранилище")
    void shouldCreateDirectory() {
        userContext.setUserId(1);
        RequestDirectoryDto directoryDto = new RequestDirectoryDto("a/");
        ResponseResourceDto resourceDto = resourceService.createDirectory(directoryDto);
        assert resourceDto.equals(new ResponseResourceDto("", "a", null, ResourceType.DIRECTORY));
    }

    @Test
    @DisplayName("Файл должен успешно загрузиться в хранилище")
    void shouldUploadResource() {
        userContext.setUserId(2);
        MockMultipartFile multipartFile = new MockMultipartFile("object", "file.txt", "text/plain", "".getBytes());
        RequestUploadDto uploadDto = new RequestUploadDto("/", List.of(multipartFile));
        List<ResponseResourceDto> resourceDtoList = resourceService.uploadResource(uploadDto);
        ResponseResourceDto resourceDto = new ResponseResourceDto("", "file.txt", 0L, ResourceType.FILE);
        assert resourceDtoList.size() == 1;
        assert resourceDtoList.getFirst().equals(resourceDto);
    }

    @Test
    @DisplayName("Должна найтись директория")
    void shouldFindDirectory() {
        userContext.setUserId(3);
        String resourceName = "a/";
        resourceService.createDirectory(new RequestDirectoryDto(resourceName));
        ResponseResourceDto resourceDto = resourceService.findResource(new RequestResourceDto(resourceName));
        assert resourceDto.equals(new ResponseResourceDto("", "a", null, ResourceType.DIRECTORY));
    }

    @Test
    @DisplayName("Должен найтись файл")
    void shouldFindFile() {
        userContext.setUserId(4);
        String resourceName = "file.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("object", resourceName, "text/plain", "".getBytes());
        resourceService.uploadResource(new RequestUploadDto("/", List.of(multipartFile)));
        ResponseResourceDto resourceDto = resourceService.findResource(new RequestResourceDto(resourceName));
        assert resourceDto.equals(new ResponseResourceDto("", resourceName, 0L, ResourceType.FILE));
    }

    @Test
    @DisplayName("Должен успешно пройти поиск по директории")
    void shouldFindAllFromDirectory() {
        userContext.setUserId(5);
        String firstDirectoryName = "a/";
        String secondDirectoryName = "b/";
        resourceService.createDirectory(new RequestDirectoryDto(firstDirectoryName));
        resourceService.createDirectory(new RequestDirectoryDto(firstDirectoryName + secondDirectoryName));
        List<ResponseResourceDto> allFromDirectory = resourceService
                .findAllFromDirectory(new RequestDirectoryDto(firstDirectoryName));
        assert allFromDirectory.size() == 1;
        assert allFromDirectory.getFirst()
                .equals(new ResponseResourceDto(firstDirectoryName, "b", null, ResourceType.DIRECTORY));
    }
}
