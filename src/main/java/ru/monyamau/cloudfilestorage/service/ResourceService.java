package ru.monyamau.cloudfilestorage.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.dto.response.ResponseResourceDto;
import ru.monyamau.cloudfilestorage.model.ResourceItem;
import ru.monyamau.cloudfilestorage.model.ResourceType;
import ru.monyamau.cloudfilestorage.repository.MinioResourceStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResourceService {
    private final static String PERSONAL_DIRECTORY_NAME = "user-%s-files/";

    private final MinioResourceStorage resourceStorage;

    public ResourceService(MinioResourceStorage resourceStorage) {
        this.resourceStorage = resourceStorage;
    }

    public String findPersonalDirectory(int userId) {
        String directoryName = PERSONAL_DIRECTORY_NAME.formatted(userId);
        Optional<ResourceItem> result = resourceStorage.findDirectory(directoryName);
        if (result.isEmpty()) {
            resourceStorage.createDirectory(directoryName);
            ResourceItem newUserDirectory = resourceStorage.findDirectory(directoryName).orElseThrow(RuntimeException::new);
            return newUserDirectory.objectName();
        }
        return result.get().objectName();
    }

    public ResponseResourceDto findResource(String path) {
        if (path.endsWith("/")) {
            ResourceItem directory = resourceStorage.findDirectory(path).orElseThrow(RuntimeException::new);
            return convert(directory);
        }
        ResourceItem file = resourceStorage.findFile(path).orElseThrow(RuntimeException::new);
        return convert(file);
    }

    public List<ResponseResourceDto> searchResource(String personalDirectory, String query) {
        List<ResponseResourceDto> result = new ArrayList<>();
        List<ResourceItem> resources = resourceStorage.findAllByPrefix(personalDirectory);
        for (ResourceItem resource : resources) {
            ResponseResourceDto converted = convert(resource);
            if (matchQueryWithLowerCase(converted.name(), query)) {
                result.add(converted);
            }
        }
        return result;
    }

    public void deleteResource(String path) {
        if (path.endsWith("/")) {
            resourceStorage.deleteDirectory(path);
        }
        resourceStorage.deleteFile(path);
    }

    public ResponseResourceDto uploadResource(String path, MultipartFile file) {
        if (file.getOriginalFilename().contains("/")) {
            createSubdirectories(file.getOriginalFilename(), path);
        }
        String fullPath = path + file.getOriginalFilename();
        resourceStorage.upload(fullPath, file);
        ResourceItem resource = resourceStorage.findFile(fullPath).orElseThrow(RuntimeException::new);
        return convert(resource);
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

    public List<ResponseResourceDto> findAllFromDirectory(String path) {
        if (!path.endsWith("/")) {
            throw new RuntimeException();
        }
        List<ResponseResourceDto> result = new ArrayList<>();
        List<ResourceItem> resources = resourceStorage.findAllFromDirectory(path);
        for (ResourceItem resource : resources) {
            result.add(convert(resource));
        }
        return result;
    }

    public ResponseResourceDto createDirectory(String path) {
        if (!path.endsWith("/")) {
            throw new RuntimeException();
        }
        resourceStorage.createDirectory(path);
        ResourceItem item = resourceStorage.findDirectory(path).orElseThrow(RuntimeException::new);
        return convert(item);
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
        if (item.objectName().endsWith("/") || item.isDir()) {
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
}
