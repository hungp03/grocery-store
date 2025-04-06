package com.store.grocery.domain.response.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SelectedProductDTO {
    private long id;
    private String productName;
    private int quantity;
    private double price;
}
