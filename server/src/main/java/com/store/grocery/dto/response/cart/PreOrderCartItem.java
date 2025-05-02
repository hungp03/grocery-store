package com.store.grocery.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PreOrderCartItem {
    private Long id;
    private String productName;
    private Double price;
    private Integer quantity;
}
