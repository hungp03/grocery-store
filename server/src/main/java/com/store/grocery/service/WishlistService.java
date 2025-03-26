package com.store.grocery.service;

import com.store.grocery.domain.Wishlist;
import com.store.grocery.domain.response.PaginationDTO;
import org.springframework.data.domain.Pageable;


public interface WishlistService {
    Wishlist addWishlist(Wishlist w);
    void deleteWishlist(Long productId);
    PaginationDTO getWishlistsByCurrentUser(Pageable pageable);
}
