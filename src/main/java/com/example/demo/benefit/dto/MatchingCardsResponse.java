package com.example.demo.benefit.dto;

import java.util.List;

public record MatchingCardsResponse(
    List<AvailableCardResponse> availableCards  // 사용 가능한 카드
) {}