package com.example.demo.lock;

import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.SetArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Redis를 직접 사용하는 LockManager
 * SETNX 명령어를 사용하여 분산락 구현
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LockManager {
    
    private final RedisCommands<String, String> redisCommands;
    
    private static final String LOCK_VALUE = "lock";
    private static final int DEFAULT_TTL_SECONDS = 3;
    
    /**
     * 분산락 획득 시도
     * @param lockKey 락 키
     * @param ttlSeconds TTL (초)
     * @return 락 획득 성공 여부
     */
    public boolean tryLock(String lockKey, int ttlSeconds) {
        try {
            SetArgs setArgs = SetArgs.Builder.nx().ex(ttlSeconds);
            String result = redisCommands.set(lockKey, LOCK_VALUE, setArgs);
            
            boolean acquired = "OK".equals(result);
            if (acquired) {
                log.debug("분산락 획득 성공: {}", lockKey);
            } else {
                log.debug("분산락 획득 실패: {}", lockKey);
            }
            
            return acquired;
        } catch (Exception e) {
            log.error("분산락 획득 중 오류 발생: {}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 분산락 해제
     * @param lockKey 락 키
     * @return 해제 성공 여부
     */
    public boolean releaseLock(String lockKey) {
        try {
            Long result = redisCommands.del(lockKey);
            boolean released = result != null && result > 0;
            
            if (released) {
                log.debug("분산락 해제 성공: {}", lockKey);
            } else {
                log.debug("분산락 해제 실패 (락이 존재하지 않음): {}", lockKey);
            }
            
            return released;
        } catch (Exception e) {
            log.error("분산락 해제 중 오류 발생: {}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 기본 TTL로 분산락 획득 시도
     * @param lockKey 락 키
     * @return 락 획득 성공 여부
     */
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_TTL_SECONDS);
    }
}

