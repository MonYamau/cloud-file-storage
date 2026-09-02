package ru.monyamau.cloudfilestorage.domain;

public record ResourceItem(String objectName, boolean isDir, Long size) {
}
