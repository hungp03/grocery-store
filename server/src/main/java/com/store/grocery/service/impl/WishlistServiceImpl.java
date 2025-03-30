package com.store.grocery.service.impl;

import com.store.grocery.domain.Product;
import com.store.grocery.domain.User;
import com.store.grocery.domain.Wishlist;
import com.store.grocery.domain.WishlistId;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.wishlist.WishlistItemDTO;
import com.store.grocery.repository.ProductRepository;
import com.store.grocery.repository.WishlistRepository;
import com.store.grocery.service.UserService;
import com.store.grocery.service.WishlistService;
import com.store.grocery.util.PaginationHelper;
import com.store.grocery.util.SecurityUtil;
import com.store.grocery.util.exception.DuplicateResourceException;
import com.store.grocery.util.exception.ResourceInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final PaginationHelper paginationHelper;
    private final UserService userService;

    @Override
    public Wishlist addWishlist(Wishlist w) {
        long uid = SecurityUtil.getUserId();
        User u = this.userService.getUserById(uid);
        Product p = this.productRepository.findById(w.getId().getProductId()).orElseThrow(() -> new ResourceInvalidException("Product không tồn tại"));
        log.info("Adding product to wishlist: {} by uid {}", p.getId(), uid);
        boolean exists = wishlistRepository.existsById_UserIdAndId_ProductId(u.getId(), p.getId());
        if (exists) {
            log.error("Product already exists in wishlist");
            throw new DuplicateResourceException("Sản phẩm đã có trong danh sách yêu thích");
        }
        w.setUser(u);
        w.setProduct(p);
        return this.wishlistRepository.save(w);
    }

    @Override
    public void deleteWishlist(Long productId) {
        long uid = SecurityUtil.getUserId();
        log.info("Deleting product from wishlist: {} by uid {}", productId, uid);
        User user = this.userService.getUserById(uid);
        boolean exists = wishlistRepository.existsById_UserIdAndId_ProductId(user.getId(), productId);
        if (!exists) {
            throw new ResourceInvalidException("Sản phẩm không tồn tại trong danh sách yêu thích");
        }

        WishlistId wishlistId = new WishlistId(user.getId(), productId);
        wishlistRepository.deleteById(wishlistId);
        log.info("Product has been deleted from wishlist");
    }

    @Override
    public PaginationDTO getWishlistsByCurrentUser(Pageable pageable) throws ResourceInvalidException {
        log.info("Get wishlist by current user");
        long uid = SecurityUtil.getUserId();
        User user = this.userService.getUserById(uid);
        Page<WishlistItemDTO> wishlistItems = this.wishlistRepository.findWishlistItemsByUserId(user.getId(), pageable);
        return this.paginationHelper.fetchAllEntities(wishlistItems);
    }
}
