package ru.monyamau.cloudfilestorage.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class RedisSessionStorage {
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisSessionStorage(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String key, String value, int ttlMin) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(ttlMin));
    }

    public Optional<String> findBy(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}