package ru.monyamau.cloudfilestorage.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class SessionStorage {
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public SessionStorage(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String key, String value, long ttlMin) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(ttlMin));
    }

    public String findBy(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
