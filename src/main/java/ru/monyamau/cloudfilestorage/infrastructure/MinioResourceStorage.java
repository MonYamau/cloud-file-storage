package ru.monyamau.cloudfilestorage.infrastructure;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteRequest.Object;
import io.minio.messages.DeleteResult.Error;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.monyamau.cloudfilestorage.domain.ResourceItem;
import ru.monyamau.cloudfilestorage.exception.ResourceStorageException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class MinioResourceStorage implements ResourceStorage {
    private static final long AUTOMATIC_BUFFER = -1;
    private static final String NOT_FOUND_RESOURCE = "NoSuchKey";
    private final static String SEPARATOR_SIGN = "/";

    private final MinioClient minioClient;
    private final String bucketName;

    @Autowired
    public MinioResourceStorage(MinioClient minioClient, @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public Optional<ResourceItem> findResource(String objectPath) {
        return isDirectory(objectPath)
                ? findDirectory(objectPath)
                : findFile(objectPath);
    }

    public void deleteResource(String objectPath) {
        if (isDirectory(objectPath)) {
            deleteDirectory(objectPath);
        } else {
            deleteFile(objectPath);
        }
    }

    public void moveResource(String oldPath, String newPath) {
        if (isDirectory(oldPath) && isDirectory(newPath)) {
            moveDirectory(oldPath, newPath);
        } else {
            moveFile(oldPath, newPath);
        }
    }

    public void createDirectory(String directoryName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(directoryName)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0L, AUTOMATIC_BUFFER)
                            .build()
            );
        } catch (MinioException e) {
            throw new ResourceStorageException("Возникла ошибка при попытке создания директории", e);
        }
    }

    public List<ResourceItem> findAllByPrefix(String prefix) {
        try {
            List<ResourceItem> itemList = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                itemList.add(convert(item));
            }
            return itemList;
        } catch (MinioException e) {
            throw new ResourceStorageException("Возникла ошибка при попытке поиска по префиксу", e);
        }
    }

    public List<ResourceItem> findAllFromDirectory(String path) {
        try {
            List<ResourceItem> itemList = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .recursive(false)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                itemList.add(convert(item));
            }
            return itemList;
        } catch (MinioException e) {
            throw new ResourceStorageException("Возникла ошибка при попытке поиска по директории", e);
        }
    }

    public String uploadResource(String objectPath, InputStream inputStream, long size, String contentType) {
        createSubdirectories(objectPath);
        try {
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(
                    PutObjectArgs.builder()
                            .stream(inputStream, size, AUTOMATIC_BUFFER)
                            .object(objectPath)
                            .bucket(bucketName)
                            .contentType(contentType)
                            .build()
            );
            return objectWriteResponse.object();
        } catch (MinioException e) {
            throw new ResourceStorageException("Возникла ошибка при попытке сохранения ресурса", e);
        }
    }

    public InputStream downloadResource(String objectPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
        } catch (MinioException e) {
            throw new ResourceStorageException("Возникла ошибка при попытке загрузки ресурса", e);
        }
    }

    private Optional<ResourceItem> findFile(String objectPath) {
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
            return Optional.of(new ResourceItem(response.object(), false, response.size()));
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals(NOT_FOUND_RESOURCE)) {
                return Optional.empty();
            }
            throw new ResourceStorageException("Возникла ошибка при попытке поиска файла", e);
        } catch (MinioException e) {
            throw new ResourceStorageException("Возникла ошибка при попытке поиска файла", e);
        }
    }

    private Optional<ResourceItem> findDirectory(String objectPath) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .recursive(false)
                            .prefix(objectPath)
                            .build()
            );
            if (results.iterator().hasNext()) {
                Item item = results.iterator().next().get();
                return Optional.of(convert(item));
            }
            return Optional.empty();
        } catch (MinioException e) {
            throw new ResourceStorageException("Возникла ошибка при попытке поиска директории", e);
        }
    }

    private void deleteFile(String objectPath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
        } catch (MinioException e) {
            throw new ResourceStorageException("Возникла ошибка при попытке удаления файла", e);
        }
    }

    private void deleteDirectory(String objectPath) {
        try {
            List<ResourceItem> result = findAllByPrefix(objectPath);
            List<Object> deletedList = new ArrayList<>();
            for (ResourceItem item : result) {
                deletedList.add(new Object(item.objectName()));
            }
            Iterable<Result<Error>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(deletedList)
                            .build()
            );
            for (Result<Error> errorResult : results) {
                if (errorResult.get() != null) {
                    throw new RuntimeException(errorResult.get().message());
                }
            }
        } catch (MinioException e) {
            throw new ResourceStorageException("Возникла ошибка при попытке удаления директории", e);
        }
    }

    private void moveFile(String oldName, String newName) {
        copy(oldName, newName);
        deleteFile(oldName);
    }

    private void moveDirectory(String oldPath, String newPath) {
        List<ResourceItem> resourceItemList = findAllByPrefix(oldPath);
        createDirectory(newPath);
        for (ResourceItem resourceItem : resourceItemList) {
            String oldObjectPath = resourceItem.objectName();
            String newObjectPath = oldObjectPath.replace(oldPath, newPath);
            copy(oldObjectPath, newObjectPath);
        }
        deleteDirectory(oldPath);
    }

    private void copy(String oldPath, String newPath) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newPath)
                            .source(
                                    SourceObject.builder()
                                            .bucket(bucketName)
                                            .object(oldPath)
                                            .build()
                            )
                            .build()
            );
        } catch (MinioException e) {
            throw new ResourceStorageException("Возникла ошибка при попытке перемещения ресурса", e);
        }
    }

    private void createSubdirectories(String objectPath) {
        List<String> resources = Arrays.stream(objectPath.split(SEPARATOR_SIGN)).toList();
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < resources.size() - 1; i++) {
            pathBuilder.append(resources.get(i)).append(SEPARATOR_SIGN);
            String directoryPath = pathBuilder.toString();
            if (findDirectory(directoryPath).isEmpty()) {
                createDirectory(directoryPath);
            }
        }
    }

    private ResourceItem convert(Item item) {
        String path = item.objectName();
        boolean isDir = isDirectory(path) || item.isDir();
        return new ResourceItem(path, isDir, isDir ? null : item.size());
    }

    private boolean isDirectory(String objectPath) {
        return objectPath.endsWith(SEPARATOR_SIGN);
    }
}
