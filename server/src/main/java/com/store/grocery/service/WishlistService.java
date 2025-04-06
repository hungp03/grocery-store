package com.store.grocery.service;

import com.store.grocery.domain.Wishlist;
import com.store.grocery.dto.request.wishlist.AddWishlistRequest;
import com.store.grocery.dto.response.PaginationResponse;
import org.springframework.data.domain.Pageable;


public interface WishlistService {
    Wishlist addWishlist(AddWishlistRequest request);
    void deleteWishlist(Long productId);
    PaginationResponse getWishlistsByCurrentUser(Pageable pageable);
}
