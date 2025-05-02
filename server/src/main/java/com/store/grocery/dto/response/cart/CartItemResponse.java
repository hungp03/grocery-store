package com.store.grocery.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private String productName;
    private Double price;
    private Integer quantity;
    private String imageUrl;
    private String category;
    private Integer stock;
    private Boolean isActive;
}
