package com.example.demo.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 스핀락을 사용한 Redis 분산락 유틸리티
 * LockManager를 DI 받아서 분산락 로직 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLockUtil {
    
    private final LockManager lockManager;
    
    private static final int DEFAULT_MAX_RETRIES = 10;
    private static final long DEFAULT_RETRY_INTERVAL_MS = 100;
    
    /**
     * 분산락을 획득하고 비즈니스 로직 실행
     * @param lockKey 락 키
     * @param businessLogic 실행할 비즈니스 로직
     * @param maxRetries 최대 재시도 횟수
     * @param retryIntervalMs 재시도 간격 (밀리초)
     * @param ttlSeconds 락 TTL (초)
     * @param <T> 반환 타입
     * @return 비즈니스 로직 실행 결과
     * @throws DistributedLockException 락 획득 실패 시
     */
    public <T> T acquireAndRunLock(String lockKey, 
                                   Supplier<T> businessLogic, 
                                   int maxRetries, 
                                   long retryIntervalMs, 
                                   int ttlSeconds) {
        
        int attempts = 0;
        boolean lockAcquired = false;
        
        try {
            // 스핀락으로 락 획득 시도
            while (attempts < maxRetries) {
                attempts++;
                
                if (lockManager.tryLock(lockKey, ttlSeconds)) {
                    lockAcquired = true;
                    log.debug("분산락 획득 성공: {} (시도: {}/{})", lockKey, attempts, maxRetries);
                    break;
                }
                
                if (attempts < maxRetries) {
                    log.debug("분산락 획득 실패, 재시도 중: {} (시도: {}/{})", lockKey, attempts, maxRetries);
                    Thread.sleep(retryIntervalMs);
                }
            }
            
            if (!lockAcquired) {
                throw new DistributedLockException("분산락 획득 실패: " + lockKey + " (최대 시도 횟수 초과: " + maxRetries + ")");
            }
            
            // 비즈니스 로직 실행
            return businessLogic.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DistributedLockException("분산락 획득 중 인터럽트 발생: " + lockKey, e);
        } catch (Exception e) {
            throw new DistributedLockException("분산락 처리 중 오류 발생: " + lockKey, e);
        } finally {
            // 락 해제
            if (lockAcquired) {
                lockManager.releaseLock(lockKey);
                log.debug("분산락 해제 완료: {}", lockKey);
            }
        }
    }
    
    /**
     * 기본 설정으로 분산락 실행
     * @param lockKey 락 키
     * @param businessLogic 실행할 비즈니스 로직
     * @param <T> 반환 타입
     * @return 비즈니스 로직 실행 결과
     */
    public <T> T acquireAndRunLock(String lockKey, Supplier<T> businessLogic) {
        return acquireAndRunLock(lockKey, businessLogic, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_INTERVAL_MS, 10);
    }
    
    /**
     * 분산락 예외 클래스
     */
    public static class DistributedLockException extends RuntimeException {
        public DistributedLockException(String message) {
            super(message);
        }
        
        public DistributedLockException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}



