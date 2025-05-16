package com.store.grocery.service.impl;

import com.store.grocery.domain.Cart;
import com.store.grocery.domain.CartId;
import com.store.grocery.domain.Product;
import com.store.grocery.domain.User;
import com.store.grocery.dto.request.cart.AddToCartRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.cart.CartItemResponse;
import com.store.grocery.dto.response.cart.PreOrderCartItem;
import com.store.grocery.repository.CartRepository;
import com.store.grocery.service.CartService;
import com.store.grocery.service.ProductService;
import com.store.grocery.service.UserService;
import com.store.grocery.util.JwtUtil;
import com.store.grocery.util.exception.ResourceInvalidException;
import com.store.grocery.util.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final UserService userService;

    @Override
    public Cart addOrUpdateCart(AddToCartRequest cartRequest) {
        long uid = JwtUtil.getUserId();
        log.info("User {} is adding/updating cart item with productId={}", uid, cartRequest.getProductId());
        User u = this.userService.findById(uid);
        Product p = this.productService.findByIdAndIsActiveTrue(cartRequest.getProductId());
        if (cartRequest.getQuantity() > p.getQuantity()) {
            log.warn("User {} tried to add {} items, but only {} available", uid, cartRequest.getQuantity(), p.getQuantity());
            throw new ResourceInvalidException("Số lượng hàng không đủ");
        }

        Optional<Cart> existingCart = cartRepository.findById(new CartId(u.getId(), p.getId()));
        Cart cartItem;
        if (existingCart.isPresent()) {
            cartItem = existingCart.get();
            int newQuantity = cartItem.getQuantity() + cartRequest.getQuantity();
            if (newQuantity < 0) {
                log.warn("User {} tried to set invalid quantity {}", uid, newQuantity);
                throw new ResourceInvalidException("Số lượng sản phẩm không hợp lệ");
            }
            if (newQuantity > p.getQuantity()) {
                log.warn("User {} tried to add {} items, but only {} available", uid, newQuantity, p.getQuantity());
                throw new ResourceInvalidException("Số lượng hàng trong kho không đủ");
            }
            cartItem.setQuantity(newQuantity);
            log.info("Updated cart item: userId={}, productId={}, newQuantity={}", uid, p.getId(), newQuantity);
        } else {
            CartId cartId = new CartId(u.getId(), p.getId());
            cartItem = Cart.builder().id(cartId).user(u).product(p).quantity(cartRequest.getQuantity()).build();
            log.info("Added new cart item: userId={}, productId={}, quantity={}", uid, p.getId(), cartRequest.getQuantity());
        }
        return this.cartRepository.save(cartItem);
    }

    @Override
    public void deleteFromCart(long productId) {
        long uid = JwtUtil.getUserId();
        log.info("User {} is deleting product {} from cart", uid, productId);
        User u = this.userService.findById(uid);
        boolean exists = this.cartRepository.existsById(new CartId(uid, productId));
        if (!exists) {
            log.warn("User {} tried to delete non-existent product {} from cart", uid, productId);
            throw new ResourceNotFoundException("Sản phẩm không tồn tại trong giỏ hàng");
        }
        CartId cartId = new CartId(uid, productId);
        this.cartRepository.deleteById(cartId);
        log.info("Deleted product {} from cart for user {}", productId, uid);
    }

    @Override
    public PaginationResponse getCartByCurrentUser(Pageable pageable) {
        long uid = JwtUtil.getUserId();
        log.info("Fetching cart items for user {}", uid);
        Page<CartItemResponse> cartItems = this.cartRepository.findCartItemsByUserId(uid, pageable);
        log.info("Fetched cart items for user {}", uid);
        return PaginationResponse.from(cartItems, pageable);
    }

    @Override
    public List<PreOrderCartItem> getCartItemsByProductIds(List<Long> productIds, Pageable pageable) {
        long uid = JwtUtil.getUserId();
        log.info("Fetching selected cart items for user {}", uid);
        return this.cartRepository.findCartItemsByUserIdAndProductId(uid, productIds, pageable);
    }

    @Override
    @Transactional
    public void deleteSelectedItems(List<Long> productIds) {
        long uid = JwtUtil.getUserId();
        log.info("User {} is deleting selected cart items", uid);
        List<CartId> cartIds = productIds.stream()
                .map(productId -> new CartId(uid, productId))
                .toList();
        cartRepository.deleteByIdIn(cartIds);
        log.info("Deleted selected cart items for user {}", uid);
    }
}
