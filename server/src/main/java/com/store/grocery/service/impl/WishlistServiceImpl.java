package com.store.grocery.service.impl;

import com.store.grocery.domain.Product;
import com.store.grocery.domain.User;
import com.store.grocery.domain.Wishlist;
import com.store.grocery.domain.WishlistId;
import com.store.grocery.dto.request.wishlist.AddWishlistRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.wishlist.WishlistItemResponse;
import com.store.grocery.repository.WishlistRepository;
import com.store.grocery.service.ProductService;
import com.store.grocery.service.UserService;
import com.store.grocery.service.WishlistService;
import com.store.grocery.util.SecurityUtil;
import com.store.grocery.util.exception.DuplicateResourceException;
import com.store.grocery.util.exception.ResourceInvalidException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductService productService;
    private final UserService userService;

    @Override
    public Wishlist addWishlist(AddWishlistRequest request) {
        long uid = SecurityUtil.getUserId();
        User u = this.userService.findById(uid);
        Product p = this.productService.findByIdAndIsActiveTrue(request.getProductId());
        log.info("Adding product to wishlist: {} by uid {}", p.getId(), uid);
        boolean exists = wishlistRepository.existsById_UserIdAndId_ProductId(u.getId(), p.getId());
        if (exists) {
            log.warn("Product already exists in wishlist");
            throw new DuplicateResourceException("Sản phẩm đã có trong danh sách yêu thích");
        }
        Wishlist wishlist = Wishlist.builder()
                .id(new WishlistId(u.getId(), p.getId()))
                .user(u)
                .product(p)
                .build();
        return this.wishlistRepository.save(wishlist);
    }

    @Override
    public void deleteWishlist(Long productId) {
        long uid = SecurityUtil.getUserId();
        log.info("Deleting product from wishlist: {} by uid {}", productId, uid);
        boolean exists = wishlistRepository.existsById_UserIdAndId_ProductId(uid, productId);
        if (!exists) {
            throw new ResourceInvalidException("Sản phẩm không tồn tại trong danh sách yêu thích");
        }

        WishlistId wishlistId = new WishlistId(uid, productId);
        wishlistRepository.deleteById(wishlistId);
        log.info("Product has been deleted from wishlist");
    }

    @Override
    public PaginationResponse getWishlistsByCurrentUser(Pageable pageable) {
        log.info("Get wishlist by current user");
        long uid = SecurityUtil.getUserId();

        Page<WishlistItemResponse> wishlistItems = findWishlistItemsByUserId(uid, pageable);
        return PaginationResponse.from(wishlistItems, pageable);
    }

    private Page<WishlistItemResponse> findWishlistItemsByUserId(Long userId, Pageable pageable) {
        // Define specification in service
        Specification<Wishlist> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("id").get("userId"), userId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Query repository and map to DTO
        return wishlistRepository.findAll(spec, pageable).map(wishlist -> {
            Product product = wishlist.getProduct();
            return new WishlistItemResponse(
                    product.getId(),
                    product.getProductName(),
                    product.getPrice(),
                    product.getImageUrl(),
                    product.getCategory().getSlug(),
                    product.isActive()
            );
        });
    }
}
