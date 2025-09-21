package com.example.demo.user.entity;

import java.time.LocalDateTime;

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
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    
    @Column(nullable = false)
    private Boolean isActive = true; // 활성 상태
    
    @Column(nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now(); // 등록일

    @Builder
    public UserCard(Long id, Long userId, Card card, Boolean isActive, LocalDateTime registeredAt) {
        this.id = id;
        this.userId = userId;
        this.card = card;
        this.isActive = isActive;
        this.registeredAt = registeredAt;
    }

}
