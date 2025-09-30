package com.example.demo.store.dto;

public record StoreSearchRequest(
    Double lat,    // 위도
    Double lng,    // 경도
    String cat     // 카테고리 코드
) {}