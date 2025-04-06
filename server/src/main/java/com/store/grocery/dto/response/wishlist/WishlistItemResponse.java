package com.store.grocery.dto.response.wishlist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WishlistItemResponse {
    private Long id;
    private String productName;
    private Double price;
    private String imageUrl;
    private String category;
}
