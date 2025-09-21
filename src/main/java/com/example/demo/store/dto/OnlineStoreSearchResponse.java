package com.example.demo.store.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record OnlineStoreSearchResponse(
    List<OnlineStoreInfoDTO> stores
) {}
