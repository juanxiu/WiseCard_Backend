package com.example.demo.store.dto;

import java.util.List;

public record StoreSearchResponse(
    List<StoreInfoDTO> stores
) {

}
