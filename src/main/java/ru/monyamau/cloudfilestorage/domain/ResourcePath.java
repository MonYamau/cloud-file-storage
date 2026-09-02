package ru.monyamau.cloudfilestorage.domain;

public record ResourcePath(String personalDirectory, String path) {
    private final static String SEPARATOR_SIGN = "/";

    public boolean isDirectory() {
        return path.endsWith(SEPARATOR_SIGN);
    }

    public boolean isPersonalDirectory() {
        return path.isEmpty() || path.equals(SEPARATOR_SIGN);
    }

    public String getFullPath() {
        if (isPersonalDirectory()) {
            return personalDirectory;
        }
        return personalDirectory + path;
    }

    public String getResourceName() {
        if (isPersonalDirectory()) {
            return "";
        }
        String[] segments = path.split(SEPARATOR_SIGN);
        return segments[segments.length - 1];
    }

    public String getParentDirectoryWithoutPersonalDirectory() {
        if (isPersonalDirectory()) {
            return SEPARATOR_SIGN;
        }
        return collectParentDirectory(path);
    }

    public String getParentDirectoryWithPersonalDirectory() {
        if (isPersonalDirectory()) {
            return personalDirectory;
        }
        return collectParentDirectory(personalDirectory + path);
    }

    private String collectParentDirectory(String path) {
        String[] segments = path.split(SEPARATOR_SIGN);
        StringBuilder parentDirectory = new StringBuilder();
        for (int i = 0; i < segments.length - 1; i++) {
            parentDirectory.append(segments[i]).append(SEPARATOR_SIGN);
        }
        return parentDirectory.toString();
    }
}
