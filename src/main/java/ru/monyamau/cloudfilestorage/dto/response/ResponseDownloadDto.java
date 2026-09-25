package ru.monyamau.cloudfilestorage.dto.response;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record ResponseDownloadDto(String filename, String contentType, StreamingResponseBody body) {
}