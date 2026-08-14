package ru.monyamau.cloudfilestorage.model;

public record ResourceItem(String objectName, boolean isDir, Long size) {
}
