package ru.monyamau.cloudfilestorage.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.dto.request.*;
import ru.monyamau.cloudfilestorage.dto.response.ResponseDownloadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.exception.InvalidInputException;
import ru.monyamau.cloudfilestorage.exception.ResourceAlreadyExistsException;
import ru.monyamau.cloudfilestorage.exception.ResourceNotFoundException;
import ru.monyamau.cloudfilestorage.model.ResourceItem;
import ru.monyamau.cloudfilestorage.model.ResourceType;
import ru.monyamau.cloudfilestorage.repository.MinioResourceStorage;
import ru.monyamau.cloudfilestorage.util.UserContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ResourceService {
    private final static String PERSONAL_DIRECTORY_NAME = "user-%s-files/";
    private final static String SEPARATOR_SIGN = "/";

    private final MinioResourceStorage resourceStorage;
    private final UserContext userContext;

    public ResourceService(MinioResourceStorage resourceStorage, UserContext userContext) {
        this.resourceStorage = resourceStorage;
        this.userContext = userContext;
    }

    public void createPersonalDirectory(int userId) {
        String directoryName = PERSONAL_DIRECTORY_NAME.formatted(userId);
        Optional<ResourceItem> result = resourceStorage.findDirectory(directoryName);
        if (result.isEmpty()) {
            resourceStorage.createDirectory(directoryName);
        }
    }

    public List<ResponseResourceDto> findAllFromDirectory(RequestDirectoryDto directoryDto) {
        String fullPath = formatPersonalPath(directoryDto.path());
        String personalDirectory = PERSONAL_DIRECTORY_NAME.formatted(userContext.getUserId());
        String personalDirectoryName = personalDirectory.replace(SEPARATOR_SIGN, "");
        if (!isResourceExists(fullPath)) {
            throw new ResourceNotFoundException("Ресурс по данному пути не найден: " + directoryDto.path());
        }
        List<ResponseResourceDto> result = new ArrayList<>();
        List<ResourceItem> resources = resourceStorage.findAllFromDirectory(fullPath);
        for (ResourceItem resource : resources) {
            ResponseResourceDto converted = convert(resource);
            if (personalDirectoryName.equals(converted.name())) continue;
            result.add(converted);
        }
        return result;
    }

    public ResponseResourceDto createDirectory(RequestDirectoryDto directoryDto) {
        String fullPath = formatPersonalPath(directoryDto.path());
        String parentDirectory = formatParentDirectory(fullPath);
        if (!isResourceExists(parentDirectory)) {
            throw new ResourceNotFoundException("Родительская директория не найдена");
        }
        if (isResourceExists(fullPath)) {
            throw new ResourceAlreadyExistsException("Директория по данному пути уже существует: " + directoryDto.path());
        }
        resourceStorage.createDirectory(fullPath);
        ResourceItem item = resourceStorage.findDirectory(fullPath)
                .orElseThrow(() -> new IllegalStateException(
                        "Ошибка создания директории: не удалось найти ресурс"));
        return convert(item);
    }

    public ResponseResourceDto findResource(RequestResourceDto resourceDto) {
        String fullPath = formatPersonalPath(resourceDto.path());
        String personalDirectory = PERSONAL_DIRECTORY_NAME.formatted(userContext.getUserId());
        if (personalDirectory.equals(fullPath)) {
            return new ResponseResourceDto("", "", null, ResourceType.DIRECTORY);
        }
        if (isDirectory(fullPath)) {
            ResourceItem directory = resourceStorage.findDirectory(fullPath)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Директория с текущим именем не найдена: " + resourceDto.path()));
            return convert(directory);
        }
        ResourceItem file = resourceStorage.findFile(fullPath)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Файл с текущим именем не найден: " + resourceDto.path()));
        return convert(file);
    }

    public List<ResponseResourceDto> searchResource(RequestQueryDto queryDto) {
        String personalDirectory = PERSONAL_DIRECTORY_NAME.formatted(userContext.getUserId());
        String personalDirectoryName = personalDirectory.replace(SEPARATOR_SIGN, "");
        List<ResponseResourceDto> result = new ArrayList<>();
        List<ResourceItem> resources = resourceStorage.findAllByPrefix(personalDirectory);
        for (ResourceItem resource : resources) {
            ResponseResourceDto converted = convert(resource);
            if (personalDirectoryName.equals(converted.name())) continue;
            if (matchQueryWithLowerCase(converted.name(), queryDto.query())) {
                result.add(converted);
            }
        }
        return result;
    }

    public void deleteResource(RequestResourceDto resourceDto) {
        String fullPath = formatPersonalPath(resourceDto.path());
        if (!isResourceExists(fullPath)) {
            throw new ResourceNotFoundException("Ресурс по данному пути не найден: " + resourceDto.path());
        }
        if (isDirectory(fullPath)) {
            resourceStorage.deleteDirectory(fullPath);
        } else {
            resourceStorage.deleteFile(fullPath);
        }
    }

    public List<ResponseResourceDto> uploadResource(RequestUploadDto uploadDto) {
        String fullRequestPath = formatPersonalPath(uploadDto.path());
        String fileName = uploadDto.file().getOriginalFilename();
        String filePath = fullRequestPath + fileName;
        if (isResourceExists(filePath)) {
            throw new ResourceAlreadyExistsException(
                    "Ресурс по данному пути уже существует: " + uploadDto.path() + fileName);
        }
        if (fileName.contains(SEPARATOR_SIGN)) {
            return uploadDirectory(fullRequestPath, fileName, uploadDto.file());
        }
        return uploadFile(filePath, uploadDto.file());
    }

    private List<ResponseResourceDto> uploadDirectory(String requestPath, String fileName, MultipartFile file) {
        List<ResourceItem> allResources = createSubdirectories(fileName, requestPath);
        String filePath = resourceStorage.upload(requestPath + fileName, file);
        ResourceItem resource = resourceStorage.findFile(filePath)
                .orElseThrow(() -> new IllegalStateException(
                        "Ошибка сохранения директории: не удалось найти ресурс"));
        allResources.add(resource);
        return allResources.stream().map(this::convert).toList();
    }

    private List<ResponseResourceDto> uploadFile(String path, MultipartFile file) {
        String resourcePath = resourceStorage.upload(path, file);
        ResourceItem resource = resourceStorage.findFile(resourcePath)
                .orElseThrow(() -> new IllegalStateException(
                        "Ошибка сохранения файла: не удалось найти ресурс"));
        return List.of(convert(resource));
    }

    public ResponseDownloadDto downloadResource(RequestResourceDto resourceDto) {
        String fullPath = formatPersonalPath(resourceDto.path());
        if (!isResourceExists(fullPath)) {
            throw new ResourceNotFoundException("Ресурс по данному пути не найден: " + resourceDto.path());
        }
        String[] resources = fullPath.split(SEPARATOR_SIGN);
        String fileName = resources[resources.length - 1];
        if (isDirectory(fullPath)) {
            return downloadDirectory(fullPath, fileName);
        }
        return downloadFile(fullPath, fileName);
    }

    private ResponseDownloadDto downloadFile(String path, String fileName) {
        try (InputStream inputStream = resourceStorage.download(path)) {
            return new ResponseDownloadDto(fileName, inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ResponseDownloadDto downloadDirectory(String path, String fileName) {
        List<ResourceItem> resourceItemList = resourceStorage.findAllByPrefix(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (ResourceItem resourceItem : resourceItemList) {
                String fullObjectName = resourceItem.objectName();
                if (isDirectory(fullObjectName)) continue;
                zipOutputStream.putNextEntry(new ZipEntry(fullObjectName.replace(path, "")));
                try (InputStream inputStream = resourceStorage.download(resourceItem.objectName())) {
                    inputStream.transferTo(zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseDownloadDto(fileName + ".zip", byteArrayOutputStream.toByteArray());
    }

    public ResponseResourceDto changeResource(RequestMovementDto movementDto) {
        String oldPath = formatPersonalPath(movementDto.from());
        String newPath = formatPersonalPath(movementDto.to());
        validateMovement(oldPath, newPath);
        if (!isResourceExists(oldPath)) {
            throw new ResourceNotFoundException("Ресурс по данному пути не найден: " + movementDto.from());
        }
        if (isResourceExists(newPath)) {
            throw new ResourceAlreadyExistsException("Ресурс по данному пути уже существует: " + movementDto.to());
        }
        if (!isResourceExists(formatParentDirectory(newPath))) {
            throw new ResourceNotFoundException(
                    "Директория для перемещения ресурса не найдена: " + formatParentDirectory(movementDto.to()));
        }
        if (isDirectory(oldPath) && isDirectory(newPath)) {
            ResourceItem resourceItem = changeDirectory(oldPath, newPath);
            return convert(resourceItem);
        }
        ResourceItem resourceItem = changeFile(oldPath, newPath);
        return convert(resourceItem);
    }

    private ResourceItem changeFile(String oldName, String newName) {
        String copy = resourceStorage.copy(oldName, newName);
        ResourceItem resourceItem = resourceStorage
                .findFile(copy)
                .orElseThrow(() -> new IllegalStateException(
                        "Ошибка перемещения/переименования файла: не удалось найти ресурс"));
        resourceStorage.deleteFile(oldName);
        return resourceItem;
    }

    private ResourceItem changeDirectory(String oldPath, String newPath) {
        List<ResourceItem> resourceItemList = resourceStorage.findAllByPrefix(oldPath);
        resourceStorage.createDirectory(newPath);
        for (ResourceItem resourceItem : resourceItemList) {
            String oldObjectPath = resourceItem.objectName();
            String newObjectPath = oldObjectPath.replace(oldPath, newPath);
            resourceStorage.copy(oldObjectPath, newObjectPath);
        }
        ResourceItem resourceItem = resourceStorage.findDirectory(newPath)
                .orElseThrow(() -> new IllegalStateException(
                        "Ошибка перемещения/переименования директории: не удалось найти ресурс"));
        resourceStorage.deleteDirectory(oldPath);
        return resourceItem;
    }

    private void validateMovement(String oldPath, String newPath) {
        String oldParentDirectory = formatParentDirectory(oldPath);
        String newParentDirectory = formatParentDirectory(newPath);
        String oldName = oldPath.substring(oldParentDirectory.length());
        String newName = newPath.substring(newParentDirectory.length());
        if (oldParentDirectory.equals(newParentDirectory) && !oldName.equals(newName)) {
            return;
        }
        if (!oldParentDirectory.equals(newParentDirectory) && oldName.equals(newName)) {
            return;
        }
        throw new InvalidInputException("Некорректный ввод пользователя: операция невалидна");
    }

    private List<ResourceItem> createSubdirectories(String originalFilename, String path) {
        List<ResourceItem> newSubdirectories = new ArrayList<>();
        List<String> resources = Arrays.stream(originalFilename.split(SEPARATOR_SIGN)).collect(Collectors.toList());
        resources.removeLast();
        StringBuilder pathBuilder = new StringBuilder(path);
        for (String resource : resources) {
            pathBuilder.append(resource).append(SEPARATOR_SIGN);
            if (resourceStorage.findDirectory(pathBuilder.toString()).isEmpty()) {
                resourceStorage.createDirectory(pathBuilder.toString());
                newSubdirectories.add(new ResourceItem(pathBuilder.toString(), true, null));
            }
        }
        return newSubdirectories;
    }

    private boolean isResourceExists(String path) {
        if (isDirectory(path)) {
            return resourceStorage.findDirectory(path).isPresent();
        }
        return resourceStorage.findFile(path).isPresent();
    }

    private boolean matchQueryWithLowerCase(String name, String query) {
        name = name.toLowerCase();
        query = query.toLowerCase();
        return name.contains(query);
    }

    private String formatPersonalPath(String path) {
        String personalDirectory = PERSONAL_DIRECTORY_NAME.formatted(userContext.getUserId());
        if (path.equals(SEPARATOR_SIGN)) {
            return personalDirectory;
        }
        return personalDirectory + path;
    }

    private String formatParentDirectory(String fullPath) {
        String[] directories = fullPath.split(SEPARATOR_SIGN);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < directories.length - 1; i++) {
            stringBuilder.append(directories[i]).append(SEPARATOR_SIGN);
        }
        return stringBuilder.toString();
    }

    private ResponseResourceDto convert(ResourceItem item) {
        String[] resources = item.objectName().split(SEPARATOR_SIGN);
        String path = collectPath(resources);
        String name = resources[resources.length - 1];
        if (isDirectory(item.objectName()) || item.isDir()) {
            return new ResponseResourceDto(path, name, null, ResourceType.DIRECTORY);
        }
        return new ResponseResourceDto(path, name, item.size(), ResourceType.FILE);
    }

    private String collectPath(String[] resources) {
        StringBuilder path = new StringBuilder();
        for (int i = 1; i < resources.length - 1; i++) {
            path.append(resources[i]).append(SEPARATOR_SIGN);
        }
        return path.toString();
    }

    private boolean isDirectory(String path) {
        return path.endsWith(SEPARATOR_SIGN);
    }
}