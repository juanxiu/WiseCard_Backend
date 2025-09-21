package com.example.demo.card.entity;

import com.example.demo.benefit.entity.Benefit;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cardName;
    private String cardBank;
    private String imgUrl;
    private String type;

    @OneToMany(mappedBy = "cardId", cascade = CascadeType.ALL)
    private List<Benefit> benefits = new ArrayList<>();

    private Long externalId;

    @Builder
    public Card(Long id, String cardName, String cardBank, String imgUrl, String type, List<Benefit> benefits, Long externalId) {
        this.id = id;
        this.cardName = cardName;
        this.cardBank = cardBank;
        this.imgUrl = imgUrl;
        this.type = type;
        this.benefits = benefits != null ? benefits : new ArrayList<>();
        this.externalId = externalId;
    }
}
