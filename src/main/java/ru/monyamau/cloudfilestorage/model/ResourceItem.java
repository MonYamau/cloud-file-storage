package ru.monyamau.cloudfilestorage.model;

public record ResourceItem(String objectName, String path, boolean isDir, long size) {
}
