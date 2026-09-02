package ru.monyamau.cloudfilestorage.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class RedisSessionStorage implements SessionStorage {
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisSessionStorage(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveWithTtl(String key, String value, int ttlMin) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(ttlMin));
        } catch (DataAccessException e) {
            throw new RuntimeException();
        }
    }

    public Optional<String> findBy(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (DataAccessException e) {
            throw new RuntimeException();
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException e) {
            throw new RuntimeException();
        }
    }
}