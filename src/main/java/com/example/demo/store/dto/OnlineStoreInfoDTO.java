package com.example.demo.store.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record OnlineStoreInfoDTO(
    String storeId,          
    String storeName,     
    List<CardBenefitDTO> availableCards  
) {}
