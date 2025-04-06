package com.store.grocery.service;

import com.store.grocery.domain.Cart;
import com.store.grocery.dto.request.cart.AddToCartRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.cart.SelectedProductInCart;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CartService {
    Cart addOrUpdateCart(AddToCartRequest cartRequest);
    void deleteFromCart(long productId);
    PaginationResponse getCartByCurrentUser(Pageable pageable);
    List<SelectedProductInCart> getCartItemsByProductIds(List<Long> productIds, Pageable pageable);
    void deleteSelectedItems(List<Long> productIds);
}
