package com.bloodline.service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributedLockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private DistributedLockService lockService;

    @Test
    void shouldAcquireLockWhenNotHeld() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("bloodline:lock:app1:release_sit"), anyString(), eq(600L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        boolean acquired = lockService.tryLock("app1", "release_sit", 600);

        assertThat(acquired).isTrue();
    }

    @Test
    void shouldFailToAcquireLockWhenAlreadyHeld() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("bloodline:lock:app1:release_sit"), anyString(), eq(600L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        boolean acquired = lockService.tryLock("app1", "release_sit", 600);

        assertThat(acquired).isFalse();
    }

    @Test
    void shouldReleaseLock() {
        when(redisTemplate.delete("bloodline:lock:app1:release_sit")).thenReturn(true);

        lockService.unlock("app1", "release_sit");

        verify(redisTemplate).delete("bloodline:lock:app1:release_sit");
    }
}
