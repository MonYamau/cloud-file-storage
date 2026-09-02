package ru.monyamau.cloudfilestorage.infrastructure;

import ru.monyamau.cloudfilestorage.domain.ResourceItem;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface ResourceStorage {
    Optional<ResourceItem> findResource(String objectPath);

    void deleteResource(String objectPath);

    void moveResource(String oldPath, String newPath);

    String uploadResource(String objectPath, InputStream inputStream, long size, String contentType);

    InputStream downloadResource(String objectPath);

    List<ResourceItem> findAllFromDirectory(String path);

    List<ResourceItem> findAllByPrefix(String prefix);

    void createDirectory(String directoryName);
}