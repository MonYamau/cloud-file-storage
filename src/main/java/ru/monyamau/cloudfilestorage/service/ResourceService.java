package ru.monyamau.cloudfilestorage.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.monyamau.cloudfilestorage.domain.ResourceItem;
import ru.monyamau.cloudfilestorage.domain.ResourcePath;
import ru.monyamau.cloudfilestorage.domain.ResourceType;
import ru.monyamau.cloudfilestorage.dto.request.RequestDirectoryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestMovementDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestQueryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestResourceDto;
import ru.monyamau.cloudfilestorage.dto.request.service.UploadCommand;
import ru.monyamau.cloudfilestorage.dto.request.service.UploadedFile;
import ru.monyamau.cloudfilestorage.dto.response.ResponseDownloadDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.exception.InvalidInputException;
import ru.monyamau.cloudfilestorage.exception.ResourceAlreadyExistsException;
import ru.monyamau.cloudfilestorage.exception.ResourceNotFoundException;
import ru.monyamau.cloudfilestorage.infrastructure.ResourceStorage;
import ru.monyamau.cloudfilestorage.mapper.ResourceItemMapper;
import ru.monyamau.cloudfilestorage.util.ArchiveUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Service
public class ResourceService {
    private final static String PERSONAL_DIRECTORY_NAME = "user-%s-files/";
    private final static String ARCHIVE_FORMAT = ".zip";
    private final static String SEPARATOR_SIGN = "/";
    private final static String DEFAULT_RESOURCE_NAME = "download";

    private final ResourceStorage resourceStorage;
    private final ResourceItemMapper resourceItemMapper;
    private final Validator validator;

    @Autowired
    public ResourceService(ResourceStorage resourceStorage, ResourceItemMapper resourceItemMapper, Validator validator) {
        this.resourceStorage = resourceStorage;
        this.resourceItemMapper = resourceItemMapper;
        this.validator = validator;
    }

    public List<ResponseResourceDto> findAllFromDirectory(RequestDirectoryDto directoryDto, Integer userId) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(userId), directoryDto.path());
        String fullPath = path.getFullPath();
        checkExistenceOfResource(fullPath, userId);
        List<ResponseResourceDto> result = new ArrayList<>();
        List<ResourceItem> resources = resourceStorage.findAllFromDirectory(fullPath);
        for (ResourceItem resource : resources) {
            if (resource.objectName().equals(fullPath)) continue;
            result.add(resourceItemMapper.toDto(resource, path.personalDirectory()));
        }
        return result;
    }

    public ResponseResourceDto createDirectory(RequestDirectoryDto directoryDto, Integer userId) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(userId), directoryDto.path());
        checkExistenceOfResource(path.getParentDirectoryWithPersonalDirectory(), userId);
        checkNonexistenceOfResource(path.getFullPath(), userId);
        resourceStorage.createDirectory(path.getFullPath());
        ResourceItem item = resourceStorage.findResource(path.getFullPath())
                .orElseThrow(() -> new IllegalStateException("Ошибка создания директории: не удалось найти ресурс " + path.getFullPath()));
        log.info("Create directory {} for user with Id:{}", path.getFullPath(), userId);
        return resourceItemMapper.toDto(item, path.personalDirectory());
    }

    public ResponseResourceDto findResource(RequestResourceDto resourceDto, Integer userId) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(userId), resourceDto.path());
        if (path.isPersonalDirectory()) {
            return new ResponseResourceDto("", "", null, ResourceType.DIRECTORY);
        }
        ResourceItem resource = resourceStorage.findResource(path.getFullPath())
                .orElseThrow(() -> new ResourceNotFoundException("Ресурс с текущим именем не найден: " + path.path()));
        return resourceItemMapper.toDto(resource, path.personalDirectory());
    }

    public List<ResponseResourceDto> searchResource(RequestQueryDto queryDto, Integer userId) {
        String personalDirectoryName = formatPersonalDirectoryName(userId);
        String personalDirectory = formatPersonalDirectory(userId);
        List<ResponseResourceDto> result = new ArrayList<>();
        List<ResourceItem> resources = resourceStorage.findAllByPrefix(personalDirectory);
        for (ResourceItem resource : resources) {
            ResponseResourceDto converted = resourceItemMapper.toDto(resource, personalDirectory);
            if (personalDirectoryName.equals(converted.name())) continue;
            if (matchQueryWithLowerCase(converted.name(), queryDto.query())) {
                result.add(converted);
            }
        }
        return result;
    }

    public void deleteResource(RequestResourceDto resourceDto, Integer userId) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(userId), resourceDto.path());
        if (path.isPersonalDirectory()) {
            throw new InvalidInputException("Ошибка удаления: нельзя удалить пользовательскую директорию");
        }
        checkExistenceOfResource(path.getFullPath(), userId);
        resourceStorage.deleteResource(path.getFullPath());
        log.info("Delete resource {} for user with Id:{}", path.getFullPath(), userId);
    }

    public List<ResponseResourceDto> uploadResource(UploadCommand uploadCommand, Integer userId) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(userId), uploadCommand.path());
        checkExistenceOfResource(path.getFullPath(), userId);
        HashSet<Object> uniqueNames = new HashSet<>();
        for (UploadedFile file : uploadCommand.files()) {
            String filename = file.filename();
            if (!uniqueNames.add(filename)) {
                throw new InvalidInputException("Ошибка загрузки: нельзя загрузить более одного файла с данным именем "
                        + filename);
            }
            validateUploadedFilename(filename);
            checkNonexistenceOfResource(path.getFullPath() + filename, userId);
        }
        List<ResourceItem> resourceItemList = uploadFiles(path.getFullPath(), uploadCommand.files());
        log.info("Upload {} resources into {} path for user with Id:{}",
                resourceItemList.size(), path.getFullPath(), userId);
        return resourceItemList.stream()
                .map(resource -> resourceItemMapper.toDto(resource, path.personalDirectory()))
                .toList();
    }

    public ResponseResourceDto changeResource(RequestMovementDto movementDto, Integer userId) {
        ResourcePath oldPath = new ResourcePath(formatPersonalDirectory(userId), movementDto.from());
        ResourcePath newPath = new ResourcePath(formatPersonalDirectory(userId), movementDto.to());
        validateMovement(oldPath, newPath);
        checkExistenceOfResource(oldPath.getFullPath(), userId);
        checkExistenceOfResource(newPath.getParentDirectoryWithPersonalDirectory(), userId);
        checkNonexistenceOfResource(newPath.getFullPath(), userId);
        resourceStorage.moveResource(oldPath.getFullPath(), newPath.getFullPath());
        ResourceItem resourceItem = resourceStorage.findResource(newPath.getFullPath()).orElseThrow(
                () -> new IllegalStateException("Ошибка перемещения/переименования: не удалось найти ресурс " + newPath.getFullPath()));
        log.info("Move/rename resource from {} to {} for user with Id:{}",
                oldPath.getFullPath(), newPath.getFullPath(), userId);
        return resourceItemMapper.toDto(resourceItem, newPath.personalDirectory());
    }

    public ResponseDownloadDto downloadResource(RequestResourceDto resourceDto, Integer userId) {
        ResourcePath path = new ResourcePath(formatPersonalDirectory(userId), resourceDto.path());
        checkExistenceOfResource(path.getFullPath(), userId);
        String resourceName = path.isPersonalDirectory() ? DEFAULT_RESOURCE_NAME : path.getResourceName();
        return path.isDirectory() ?
                downloadDirectory(path.getFullPath(), resourceName, userId)
                : downloadFile(path.getFullPath(), resourceName, userId);
    }

    private List<ResourceItem> uploadFiles(String path, List<UploadedFile> files) {
        List<ResourceItem> allResources = new ArrayList<>();
        for (UploadedFile file : files) {
            String filename = file.filename();
            if (filename == null || filename.endsWith(SEPARATOR_SIGN)) continue;
            String filePath = resourceStorage
                    .uploadResource(path + filename, file.inputStream(), file.size(), file.contentType());
            allResources.add(new ResourceItem(filePath, false, file.size()));
        }
        return allResources;
    }

    private ResponseDownloadDto downloadFile(String fullPath, String filename, Integer userId) {
        Consumer<OutputStream> performer = outputStream -> {
            try (InputStream inputStream = resourceStorage.downloadResource(fullPath)) {
                inputStream.transferTo(outputStream);
            } catch (IOException e) {
                throw new IllegalStateException("Ошибка загрузки: не удалось скачать ресурс " + fullPath, e);
            }
        };
        log.info("Download file from {} for user with Id:{}", fullPath, userId);
        return new ResponseDownloadDto(filename, performer);
    }

    private ResponseDownloadDto downloadDirectory(String fullPath, String directoryName, Integer userId) {
        List<ResourceItem> resourceItemList = resourceStorage.findAllByPrefix(fullPath);
        Consumer<OutputStream> performer = outputStream -> {
            try {
                ArchiveUtil
                        .archiveItemsToZip(resourceItemList, outputStream, fullPath, resourceStorage::downloadResource);
            } catch (IOException e) {
                throw new IllegalStateException("Ошибка загрузки: не удалось скачать архив " + fullPath, e);
            }
        };
        log.info("Download archive from {} for user with Id:{}", fullPath, userId);
        return new ResponseDownloadDto(directoryName + ARCHIVE_FORMAT, performer);
    }

    private void validateMovement(ResourcePath oldPath, ResourcePath newPath) {
        if (oldPath.path().equals(newPath.path())) {
            throw new InvalidInputException("Ошибка перемещения/переименования: ресурс " + newPath.path() + " уже существует по пути назначения");
        }
        if (newPath.path().startsWith(oldPath.path()) && oldPath.isDirectory()) {
            throw new InvalidInputException("Ошибка перемещения/переименования: директорию " + newPath.path() + " нельзя перенести в свои поддиректории");
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

    private void validateUploadedFilename(String filename) {
        Set<ConstraintViolation<RequestResourceDto>> violations = validator.validate(new RequestResourceDto(filename));
        if (!violations.isEmpty()) {
            throw new InvalidInputException("Имя загружаемого файла " + filename + " содержит недопустимые символы");
        }
    }

    private void checkExistenceOfResource(String pathWithPersonalDirectory, Integer userId) {
        if (!isResourceExists(pathWithPersonalDirectory, userId)) {
            String path = pathWithPersonalDirectory.substring(formatPersonalDirectory(userId).length());
            throw new ResourceNotFoundException("Ресурс по данному пути не найден: " + path);
        }
    }

    private void checkNonexistenceOfResource(String pathWithPersonalDirectory, Integer userId) {
        if (isResourceExists(pathWithPersonalDirectory, userId)) {
            String path = pathWithPersonalDirectory.substring(formatPersonalDirectory(userId).length());
            throw new ResourceAlreadyExistsException("Ресурс по данному пути уже существует: " + path);
        }
    }

    private boolean isResourceExists(String path, Integer userId) {
        if (path.equals(formatPersonalDirectory(userId))) {
            return true;
        }
        return resourceStorage.findResource(path).isPresent();
    }

    private boolean matchQueryWithLowerCase(String name, String query) {
        return name.toLowerCase().contains(query.toLowerCase());
    }

    private String formatPersonalDirectory(Integer userId) {
        return PERSONAL_DIRECTORY_NAME.formatted(userId);
    }

    private String formatPersonalDirectoryName(Integer userId) {
        return formatPersonalDirectory(userId).replace(SEPARATOR_SIGN, "");
    }
}