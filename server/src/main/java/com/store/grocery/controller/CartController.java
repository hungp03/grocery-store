package com.store.grocery.controller;

import com.store.grocery.domain.Cart;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.cart.CartItemDTO;
import com.store.grocery.service.CartService;
import com.store.grocery.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v2")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("cart")
    @ApiMessage("Add to cart")
    public ResponseEntity<Cart> add(@RequestBody Cart cart){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.cartService.addOrUpdateCart(cart));
    }

    @DeleteMapping("cart/{productId}")
    @ApiMessage("Delete product from cart")
    public ResponseEntity<Void> delete(@PathVariable long productId){
        this.cartService.deleteFromCart(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("cart")
    @ApiMessage("Get cart by user")
    public ResponseEntity<PaginationDTO> getCartByUser(Pageable pageable) {
        return ResponseEntity.ok(this.cartService.getCartByCurrentUser(pageable));
    }
    @GetMapping("cart/product-selected")
    @ApiMessage("Get products from cart")
    public ResponseEntity<List<CartItemDTO>> getSelectedItemsCart(@RequestParam("productIds") List<Long> productIds, Pageable pageable){
        return ResponseEntity.ok(this.cartService.getCartItemsByProductIds(productIds, pageable));
    }
}
