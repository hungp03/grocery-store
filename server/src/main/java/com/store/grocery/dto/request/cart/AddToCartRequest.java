package com.store.grocery.dto.request.cart;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {
    @NotNull(message = "Không được bỏ trống productId")
    private Long productId;
    @NotNull(message = "Không được bỏ trống số lượng")
    private Integer quantity;
}
