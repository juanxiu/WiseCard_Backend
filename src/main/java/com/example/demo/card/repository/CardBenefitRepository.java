package com.example.demo.card.repository;

import com.example.demo.card.entity.Card;
import com.example.demo.card.entity.CardBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardBenefitRepository extends JpaRepository<CardBenefit, Long> {

    List<CardBenefit> findByCard(Card card);
}
