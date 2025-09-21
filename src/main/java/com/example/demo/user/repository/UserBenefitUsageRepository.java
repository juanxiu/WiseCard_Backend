package com.example.demo.user.repository;

import com.example.demo.user.entity.UserBenefitUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;


public interface UserBenefitUsageRepository extends JpaRepository<UserBenefitUsage, Long> {
    

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT COALESCE(SUM(ubu.usedAmount), 0) FROM UserBenefitUsage ubu " +
           "WHERE ubu.userId = :userId AND ubu.card.id = :cardId AND ubu.benefitType = :benefitType")
    Long findTotalUsedAmountByUserAndCardAndBenefitType(@Param("userId") Long userId, 
                                                       @Param("cardId") Long cardId, 
                                                       @Param("benefitType") String benefitType);
    
}
