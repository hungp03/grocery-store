package com.store.grocery.dto.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {
    @NotNull(message = "Không được bỏ trống productId")
    private Long productId;
    @Min(value = 1, message = "Số lượng nhỏ nhất là 1")
    private int quantity;
}
