package com.store.grocery.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SelectedProductInCart {
    private long id;
    private String productName;
    private int quantity;
    private double price;
}
