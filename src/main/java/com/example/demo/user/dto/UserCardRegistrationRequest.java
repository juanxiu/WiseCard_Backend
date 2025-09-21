package com.example.demo.user.dto;

import lombok.Builder;

@Builder
public record UserCardRegistrationRequest(
    Long userId,    // 사용자 ID
    Long cardId     // 카드 ID
) {}

