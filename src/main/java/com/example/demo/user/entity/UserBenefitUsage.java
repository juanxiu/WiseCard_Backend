package com.example.demo.user.entity;

import java.time.LocalDateTime;

import com.example.demo.benefit.entity.Benefit;
import com.example.demo.card.entity.Card;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_benefit_usage")
@Getter
@NoArgsConstructor
public class UserBenefitUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id", nullable = false)
    private Benefit benefit; 

    @Column(nullable = false)
    private String benefitType; // DISCOUNT, POINT, CASHBACK

    @Column(nullable = false)
    private Long usedAmount; // 사용한 혜택 금액

    @Column(nullable = false)
    private Long remainingLimit; // 남은 혜택 한도

    @Column(nullable = false)
    private String place; // 사용 장소

    @Column(nullable = false)
    private LocalDateTime usedAt; // 사용 시간

    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public UserBenefitUsage(Long id, Long userId, Card card, Benefit benefit, String benefitType, Long usedAmount, Long remainingLimit, String place, LocalDateTime usedAt) {
        this.id = id;
        this.userId = userId;
        this.card = card;
        this.benefit = benefit;
        this.benefitType = benefitType;
        this.usedAmount = usedAmount;
        this.remainingLimit = remainingLimit;
        this.place = place;
    }
}

