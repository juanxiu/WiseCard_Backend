package com.example.demo.store.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record CardBenefitDTO(
    Long cardId,
    String cardName,
    List<BenefitInfoDTO> benefits
) {}
