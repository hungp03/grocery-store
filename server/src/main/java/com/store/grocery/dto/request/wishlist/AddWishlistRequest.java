package com.store.grocery.dto.request.wishlist;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddWishlistRequest {
    @NotNull(message = "Product ID không được để trống")
    private Long productId;
}
