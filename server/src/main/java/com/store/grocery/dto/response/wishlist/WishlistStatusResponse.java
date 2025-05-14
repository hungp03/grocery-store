package com.store.grocery.dto.response.wishlist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WishlistStatusResponse {
    private Long productId;
    private boolean isWishlisted;
}
