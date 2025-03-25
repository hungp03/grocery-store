package com.app.webnongsan.service;

import com.app.webnongsan.domain.Wishlist;
import com.app.webnongsan.domain.response.PaginationDTO;
import org.springframework.data.domain.Pageable;


public interface WishlistService {
    Wishlist addWishlist(Wishlist w);
    void deleteWishlist(Long productId);
    PaginationDTO getWishlistsByCurrentUser(Pageable pageable);
}
