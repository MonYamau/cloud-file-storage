package ru.monyamau.cloudfilestorage.dto.response;

public record ResponseDownloadDto(String filename, String contentType, byte[] bytes) {
}