package com.example.demo.benefit.dto;

public record PerformanceInfo(
    Long currentAmount,    // 현재 실적
    Long targetAmount,     // 목표 실적
    boolean isAchieved    // 달성 여부
) {}
