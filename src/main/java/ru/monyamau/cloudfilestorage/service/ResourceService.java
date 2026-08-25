package ru.monyamau.cloudfilestorage.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.dto.request.RequestDirectoryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestQueryDto;
import ru.monyamau.cloudfilestorage.dto.request.RequestResourceDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.model.ResourceItem;
import ru.monyamau.cloudfilestorage.model.ResourceType;
import ru.monyamau.cloudfilestorage.repository.MinioResourceStorage;
import ru.monyamau.cloudfilestorage.util.UserContext;

import java.io.ByteArrayOutputStream;
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

    public ResponseResourceDto findResource(RequestResourceDto resourceDto) {
        String fullPath = formatPersonalPath(resourceDto.path());
        if (isDirectory(fullPath)) {
            ResourceItem directory = resourceStorage.findDirectory(fullPath).orElseThrow(RuntimeException::new);
            return convert(directory);
        }
        ResourceItem file = resourceStorage.findFile(fullPath).orElseThrow(RuntimeException::new);
        return convert(file);
    }

    public List<ResponseResourceDto> searchResource(RequestQueryDto queryDto) {
        String personalDirectory = PERSONAL_DIRECTORY_NAME.formatted(userContext.getUserId());
        String personalDirectoryName = personalDirectory.replace("/", "");
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
        if (isDirectory(fullPath)) {
            resourceStorage.deleteDirectory(fullPath);
        }
        resourceStorage.deleteFile(fullPath);
    }

    public ResponseResourceDto uploadResource(RequestResourceDto resourceDto, MultipartFile file) {
        String fullPath = formatPersonalPath(resourceDto.path());
        if (file.getOriginalFilename().contains("/")) {
            createSubdirectories(file.getOriginalFilename(), fullPath);
        }
        String filePath = fullPath + file.getOriginalFilename();
        String resourcePath = resourceStorage.upload(filePath, file);
        ResourceItem resource = resourceStorage.findFile(resourcePath).orElseThrow(RuntimeException::new);
        return convert(resource);
    }

    public byte[] downloadResource(RequestResourceDto resourceDto) {
        String fullPath = formatPersonalPath(resourceDto.path());
        List<ResourceItem> resourceItemList = resourceStorage.findAllByPrefix(fullPath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (ResourceItem resourceItem : resourceItemList) {
                String fullObjectName = resourceItem.objectName();
                if (isDirectory(fullObjectName)) continue;
                zipOutputStream.putNextEntry(new ZipEntry(fullObjectName.replace(fullPath, "")));
                InputStream inputStream = resourceStorage.download(resourceItem.objectName());
                inputStream.transferTo(zipOutputStream);
                zipOutputStream.closeEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public ResponseResourceDto changeResource(String oldName, String newName) {
        String oldPath = formatPersonalPath(oldName);
        String newPath = formatPersonalPath(newName);
        if (isDirectory(oldPath) && isDirectory(newPath)) {
            ResourceItem resourceItem = changeDirectory(oldPath, newPath);
            return convert(resourceItem);
        }
        ResourceItem resourceItem = changeFile(oldPath, newPath);
        return convert(resourceItem);
    }

    public List<ResponseResourceDto> findAllFromDirectory(RequestDirectoryDto directoryDto) {
        String fullPath = formatPersonalPath(directoryDto.path());
        List<ResponseResourceDto> result = new ArrayList<>();
        List<ResourceItem> resources = resourceStorage.findAllFromDirectory(fullPath);
        for (ResourceItem resource : resources) {
            result.add(convert(resource));
        }
        return result;
    }

    public ResponseResourceDto createDirectory(RequestDirectoryDto directoryDto) {
        String fullPath = formatPersonalPath(directoryDto.path());
        resourceStorage.createDirectory(fullPath);
        ResourceItem item = resourceStorage.findDirectory(fullPath).orElseThrow(RuntimeException::new);
        return convert(item);
    }

    private String formatPersonalPath(String path) {
        String personalDirectory = PERSONAL_DIRECTORY_NAME.formatted(userContext.getUserId());
        return personalDirectory + path;
    }

    private ResourceItem changeFile(String oldName, String newName) {
        String copy = resourceStorage.copy(oldName, newName);
        ResourceItem resourceItem = resourceStorage.findFile(copy).orElseThrow(RuntimeException::new);
        resourceStorage.deleteFile(oldName);
        return resourceItem;
    }

    private ResourceItem changeDirectory(String oldPath, String newPath) {
        List<ResourceItem> resourceItemList = resourceStorage.findAllByPrefix(oldPath);
        for (ResourceItem resourceItem : resourceItemList) {
            String oldObjectPath = resourceItem.objectName();
            String newObjectPath = oldObjectPath.replace(oldPath, newPath);
            resourceStorage.copy(oldObjectPath, newObjectPath);
        }
        resourceStorage.deleteDirectory(oldPath);
        return resourceStorage.findDirectory(newPath).orElseThrow(RuntimeException::new);
    }

    private void createSubdirectories(String originalFilename, String path) {
        List<String> resources = Arrays.stream(originalFilename.split("/")).collect(Collectors.toList());
        resources.removeLast();
        StringBuilder pathBuilder = new StringBuilder(path);
        for (String resource : resources) {
            pathBuilder.append(resource).append("/");
            resourceStorage.createDirectory(pathBuilder.toString());
        }
    }

    private boolean matchQueryWithLowerCase(String name, String query) {
        name = name.toLowerCase();
        query = query.toLowerCase();
        return name.contains(query);
    }

    private ResponseResourceDto convert(ResourceItem item) {
        String[] resources = item.objectName().split("/");
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
            path.append(resources[i]).append("/");
        }
        return path.toString();
    }

    private boolean isDirectory(String path) {
        return path.endsWith("/");
    }
}
