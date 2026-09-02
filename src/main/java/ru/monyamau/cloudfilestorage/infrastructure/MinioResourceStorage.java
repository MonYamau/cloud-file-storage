package ru.monyamau.cloudfilestorage.infrastructure;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteRequest.Object;
import io.minio.messages.DeleteResult.Error;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.model.ResourceItem;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
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

    public Optional<ResourceItem> findFile(String objectPath) {
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
            if (results.iterator().hasNext()) {
                Item item = results.iterator().next().get();
                return Optional.of(convert(item));
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                            .recursive(true)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                itemList.add(convert(item));
            }
            return itemList;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String copy(String oldPath, String newPath) {
        try {
            ObjectWriteResponse objectWriteResponse = minioClient.copyObject(
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
            return objectWriteResponse.object();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String upload(String objectPath, MultipartFile file) {
        try {
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(
                    PutObjectArgs.builder()
                            .stream(file.getInputStream(), file.getSize(), AUTOMATIC_BUFFER)
                            .object(objectPath)
                            .bucket(bucketName)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectWriteResponse.object();
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(String objectPath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteDirectory(String objectPath) {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResourceItem convert(Item item) {
        boolean isDir = item.objectName().endsWith("/");
        return new ResourceItem(item.objectName(), isDir, isDir ? null : item.size());
    }
}
