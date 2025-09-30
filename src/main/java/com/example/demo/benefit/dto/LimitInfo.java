package com.example.demo.benefit.dto;

public record LimitInfo(
    Long usedDiscountAmount,    // 사용된 할인 금액
    Long totalDiscountLimit,   // 할인 한도
    Long usedPointAmount,      // 사용된 포인트 금액
    Long totalPointLimit,      // 포인트 한도
    Long usedCashbackAmount,   // 사용된 캐시백 금액
    Long totalCashbackLimit    // 캐시백 한도
) {}
