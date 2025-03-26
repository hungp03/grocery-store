package com.store.grocery.service.impl;

import com.store.grocery.domain.Cart;
import com.store.grocery.domain.CartId;
import com.store.grocery.domain.Product;
import com.store.grocery.domain.User;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.cart.CartItemDTO;
import com.store.grocery.repository.CartRepository;
import com.store.grocery.repository.UserRepository;
import com.store.grocery.service.CartService;
import com.store.grocery.service.ProductService;
import com.store.grocery.util.PaginationHelper;
import com.store.grocery.util.SecurityUtil;
import com.store.grocery.util.exception.ResourceInvalidException;
import com.store.grocery.util.exception.UserNotFoundException;
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
public class CartServiceImpl implements CartService{
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final PaginationHelper paginationHelper;

    @Override
    public Cart addOrUpdateCart(Cart cart) {
        long uid = SecurityUtil.getUserId();
        User u = this.userRepository.findById(uid).orElseThrow(() -> new UserNotFoundException("User không tồn tại"));
        Product p = this.productService.findById(cart.getId().getProductId());

        if (cart.getQuantity() > p.getQuantity()) {
            throw new ResourceInvalidException("Số lượng hàng không đủ");
        }

        Optional<Cart> existingCart = cartRepository.findById(new CartId(u.getId(), p.getId()));
        if (existingCart.isPresent()) {
            Cart cartItem = existingCart.get();
            int newQuantity = cartItem.getQuantity() + cart.getQuantity();
            if (newQuantity < 0) {
                throw new ResourceInvalidException("Số lượng sản phẩm không hợp lệ");
            }
            if (newQuantity > p.getQuantity()) {
                throw new ResourceInvalidException("Số lượng hàng trong kho không đủ");
            }
            cartItem.setQuantity(newQuantity);
            return this.cartRepository.save(cartItem);
        } else {
            cart.setUser(u);
            cart.setProduct(p);
            return this.cartRepository.save(cart);
        }
    }

    @Override
    public void deleteFromCart(long productId) {
        long uid = SecurityUtil.getUserId();
        User u = this.userRepository.findById(uid).orElseThrow(() -> new UserNotFoundException("User không tồn tại"));
        boolean exists = this.cartRepository.existsById(new CartId(uid, productId));
        if (!exists) {
            throw new ResourceInvalidException("Sản phẩm không tồn tại trong giỏ hàng");
        }
        CartId cartId = new CartId(uid, productId);
        this.cartRepository.deleteById(cartId);
    }
    @Override
    public PaginationDTO getCartByCurrentUser(Pageable pageable) {
        long uid = SecurityUtil.getUserId();
        User u = this.userRepository.findById(uid).orElseThrow(() -> new UserNotFoundException("User không tồn tại"));

        Page<CartItemDTO> cartItems = this.cartRepository.findCartItemsByUserId(uid, pageable);
        return this.paginationHelper.fetchAllEntities(cartItems);
    }
    @Override
    public List<CartItemDTO> getCartItemsByProductIds(List<Long> productIds, Pageable pageable) {
        long uid = SecurityUtil.getUserId();
        User u = this.userRepository.findById(uid).orElseThrow(() -> new UserNotFoundException("User không tồn tại"));
        return this.cartRepository.findCartItemsByUserIdAndProductId(uid, productIds, pageable);
    }
    @Override
    @Transactional
    public void deleteSelectedItems(List<Long> productIds) {
        long uid = SecurityUtil.getUserId();
        List<CartId> cartIds = productIds.stream()
                .map(productId -> new CartId(uid, productId))
                .toList();
        cartRepository.deleteByIdIn(cartIds);
    }
    @Override
    public long countProductInCart(long userId) {
        log.debug("Counting products in cart for user ID: {}", userId);
        long count = this.cartRepository.countById_UserId(userId);
        log.debug("Found {} products in cart for user ID: {}", count, userId);
        return count;
    }
}
