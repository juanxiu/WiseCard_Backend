package com.example.demo.benefit.dto;

import lombok.Builder;

@Builder
public record CardWithBenefitResponse(
    Long cardId,
    String cardName,
    String cardBank,
    String imgUrl,
    String type,
    BenefitDetailDTO benefits
) {}
