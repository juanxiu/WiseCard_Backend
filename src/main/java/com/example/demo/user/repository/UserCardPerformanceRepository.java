package com.example.demo.user.repository;

import com.example.demo.user.entity.UserCardPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface UserCardPerformanceRepository extends JpaRepository<UserCardPerformance, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<UserCardPerformance> findByUserIdAndCardId(Long userId, Long cardId);
    
}

