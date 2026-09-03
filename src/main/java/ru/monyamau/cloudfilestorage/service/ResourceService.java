package ru.monyamau.cloudfilestorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.domain.ResourceItem;
import ru.monyamau.cloudfilestorage.domain.ResourcePath;
import ru.monyamau.cloudfilestorage.domain.ResourceType;
import ru.monyamau.cloudfilestorage.dto.event.UserRegistrationEventDto;
import ru.monyamau.cloudfilestorage.dto.request.*;
import ru.monyamau.cloudfilestorage.dto.response.ResponseDownloadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.exception.InvalidInputException;
import ru.monyamau.cloudfilestorage.exception.ResourceAlreadyExistsException;
import ru.monyamau.cloudfilestorage.exception.ResourceNotFoundException;
import ru.monyamau.cloudfilestorage.handler.UserContext;
import ru.monyamau.cloudfilestorage.infrastructure.ResourceStorage;
import ru.monyamau.cloudfilestorage.mapper.ResourceItemMapper;
import ru.monyamau.cloudfilestorage.util.ArchiveUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceService {
    private final static String PERSONAL_DIRECTORY_NAME = "user-%s-files/";
    private final static String ARCHIVE_FORMAT = ".zip";
    private final static String SEPARATOR_SIGN = "/";

    private final ResourceStorage resourceStorage;
    private final UserContext userContext;
    private final ResourceItemMapper resourceItemMapper;

    @Autowired
    public ResourceService(ResourceStorage resourceStorage, UserContext userContext, ResourceItemMapper resourceItemMapper) {
        this.resourceStorage = resourceStorage;
        this.userContext = userContext;
        this.resourceItemMapper = resourceItemMapper;
    }

    @EventListener
    public void createPersonalDirectory(UserRegistrationEventDto eventDto) {
        String directoryName = PERSONAL_DIRECTORY_NAME.formatted(eventDto.userId());
        if (resourceStorage.findResource(directoryName).isEmpty()) {
            resourceStorage.createDirectory(directoryName);
        }
    }

    public List<ResponseResourceDto> findAllFromDirectory(RequestDirectoryDto directoryDto) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(), directoryDto.path());
        String personalDirectoryName = formatPersonalDirectoryName();
        checkExistenceOfResource(path.getFullPath());
        List<ResponseResourceDto> result = new ArrayList<>();
        List<ResourceItem> resources = resourceStorage.findAllFromDirectory(path.getFullPath());
        for (ResourceItem resource : resources) {
            ResponseResourceDto converted = resourceItemMapper.toDto(resource);
            if (personalDirectoryName.equals(converted.name())) continue;
            result.add(converted);
        }
        return result;
    }

    public ResponseResourceDto createDirectory(RequestDirectoryDto directoryDto) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(), directoryDto.path());
        checkExistenceOfResource(path.getParentDirectoryWithPersonalDirectory());
        checkNonexistenceOfResource(path.getFullPath());
        resourceStorage.createDirectory(path.getFullPath());
        ResourceItem item = resourceStorage.findResource(path.getFullPath())
                .orElseThrow(() -> new IllegalStateException("Ошибка создания директории: не удалось найти ресурс"));
        return resourceItemMapper.toDto(item);
    }

    public ResponseResourceDto findResource(RequestResourceDto resourceDto) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(), resourceDto.path());
        if (path.isPersonalDirectory()) {
            return new ResponseResourceDto("", "", null, ResourceType.DIRECTORY);
        }
        ResourceItem resource = resourceStorage.findResource(path.getFullPath())
                .orElseThrow(() -> new ResourceNotFoundException("Ресурс с текущим именем не найден: " + path.path()));
        return resourceItemMapper.toDto(resource);
    }

    public List<ResponseResourceDto> searchResource(RequestQueryDto queryDto) {
        String personalDirectoryName = formatPersonalDirectoryName();
        List<ResponseResourceDto> result = new ArrayList<>();
        List<ResourceItem> resources = resourceStorage.findAllByPrefix(formatPersonalDirectory());
        for (ResourceItem resource : resources) {
            ResponseResourceDto converted = resourceItemMapper.toDto(resource);
            if (personalDirectoryName.equals(converted.name())) continue;
            if (matchQueryWithLowerCase(converted.name(), queryDto.query())) {
                result.add(converted);
            }
        }
        return result;
    }

    public void deleteResource(RequestResourceDto resourceDto) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(), resourceDto.path());
        checkExistenceOfResource(path.getFullPath());
        resourceStorage.deleteResource(path.getFullPath());
    }

    public List<ResponseResourceDto> uploadResource(RequestUploadDto uploadDto) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(), uploadDto.path());
        checkExistenceOfResource(path.getFullPath());
        for (MultipartFile multipartFile : uploadDto.files()) {
            String filename = multipartFile.getOriginalFilename();
            checkNonexistenceOfResource(path.getFullPath() + filename);
        }
        List<ResourceItem> resourceItemList = uploadFiles(path.getFullPath(), uploadDto.files());
        return resourceItemList.stream().map(resourceItemMapper::toDto).toList();
    }

    public ResponseResourceDto changeResource(RequestMovementDto movementDto) {
        ResourcePath oldPath = new ResourcePath(formatPersonalDirectory(), movementDto.from());
        ResourcePath newPath = new ResourcePath(formatPersonalDirectory(), movementDto.to());
        validateMovement(oldPath, newPath);
        checkExistenceOfResource(oldPath.getFullPath());
        checkExistenceOfResource(newPath.getParentDirectoryWithPersonalDirectory());
        checkNonexistenceOfResource(newPath.getFullPath());
        resourceStorage.moveResource(oldPath.getFullPath(), newPath.getFullPath());
        ResourceItem resourceItem = resourceStorage.findResource(newPath.getFullPath()).orElseThrow(
                () -> new IllegalStateException("Ошибка перемещения/переименования: не удалось найти ресурс"));
        return resourceItemMapper.toDto(resourceItem);
    }

    public ResponseDownloadDto downloadResource(RequestResourceDto resourceDto) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(), resourceDto.path());
        checkExistenceOfResource(path.getFullPath());
        try {
            if (path.isDirectory()) {
                List<ResourceItem> resourceItemList = resourceStorage.findAllByPrefix(path.getFullPath());
                ByteArrayOutputStream outputStream = ArchiveUtil
                        .archiveItemsToZip(resourceItemList, path.getFullPath(), resourceStorage::downloadResource);
                return new ResponseDownloadDto(path.getResourceName() + ARCHIVE_FORMAT, outputStream.toByteArray());
            }
            InputStream inputStream = resourceStorage.downloadResource(path.getFullPath());
            return new ResponseDownloadDto(path.getResourceName(), inputStream.readAllBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка загрузки: не удалось скачать ресурс", e);
        }
    }

    private List<ResourceItem> uploadFiles(String path, List<MultipartFile> files) {
        List<ResourceItem> allResources = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String filename = file.getOriginalFilename();
                if (filename == null || filename.endsWith(SEPARATOR_SIGN)) continue;
                String filePath = resourceStorage
                        .uploadResource(path + filename, file.getInputStream(), file.getSize(), file.getContentType());
                allResources.add(new ResourceItem(filePath, false, file.getSize()));
            }
            return allResources;
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка загрузки: не удалось сохранить ресурс", e);
        }
    }

    private void validateMovement(ResourcePath oldPath, ResourcePath newPath) {
        if (oldPath.path().equals(newPath.path())) {
            throw new InvalidInputException("Ошибка перемещения/переименования: ресурс уже существует по пути назначения");
        }
        if (newPath.path().startsWith(oldPath.path())) {
            throw new InvalidInputException("Ошибка перемещения/переименования: директорию нельзя перенести в свои поддиректории");
        }
        String oldParentDirectory = oldPath.getParentDirectoryWithoutPersonalDirectory();
        String newParentDirectory = newPath.getParentDirectoryWithoutPersonalDirectory();
        String oldName = oldPath.getResourceName();
        String newName = newPath.getResourceName();
        boolean isRenaming = oldParentDirectory.equals(newParentDirectory) && !oldName.equals(newName);
        boolean isMovement = !oldParentDirectory.equals(newParentDirectory) && oldName.equals(newName);
        if (!isRenaming && !isMovement) {
            throw new InvalidInputException("Ошибка перемещения/переименования: операция невалидна");
        }
    }

    private void checkExistenceOfResource(String pathWithPersonalDirectory) {
        if (!isResourceExists(pathWithPersonalDirectory)) {
            String path = pathWithPersonalDirectory.substring(formatPersonalDirectory().length());
            throw new ResourceNotFoundException("Ресурс по данному пути не найден: " + path);
        }
    }

    private void checkNonexistenceOfResource(String pathWithPersonalDirectory) {
        if (isResourceExists(pathWithPersonalDirectory)) {
            String path = pathWithPersonalDirectory.substring(formatPersonalDirectory().length());
            throw new ResourceAlreadyExistsException("Ресурс по данному пути уже существует: " + path);
        }
    }

    private boolean isResourceExists(String path) {
        return resourceStorage.findResource(path).isPresent();
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
}