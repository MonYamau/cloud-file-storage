package ru.monyamau.cloudfilestorage.dto.response;

import java.io.OutputStream;
import java.util.function.Consumer;

public record ResponseDownloadDto(String filename, Consumer<OutputStream> performer) {
}