package com.bloodline.service.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DistributedLockService {

    private static final String LOCK_PREFIX = "bloodline:lock:";

    private final StringRedisTemplate redisTemplate;

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String appId, String branch, long ttlSeconds) {
        String key = buildKey(appId, branch);
        String value = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, value, ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(acquired);
    }

    public void unlock(String appId, String branch) {
        String key = buildKey(appId, branch);
        redisTemplate.delete(key);
    }

    private String buildKey(String appId, String branch) {
        return LOCK_PREFIX + appId + ":" + branch;
    }
}
