package ru.monyamau.cloudfilestorage.repository;

import java.util.Optional;

public interface SessionStorage {
    void saveWithTtl(String key, String value, int ttlMin);

    Optional<String> findBy(String key);

    void delete(String key);
}