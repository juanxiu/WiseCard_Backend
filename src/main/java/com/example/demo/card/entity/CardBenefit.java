package com.example.demo.card.entity;

import com.example.demo.benefit.entity.Benefit;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class CardBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id")
    private Benefit benefit;



    @Builder
    public CardBenefit(Card card, Benefit benefit) {
        this.card = card;
        this.benefit = benefit;
    }


}

