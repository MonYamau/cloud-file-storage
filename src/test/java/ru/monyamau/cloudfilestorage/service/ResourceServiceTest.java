package ru.monyamau.cloudfilestorage.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.monyamau.cloudfilestorage.BaseContextTest;
import ru.monyamau.cloudfilestorage.domain.ResourceItem;
import ru.monyamau.cloudfilestorage.domain.ResourceType;
import ru.monyamau.cloudfilestorage.dto.request.*;
import ru.monyamau.cloudfilestorage.dto.response.ResponseDownloadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.exception.InvalidInputException;
import ru.monyamau.cloudfilestorage.exception.ResourceAlreadyExistsException;
import ru.monyamau.cloudfilestorage.exception.ResourceNotFoundException;
import ru.monyamau.cloudfilestorage.handler.UserContext;
import ru.monyamau.cloudfilestorage.infrastructure.ResourceStorage;

import java.util.List;
import java.util.Optional;

@WebAppConfiguration
public class ResourceServiceTest extends BaseContextTest {
    @Autowired
    UserContext userContext;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private ResourceStorage resourceStorage;

    @Test
    @DisplayName("Успешное создание директории в хранилище")
    void shouldCreateDirectory() {
        userContext.setUserId(1);
        RequestDirectoryDto directoryDto = new RequestDirectoryDto("a/");
        ResponseResourceDto resourceDto = resourceService.createDirectory(directoryDto);
        Optional<ResourceItem> resource = resourceStorage.findResource("user-1-files/a/");
        assert resource.isPresent();
        assert resource.get().equals(new ResourceItem("user-1-files/a/", true, null));
        assert resourceDto.equals(new ResponseResourceDto("", "a", null, ResourceType.DIRECTORY));
    }

    @Test
    @DisplayName("Успешная загрузка файла в хранилище")
    void shouldUploadResource() {
        userContext.setUserId(2);
        MockMultipartFile multipartFile = new MockMultipartFile("object", "file.txt", "text/plain", "".getBytes());
        RequestUploadDto uploadDto = new RequestUploadDto("/", List.of(multipartFile));
        List<ResponseResourceDto> resourceDtoList = resourceService.uploadResource(uploadDto);
        ResponseResourceDto resourceDto = new ResponseResourceDto("", "file.txt", 0L, ResourceType.FILE);
        Optional<ResourceItem> resource = resourceStorage.findResource("user-2-files/file.txt");
        assert resource.isPresent();
        assert resource.get().equals(new ResourceItem("user-2-files/file.txt", false, 0L));
        assert resourceDtoList.size() == 1;
        assert resourceDtoList.getFirst().equals(resourceDto);
    }

    @Test
    @DisplayName("Получение информации о существующей директории")
    void shouldFindDirectory() {
        userContext.setUserId(3);
        String resourceName = "a/";
        resourceService.createDirectory(new RequestDirectoryDto(resourceName));
        ResponseResourceDto resourceDto = resourceService.findResource(new RequestResourceDto(resourceName));
        assert resourceDto.equals(new ResponseResourceDto("", "a", null, ResourceType.DIRECTORY));
    }

    @Test
    @DisplayName("Получение информации о существующем файле")
    void shouldFindFile() {
        userContext.setUserId(4);
        String resourceName = "file.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("object", resourceName, "text/plain", "".getBytes());
        resourceService.uploadResource(new RequestUploadDto("/", List.of(multipartFile)));
        ResponseResourceDto resourceDto = resourceService.findResource(new RequestResourceDto(resourceName));
        assert resourceDto.equals(new ResponseResourceDto("", resourceName, 0L, ResourceType.FILE));
    }

    @Test
    @DisplayName("Получение списка вложенных ресурсов из целевой директории")
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

    @Test
    @DisplayName("Выброс InvalidInputException при попытке удаления пользовательской директории")
    void shouldNotDeletePersonalDirectory() {
        userContext.setUserId(6);
        resourceService.createDirectory(new RequestDirectoryDto("a/"));
        Assertions.assertThrows(InvalidInputException.class,
                ()-> resourceService.deleteResource(new RequestResourceDto("/")));
    }

    @Test
    @DisplayName("Успешное переименование директории")
    void shouldChangeDirectoryName() {
        userContext.setUserId(7);
        resourceService.createDirectory(new RequestDirectoryDto("a/"));
        ResponseResourceDto resourceDto = resourceService.changeResource(new RequestMovementDto("a/", "b/"));
        assert resourceDto.equals(new ResponseResourceDto("", "b", null, ResourceType.DIRECTORY));
        assert resourceStorage.findResource("user-7-files/a/").isEmpty();
        Optional<ResourceItem> resource = resourceStorage.findResource("user-7-files/b/");
        assert resource.isPresent();
        assert resource.get().equals(new ResourceItem("user-7-files/b/", true, null));
    }

    @Test
    @DisplayName("Успешный перенос директории")
    void shouldChangeDirectoryPath() {
        userContext.setUserId(8);
        resourceService.createDirectory(new RequestDirectoryDto("a/"));
        resourceService.createDirectory(new RequestDirectoryDto("b/"));
        resourceService.createDirectory(new RequestDirectoryDto("a/test/"));
        ResponseResourceDto resourceDto = resourceService.changeResource(new RequestMovementDto("a/test/", "b/test/"));
        assert resourceDto.equals(new ResponseResourceDto("b/", "test", null, ResourceType.DIRECTORY));
        assert resourceStorage.findResource("user-8-files/a/test/").isEmpty();
        Optional<ResourceItem> resource = resourceStorage.findResource("user-8-files/b/test/");
        assert resource.isPresent();
        assert resource.get().equals(new ResourceItem("user-8-files/b/test/", true, null));
    }

    @Test
    @DisplayName("Успешное переименование файла")
    void shouldChangeFileName() {
        userContext.setUserId(9);
        MockMultipartFile multipartFile = new MockMultipartFile("object", "a/file.txt", "text/plain", "".getBytes());
        resourceService.uploadResource(new RequestUploadDto("", List.of(multipartFile)));
        ResponseResourceDto resourceDto = resourceService.changeResource(new RequestMovementDto("a/file.txt", "a/test.txt"));
        assert resourceDto.equals(new ResponseResourceDto("a/", "test.txt", 0L, ResourceType.FILE));
        assert resourceStorage.findResource("user-9-files/a/file.txt").isEmpty();
        Optional<ResourceItem> resource = resourceStorage.findResource("user-9-files/a/test.txt");
        assert resource.isPresent();
        assert resource.get().equals(new ResourceItem("user-9-files/a/test.txt", false, 0L));
    }

    @Test
    @DisplayName("Успешный перенос файла")
    void shouldChangeFilePath() {
        userContext.setUserId(10);
        MockMultipartFile multipartFile = new MockMultipartFile("object", "a/file.txt", "text/plain", "".getBytes());
        resourceService.uploadResource(new RequestUploadDto("", List.of(multipartFile)));
        resourceService.createDirectory(new RequestDirectoryDto("b/"));
        ResponseResourceDto resourceDto = resourceService.changeResource(new RequestMovementDto("a/file.txt", "b/file.txt"));
        assert resourceDto.equals(new ResponseResourceDto("b/", "file.txt", 0L, ResourceType.FILE));
        assert resourceStorage.findResource("user-10-files/a/file.txt").isEmpty();
        Optional<ResourceItem> resource = resourceStorage.findResource("user-10-files/b/file.txt");
        assert resource.isPresent();
        assert resource.get().equals(new ResourceItem("user-10-files/b/file.txt", false, 0L));
    }

    @Test
    @DisplayName("Успешное удаление директории")
    void shouldDeleteDirectory() {
        userContext.setUserId(11);
        resourceService.createDirectory(new RequestDirectoryDto("a/"));
        assert resourceStorage.findResource("user-11-files/a/").isPresent();
        resourceService.deleteResource(new RequestResourceDto("a/"));
        assert resourceStorage.findResource("user-11-files/a/").isEmpty();
    }

    @Test
    @DisplayName("Успешное удаление файла")
    void shouldDeleteFile() {
        userContext.setUserId(12);
        MockMultipartFile multipartFile = new MockMultipartFile("object", "file.txt", "text/plain", "".getBytes());
        resourceService.uploadResource(new RequestUploadDto("", List.of(multipartFile)));
        assert resourceStorage.findResource("user-12-files/file.txt").isPresent();
        resourceService.deleteResource(new RequestResourceDto("file.txt"));
        assert resourceStorage.findResource("user-12-files/file.txt").isEmpty();
    }

    @Test
    @DisplayName("Выброс ResourceNotFoundException при попытке создания ресурса в несуществующем пути")
    void shouldThrowResourceNotFoundException() {
        userContext.setUserId(13);
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> resourceService.createDirectory(new RequestDirectoryDto("a/b/")));
    }

    @Test
    @DisplayName("Выброс ResourceAlreadyExistsException при повторном создании уже существующего ресурса")
    void shouldThrowResourceAlreadyExistsException() {
        userContext.setUserId(14);
        resourceService.createDirectory(new RequestDirectoryDto("a/"));
        Assertions.assertThrows(ResourceAlreadyExistsException.class,
                () -> resourceService.createDirectory(new RequestDirectoryDto("a/")));
    }

    @Test
    @DisplayName("Успешное получение байтов файла для скачивания")
    void shouldDownloadResource() {
        userContext.setUserId(15);
        MockMultipartFile multipartFile = new MockMultipartFile("object", "file.txt", "text/plain", "".getBytes());
        resourceService.uploadResource(new RequestUploadDto("", List.of(multipartFile)));
        ResponseDownloadDto responseDownloadDto = resourceService.downloadResource(new RequestResourceDto("file.txt"));
        assert responseDownloadDto.filename().equals("file.txt");
        assert responseDownloadDto.bytes() != null;
    }

    @Test
    @DisplayName("Успешный поиск ресурсов во всех директориях пользователя по подстроке")
    void shouldSearchResources() {
        userContext.setUserId(16);
        MockMultipartFile multipartFile = new MockMultipartFile("object", "abc/text/file.txt", "text/plain", "".getBytes());
        resourceService.createDirectory(new RequestDirectoryDto("text_folder/"));
        resourceService.createDirectory(new RequestDirectoryDto("папка/"));
        resourceService.uploadResource(new RequestUploadDto("", List.of(multipartFile)));
        List<ResponseResourceDto> resourceDtoList = resourceService.searchResource(new RequestQueryDto("xt"));
        assert resourceDtoList.contains(new ResponseResourceDto("", "text_folder", null, ResourceType.DIRECTORY));
        assert resourceDtoList.contains(new ResponseResourceDto("abc/", "text", null, ResourceType.DIRECTORY));
        assert resourceDtoList.contains(new ResponseResourceDto("abc/text/", "file.txt", 0L, ResourceType.FILE));
        List<ResponseResourceDto> resourceDtoList1 = resourceService.searchResource(new RequestQueryDto("папка"));
        assert resourceDtoList1.contains(new ResponseResourceDto("", "папка", null, ResourceType.DIRECTORY));
    }
}
