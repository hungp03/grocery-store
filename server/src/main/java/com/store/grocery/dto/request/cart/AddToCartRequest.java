package com.store.grocery.dto.request.cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {
    private long productId;
    private int quantity;
}
