package ru.monyamau.cloudfilestorage.repository;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.monyamau.cloudfilestorage.model.ResourceItem;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//TODO exceptions
@Repository
public class MinioStorage {
    private static final long AUTOMATIC_BUFFER = -1;
    private static final String NOT_FOUND_RESOURCE = "NoSuchKey";
    private final MinioClient minioClient;
    private final String bucketName;

    @Autowired
    public MinioStorage(MinioClient minioClient, @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public Optional<ResourceItem> findFile(String objectPath) {
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
            if (response == null) {
            return Optional.of(new ResourceItem(response.object(), false, response.size()));
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals(NOT_FOUND_RESOURCE)) {
                return Optional.empty();
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ResourceItem> findDirectory(String objectPath) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .recursive(false)
                            .prefix(objectPath)
                            .build()
            );
            if (results == null) {
                return Optional.empty();
            }
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir()) {
                    return Optional.of(new ResourceItem(item.objectName(), objectPath, true, item.size()));
                }
            if (results.iterator().hasNext()) {
                return Optional.of(new ResourceItem(objectPath, true, null));
            }
            return Optional.empty();
        } catch (MinioException e) {
            throw new RuntimeException("minio error");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<ResourceItem> findAllByPrefix(String prefix) {
        try {
            List<ResourceItem> itemList = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(false)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                itemList.add(new ResourceItem(item.objectName(), item.isDir(), item.size()));
            }
            return itemList;
        } catch (MinioException e) {
            throw new RuntimeException("minio error");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void upload(String objectPath, long fileSize, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .stream(inputStream, fileSize, AUTOMATIC_BUFFER)
                            .object(objectPath)
                            .bucket(bucketName)
                            .contentType(contentType)
                            .build()
            );
        } catch (MinioException e) {
            throw new RuntimeException("minio error");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream download(String objectPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
        } catch (MinioException e) {
            throw new RuntimeException("minio error");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String objectPath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
        } catch (MinioException e) {
            throw new RuntimeException("minio error");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
