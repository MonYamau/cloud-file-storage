package ru.monyamau.cloudfilestorage.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.domain.ResourceItem;
import ru.monyamau.cloudfilestorage.domain.ResourceType;
import ru.monyamau.cloudfilestorage.dto.event.UserRegistrationEventDto;
import ru.monyamau.cloudfilestorage.dto.request.*;
import ru.monyamau.cloudfilestorage.dto.response.ResponseDownloadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.exception.InvalidInputException;
import ru.monyamau.cloudfilestorage.exception.ResourceAlreadyExistsException;
import ru.monyamau.cloudfilestorage.exception.ResourceNotFoundException;
import ru.monyamau.cloudfilestorage.model.ResourceItem;
import ru.monyamau.cloudfilestorage.model.ResourceType;
import ru.monyamau.cloudfilestorage.handler.UserContext;
import ru.monyamau.cloudfilestorage.infrastructure.ResourceStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ResourceService {
    private final static String PERSONAL_DIRECTORY_NAME = "user-%s-files/";
    private final static String SEPARATOR_SIGN = "/";

    private final ResourceStorage resourceStorage;
    private final UserContext userContext;

    public ResourceService(ResourceStorage resourceStorage, UserContext userContext) {
        this.resourceStorage = resourceStorage;
        this.userContext = userContext;
    }

    @EventListener
    public void createPersonalDirectory(UserRegistrationEventDto eventDto) {
        String directoryName = PERSONAL_DIRECTORY_NAME.formatted(eventDto.userId());
        if (resourceStorage.findResource(directoryName).isEmpty()) {
            resourceStorage.createDirectory(directoryName);
        }
    }

    public List<ResponseResourceDto> findAllFromDirectory(RequestDirectoryDto directoryDto) {
        String fullPath = formatPersonalPath(directoryDto.path());
        String personalDirectoryName = formatPersonalDirectoryName();
        checkExistenceOfResource(fullPath, directoryDto.path());
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
        checkExistenceOfResource(extractParentDirectory(fullPath), extractParentDirectory(directoryDto.path()));
        checkNonexistenceOfResource(fullPath, directoryDto.path());
        resourceStorage.createDirectory(fullPath);
        ResourceItem item = resourceStorage.findDirectory(fullPath)
                .orElseThrow(() -> new IllegalStateException("Ошибка создания директории: не удалось найти ресурс"));
        return convert(item);
    }

    public ResponseResourceDto findResource(RequestResourceDto resourceDto) {
        String fullPath = formatPersonalPath(resourceDto.path());
        if (formatPersonalDirectory().equals(fullPath)) {
            return new ResponseResourceDto("", "", null, ResourceType.DIRECTORY);
        }
        if (isDirectory(fullPath)) {
            ResourceItem directory = resourceStorage.findDirectory(fullPath)
                    .orElseThrow(() -> new ResourceNotFoundException("Директория с текущим именем не найдена: " + resourceDto.path()));
            return convert(directory);
        }
        ResourceItem file = resourceStorage.findFile(fullPath)
                .orElseThrow(() -> new ResourceNotFoundException("Файл с текущим именем не найден: " + resourceDto.path()));
        return convert(file);
    }

    public List<ResponseResourceDto> searchResource(RequestQueryDto queryDto) {
        String personalDirectoryName = formatPersonalDirectoryName();
        List<ResponseResourceDto> result = new ArrayList<>();
        List<ResourceItem> resources = resourceStorage.findAllByPrefix(formatPersonalDirectory());
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
        checkExistenceOfResource(fullPath, resourceDto.path());
        if (isDirectory(fullPath)) {
            resourceStorage.deleteDirectory(fullPath);
        } else {
            resourceStorage.deleteFile(fullPath);
        }
    }

    public List<ResponseResourceDto> uploadResource(RequestUploadDto uploadDto) {
        String fullPath = formatPersonalPath(uploadDto.path());
        checkExistenceOfResource(fullPath, uploadDto.path());
        for (MultipartFile multipartFile : uploadDto.files()) {
            String originalFilename = multipartFile.getOriginalFilename();
            checkNonexistenceOfResource(fullPath + originalFilename, uploadDto.path() + originalFilename);
        }
        List<ResourceItem> resourceItemList = uploadFiles(fullPath, uploadDto.files());
        return resourceItemList.stream().map(this::convert).toList();
    }

    public ResponseDownloadDto downloadResource(RequestResourceDto resourceDto) {
        String fullPath = formatPersonalPath(resourceDto.path());
        checkExistenceOfResource(fullPath, resourceDto.path());
        String filename = extractFilename(fullPath);
        return isDirectory(fullPath) ?
                downloadDirectory(fullPath, filename)
                : downloadFile(fullPath, filename);
    }

    public ResponseResourceDto changeResource(RequestMovementDto movementDto) {
        String oldPath = formatPersonalPath(movementDto.from());
        String newPath = formatPersonalPath(movementDto.to());
        validateMovement(oldPath, newPath);
        checkExistenceOfResource(oldPath, movementDto.from());
        checkExistenceOfResource(extractParentDirectory(newPath), extractParentDirectory(movementDto.to()));
        checkNonexistenceOfResource(newPath, movementDto.to());
        ResourceItem resourceItem = isDirectory(oldPath) && isDirectory(newPath) ?
                changeDirectory(oldPath, newPath)
                : changeFile(oldPath, newPath);
        return convert(resourceItem);
    }

    private ResponseDownloadDto downloadFile(String path, String filename) {
        try (InputStream inputStream = resourceStorage.download(path)) {
            return new ResponseDownloadDto(filename, inputStream.readAllBytes());
        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
    }

    private ResponseDownloadDto downloadDirectory(String path, String filename) {
        List<ResourceItem> resourceItemList = resourceStorage.findAllByPrefix(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            writeDirectoryContentsToZip(zipOutputStream, resourceItemList, path);
        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
        return new ResponseDownloadDto(filename + ".zip", byteArrayOutputStream.toByteArray());
    }

    private void writeDirectoryContentsToZip(ZipOutputStream zipOutputStream, List<ResourceItem> resourceItemList, String path) throws IOException {
        for (ResourceItem resourceItem : resourceItemList) {
            String fullObjectName = resourceItem.objectName();
            if (isDirectory(fullObjectName)) continue;
            zipOutputStream.putNextEntry(new ZipEntry(fullObjectName.replace(path, "")));
            try (InputStream inputStream = resourceStorage.download(resourceItem.objectName())) {
                inputStream.transferTo(zipOutputStream);
            }
            zipOutputStream.closeEntry();
        }
    }

    private ResourceItem changeFile(String oldName, String newName) {
        String copy = resourceStorage.copy(oldName, newName);
        ResourceItem resourceItem = resourceStorage.findFile(copy)
                .orElseThrow(() -> new IllegalStateException("Ошибка перемещения/переименования файла: не удалось найти ресурс"));
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
                .orElseThrow(() -> new IllegalStateException("Ошибка перемещения/переименования директории: не удалось найти ресурс"));
        resourceStorage.deleteDirectory(oldPath);
        return resourceItem;
    }

    private List<ResourceItem> uploadFiles(String path, List<MultipartFile> files) {
        List<ResourceItem> allResources = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            if (filename == null || isDirectory(filename)) continue;
            List<ResourceItem> subdirectories = createSubdirectories(filename, path, allResources);
            String uploaded = resourceStorage.upload(path + filename, file);
            ResourceItem resourceItem = resourceStorage.findFile(uploaded)
                    .orElseThrow(() -> new IllegalStateException("Ошибка сохранения файла: не удалось найти ресурс"));
            allResources.addAll(subdirectories);
            allResources.add(resourceItem);
        }
        return allResources;
    }

    private void validateMovement(String oldPath, String newPath) {
        String oldParentDirectory = extractParentDirectory(oldPath);
        String newParentDirectory = extractParentDirectory(newPath);
        String oldName = oldPath.substring(oldParentDirectory.length());
        String newName = newPath.substring(newParentDirectory.length());
        boolean isRenaming = oldParentDirectory.equals(newParentDirectory) && !oldName.equals(newName);
        boolean isMovement = !oldParentDirectory.equals(newParentDirectory) && oldName.equals(newName);
        if (!isRenaming && !isMovement) {
            throw new InvalidInputException("Некорректный ввод пользователя: операция невалидна");
        }
    }

    private List<ResourceItem> createSubdirectories(String filename, String path, List<ResourceItem> createdResources) {
        List<ResourceItem> newSubdirectories = new ArrayList<>();
        List<String> resources = Arrays.stream(filename.split(SEPARATOR_SIGN)).collect(Collectors.toList());
        resources.removeLast();
        StringBuilder pathBuilder = new StringBuilder(path);
        for (String resource : resources) {
            pathBuilder.append(resource).append(SEPARATOR_SIGN);
            String fullResourcePath = pathBuilder.toString();
            if (createdResources.contains(new ResourceItem(fullResourcePath, true, null))) continue;
            if (resourceStorage.findDirectory(fullResourcePath).isEmpty()) {
                resourceStorage.createDirectory(fullResourcePath);
                newSubdirectories.add(new ResourceItem(fullResourcePath, true, null));
            }
        }
        return newSubdirectories;
    }

    private void checkExistenceOfResource(String path, String formattedPath) {
        if (!isResourceExists(path)) {
            throw new ResourceNotFoundException("Ресурс по данному пути не найден: " + formattedPath);
        }
    }

    private void checkNonexistenceOfResource(String path, String formattedPath) {
        if (isResourceExists(path)) {
            throw new ResourceAlreadyExistsException("Ресурс по данному пути уже существует: " + formattedPath);
        }
    }

    private boolean isResourceExists(String path) {
        return isDirectory(path) ?
                resourceStorage.findDirectory(path).isPresent()
                : resourceStorage.findFile(path).isPresent();
    }

    private String formatPersonalPath(String path) {
        return path.equals(SEPARATOR_SIGN)
                ? formatPersonalDirectory()
                : formatPersonalDirectory() + path;
    }

    private String extractFilename(String fullPath) {
        String[] resources = fullPath.split(SEPARATOR_SIGN);
        return resources[resources.length - 1];
    }

    private String extractParentDirectory(String fullPath) {
        String[] resources = fullPath.split(SEPARATOR_SIGN);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < resources.length - 1; i++) {
            stringBuilder.append(resources[i]).append(SEPARATOR_SIGN);
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

    private boolean matchQueryWithLowerCase(String name, String query) {
        return name.toLowerCase().contains(query.toLowerCase());
    }

    private String formatPersonalDirectory() {
        return PERSONAL_DIRECTORY_NAME.formatted(userContext.getUserId());
    }

    private String formatPersonalDirectoryName() {
        return formatPersonalDirectory().replace(SEPARATOR_SIGN, "");
    }

    private boolean isDirectory(String path) {
        return path.endsWith(SEPARATOR_SIGN);
    }
}