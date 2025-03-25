package com.app.webnongsan.service;

import com.app.webnongsan.domain.Cart;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.domain.response.cart.CartItemDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CartService {
    Cart addOrUpdateCart(Cart cart);
    void deleteFromCart(long productId);
    PaginationDTO getCartByCurrentUser(Pageable pageable);
    List<CartItemDTO> getCartItemsByProductIds(List<Long> productIds, Pageable pageable);
    void deleteSelectedItems(List<Long> productIds);
    long countProductInCart(long userId);

}
