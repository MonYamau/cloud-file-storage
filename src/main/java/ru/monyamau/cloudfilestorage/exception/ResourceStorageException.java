package ru.monyamau.cloudfilestorage.exception;

public class ResourceStorageException extends RuntimeException {
    public ResourceStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
