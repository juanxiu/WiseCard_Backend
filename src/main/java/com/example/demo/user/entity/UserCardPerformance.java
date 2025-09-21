package com.example.demo.user.entity;

import com.example.demo.card.entity.Card;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_card_performance")
@Getter
@NoArgsConstructor
public class UserCardPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card; // 카드

    @Column(nullable = false)
    private Long currentAmount; // 현재 실적 금액

    @Column(nullable = false)
    private Long targetAmount; // 목표 실적 금액 (혜택 최소 실적)

    @Column(nullable = false)
    private Boolean isTargetAchieved; // 목표 실적 달성 여부

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt; // 마지막 실적 업데이트 시간

    @Builder
    public UserCardPerformance(Long id, Long userId, Card card, Long currentAmount, Long targetAmount, Boolean isTargetAchieved, LocalDateTime lastUpdatedAt) {
        this.id = id;
        this.userId = userId;
        this.card = card;
        this.currentAmount = currentAmount;
        this.targetAmount = targetAmount;
        this.isTargetAchieved = isTargetAchieved;
        this.lastUpdatedAt = lastUpdatedAt;
    }

}
