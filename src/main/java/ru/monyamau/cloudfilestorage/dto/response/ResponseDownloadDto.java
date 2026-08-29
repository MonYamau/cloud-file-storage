package ru.monyamau.cloudfilestorage.dto.response;

public record ResponseDownloadDto(String filename, byte[] bytes) {
}