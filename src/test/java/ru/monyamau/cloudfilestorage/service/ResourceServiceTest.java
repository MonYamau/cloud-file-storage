package ru.monyamau.cloudfilestorage.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.monyamau.cloudfilestorage.BaseContextTest;
import ru.monyamau.cloudfilestorage.domain.ResourceItem;
import ru.monyamau.cloudfilestorage.domain.ResourceType;
import ru.monyamau.cloudfilestorage.dto.request.RequestDirectoryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestMovementDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestQueryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestResourceDto;
import ru.monyamau.cloudfilestorage.dto.request.service.UploadCommand;
import ru.monyamau.cloudfilestorage.dto.request.service.UploadedFile;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.exception.InvalidInputException;
import ru.monyamau.cloudfilestorage.exception.ResourceAlreadyExistsException;
import ru.monyamau.cloudfilestorage.exception.ResourceNotFoundException;
import ru.monyamau.cloudfilestorage.infrastructure.ResourceStorage;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

public class ResourceServiceTest extends BaseContextTest {
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private ResourceStorage resourceStorage;

    @Test
    @DisplayName("Успешное создание директории в хранилище")
    void shouldCreateDirectory() {
        Integer userId = 1;
        RequestDirectoryDto directoryDto = new RequestDirectoryDto("a/");
        ResponseResourceDto resourceDto = resourceService.createDirectory(directoryDto, userId);
        Optional<ResourceItem> resource = resourceStorage.findResource("user-1-files/a/");
        Assertions.assertTrue(resource.isPresent());
        Assertions.assertEquals(new ResourceItem("user-1-files/a/", true, null), resource.get());
        Assertions.assertEquals(new ResponseResourceDto("", "a", null, ResourceType.DIRECTORY), resourceDto);
    }

    @Test
    @DisplayName("Успешная загрузка файла в хранилище")
    void shouldUploadResource() {
        Integer userId = 2;
        UploadedFile uploadedFile = new UploadedFile("file.txt", "text/plain", 0L, new ByteArrayInputStream("".getBytes()));
        UploadCommand command = new UploadCommand("/", List.of(uploadedFile));
        List<ResponseResourceDto> resourceDtoList = resourceService.uploadResource(command, userId);
        ResponseResourceDto resourceDto = new ResponseResourceDto("", "file.txt", 0L, ResourceType.FILE);
        Optional<ResourceItem> resource = resourceStorage.findResource("user-2-files/file.txt");
        Assertions.assertTrue(resource.isPresent());
        Assertions.assertEquals(new ResourceItem("user-2-files/file.txt", false, 0L), resource.get());
        Assertions.assertEquals(1, resourceDtoList.size());
        Assertions.assertEquals(resourceDtoList.getFirst(), resourceDto);
    }

    @Test
    @DisplayName("Получение информации о существующей директории")
    void shouldFindDirectory() {
        Integer userId = 3;
        String resourceName = "a/";
        resourceService.createDirectory(new RequestDirectoryDto(resourceName), userId);
        ResponseResourceDto resourceDto = resourceService.findResource(new RequestResourceDto(resourceName), userId);
        Assertions.assertEquals(new ResponseResourceDto("", "a", null, ResourceType.DIRECTORY), resourceDto);
    }

    @Test
    @DisplayName("Получение информации о существующем файле")
    void shouldFindFile() {
        Integer userId = 4;
        String resourceName = "file.txt";
        UploadedFile uploadedFile = new UploadedFile(resourceName, "text/plain", 0L, new ByteArrayInputStream("".getBytes()));
        UploadCommand command = new UploadCommand("/", List.of(uploadedFile));
        resourceService.uploadResource(command, userId);
        ResponseResourceDto resourceDto = resourceService.findResource(new RequestResourceDto(resourceName), userId);
        Assertions.assertEquals(new ResponseResourceDto("", resourceName, 0L, ResourceType.FILE), resourceDto);
    }

    @Test
    @DisplayName("Получение списка вложенных ресурсов из целевой директории")
    void shouldFindAllFromDirectory() {
        Integer userId = 5;
        String firstDirectoryName = "a/";
        String secondDirectoryName = "b/";
        resourceService.createDirectory(new RequestDirectoryDto(firstDirectoryName), userId);
        resourceService.createDirectory(new RequestDirectoryDto(firstDirectoryName + secondDirectoryName), userId);
        List<ResponseResourceDto> allFromDirectory = resourceService
                .findAllFromDirectory(new RequestDirectoryDto(firstDirectoryName), userId);
        Assertions.assertEquals(1, allFromDirectory.size());
        Assertions.assertEquals(new ResponseResourceDto(firstDirectoryName, "b", null, ResourceType.DIRECTORY), allFromDirectory.getFirst());
    }

    @Test
    @DisplayName("Выброс InvalidInputException при попытке удаления пользовательской директории")
    void shouldNotDeletePersonalDirectory() {
        Integer userId = 6;
        resourceService.createDirectory(new RequestDirectoryDto("a/"), userId);
        Assertions.assertThrows(InvalidInputException.class,
                () -> resourceService.deleteResource(new RequestResourceDto("/"), userId));
    }

    @Test
    @DisplayName("Успешное переименование директории")
    void shouldChangeDirectoryName() {
        Integer userId = 7;
        resourceService.createDirectory(new RequestDirectoryDto("a/"), userId);
        ResponseResourceDto resourceDto = resourceService.changeResource(new RequestMovementDto("a/", "b/"), userId);
        Assertions.assertEquals(new ResponseResourceDto("", "b", null, ResourceType.DIRECTORY), resourceDto);
        Assertions.assertTrue(resourceStorage.findResource("user-7-files/a/").isEmpty());
        Optional<ResourceItem> resource = resourceStorage.findResource("user-7-files/b/");
        Assertions.assertTrue(resource.isPresent());
        Assertions.assertEquals(new ResourceItem("user-7-files/b/", true, null), resource.get());
    }

    @Test
    @DisplayName("Успешный перенос директории")
    void shouldChangeDirectoryPath() {
        Integer userId = 8;
        resourceService.createDirectory(new RequestDirectoryDto("a/"), userId);
        resourceService.createDirectory(new RequestDirectoryDto("b/"), userId);
        resourceService.createDirectory(new RequestDirectoryDto("a/test/"), userId);
        ResponseResourceDto resourceDto = resourceService.changeResource(new RequestMovementDto("a/test/", "b/test/"), userId);
        Assertions.assertEquals(new ResponseResourceDto("b/", "test", null, ResourceType.DIRECTORY), resourceDto);
        Assertions.assertTrue(resourceStorage.findResource("user-8-files/a/test/").isEmpty());
        Optional<ResourceItem> resource = resourceStorage.findResource("user-8-files/b/test/");
        Assertions.assertTrue(resource.isPresent());
        Assertions.assertEquals(new ResourceItem("user-8-files/b/test/", true, null), resource.get());
    }

    @Test
    @DisplayName("Успешное переименование файла")
    void shouldChangeFileName() {
        Integer userId = 9;
        UploadedFile uploadedFile = new UploadedFile("a/file.txt", "text/plain", 0L, new ByteArrayInputStream("".getBytes()));
        UploadCommand command = new UploadCommand("/", List.of(uploadedFile));
        resourceService.uploadResource(command, userId);
        ResponseResourceDto resourceDto = resourceService.changeResource(new RequestMovementDto("a/file.txt", "a/test.txt"), userId);
        Assertions.assertEquals(new ResponseResourceDto("a/", "test.txt", 0L, ResourceType.FILE), resourceDto);
        Assertions.assertTrue(resourceStorage.findResource("user-9-files/a/file.txt").isEmpty());
        Optional<ResourceItem> resource = resourceStorage.findResource("user-9-files/a/test.txt");
        Assertions.assertTrue(resource.isPresent());
        Assertions.assertEquals(new ResourceItem("user-9-files/a/test.txt", false, 0L), resource.get());
    }

    @Test
    @DisplayName("Успешный перенос файла")
    void shouldChangeFilePath() {
        Integer userId = 10;
        UploadedFile uploadedFile = new UploadedFile("a/file.txt", "text/plain", 0L, new ByteArrayInputStream("".getBytes()));
        UploadCommand command = new UploadCommand("/", List.of(uploadedFile));
        resourceService.uploadResource(command, userId);
        resourceService.createDirectory(new RequestDirectoryDto("b/"), userId);
        ResponseResourceDto resourceDto = resourceService.changeResource(new RequestMovementDto("a/file.txt", "b/file.txt"), userId);
        Assertions.assertEquals(new ResponseResourceDto("b/", "file.txt", 0L, ResourceType.FILE), resourceDto);
        Assertions.assertTrue(resourceStorage.findResource("user-10-files/a/file.txt").isEmpty());
        Optional<ResourceItem> resource = resourceStorage.findResource("user-10-files/b/file.txt");
        Assertions.assertTrue(resource.isPresent());
        Assertions.assertEquals(new ResourceItem("user-10-files/b/file.txt", false, 0L), resource.get());
    }

    @Test
    @DisplayName("Успешное удаление директории")
    void shouldDeleteDirectory() {
        Integer userId = 11;
        resourceService.createDirectory(new RequestDirectoryDto("a/"), userId);
        Assertions.assertTrue(resourceStorage.findResource("user-11-files/a/").isPresent());
        resourceService.deleteResource(new RequestResourceDto("a/"), userId);
        Assertions.assertTrue(resourceStorage.findResource("user-11-files/a/").isEmpty());
    }

    @Test
    @DisplayName("Успешное удаление файла")
    void shouldDeleteFile() {
        Integer userId = 12;
        UploadedFile uploadedFile = new UploadedFile("file.txt", "text/plain", 0L, new ByteArrayInputStream("".getBytes()));
        UploadCommand command = new UploadCommand("/", List.of(uploadedFile));
        resourceService.uploadResource(command, userId);
        Assertions.assertTrue(resourceStorage.findResource("user-12-files/file.txt").isPresent());
        resourceService.deleteResource(new RequestResourceDto("file.txt"), userId);
        Assertions.assertTrue(resourceStorage.findResource("user-12-files/file.txt").isEmpty());
    }

    @Test
    @DisplayName("Выброс ResourceNotFoundException при попытке создания ресурса в несуществующем пути")
    void shouldThrowResourceNotFoundException() {
        Integer userId = 13;
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> resourceService.createDirectory(new RequestDirectoryDto("a/b/"), userId));
    }

    @Test
    @DisplayName("Выброс ResourceAlreadyExistsException при повторном создании уже существующего ресурса")
    void shouldThrowResourceAlreadyExistsException() {
        Integer userId = 14;
        resourceService.createDirectory(new RequestDirectoryDto("a/"), userId);
        Assertions.assertThrows(ResourceAlreadyExistsException.class,
                () -> resourceService.createDirectory(new RequestDirectoryDto("a/"), userId));
    }

    @Test
    @DisplayName("Успешный поиск ресурсов во всех директориях пользователя по подстроке")
    void shouldSearchResources() {
        Integer userId = 16;
        UploadedFile uploadedFile = new UploadedFile("abc/text/file.txt", "text/plain", 0L, new ByteArrayInputStream("".getBytes()));
        UploadCommand command = new UploadCommand("/", List.of(uploadedFile));
        resourceService.createDirectory(new RequestDirectoryDto("text_folder/"), userId);
        resourceService.createDirectory(new RequestDirectoryDto("папка/"), userId);
        resourceService.uploadResource(command, userId);
        List<ResponseResourceDto> resourceDtoList = resourceService.searchResource(new RequestQueryDto("xt"), userId);
        Assertions.assertTrue(resourceDtoList.contains(new ResponseResourceDto("", "text_folder", null, ResourceType.DIRECTORY)));
        Assertions.assertTrue(resourceDtoList.contains(new ResponseResourceDto("abc/", "text", null, ResourceType.DIRECTORY)));
        Assertions.assertTrue(resourceDtoList.contains(new ResponseResourceDto("abc/text/", "file.txt", 0L, ResourceType.FILE)));
        List<ResponseResourceDto> resourceDtoList1 = resourceService.searchResource(new RequestQueryDto("папка"), userId);
        Assertions.assertTrue(resourceDtoList1.contains(new ResponseResourceDto("", "папка", null, ResourceType.DIRECTORY)));
    }


    @Test
    @DisplayName("Выброс InvalidInputException при попытке сохранить файлы с дублирующимся именем")
    void shouldNotUploadResourcesWithDuplicateName() {
        Integer userId = 17;
        UploadedFile uploadedFile = new UploadedFile("file.txt", "text/plain", 0L, new ByteArrayInputStream("".getBytes()));
        UploadedFile uploadedFileDuplicate = new UploadedFile("file.txt", "text/plain", 0L, new ByteArrayInputStream("".getBytes()));
        UploadCommand command = new UploadCommand("/", List.of(uploadedFile, uploadedFileDuplicate));
        Assertions.assertThrows(InvalidInputException.class, () -> resourceService.uploadResource(command, userId));
    }
}
