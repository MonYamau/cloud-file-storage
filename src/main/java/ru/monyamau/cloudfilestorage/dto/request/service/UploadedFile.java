package ru.monyamau.cloudfilestorage.dto.request.service;

import java.io.InputStream;

public record UploadedFile(String filename, String contentType, long size, InputStream inputStream) {
}