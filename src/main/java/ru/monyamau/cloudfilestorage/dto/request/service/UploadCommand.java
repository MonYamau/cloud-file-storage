package ru.monyamau.cloudfilestorage.dto.request.service;

import java.util.List;

public record UploadCommand(String path, List<UploadedFile> files) {
}