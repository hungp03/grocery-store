package com.store.grocery.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SearchProductResponse {
    private long id;
    private String productName;
    private double price;
    private String imageUrl;
    private String category;
}
