package com.app.webnongsan.service;

import com.app.webnongsan.domain.Product;
import com.app.webnongsan.domain.User;
import com.app.webnongsan.domain.Wishlist;
import com.app.webnongsan.domain.WishlistId;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.domain.response.wishlist.WishlistItemDTO;
import com.app.webnongsan.repository.ProductRepository;
import com.app.webnongsan.repository.WishlistRepository;
import com.app.webnongsan.util.PaginationHelper;
import com.app.webnongsan.util.SecurityUtil;
import com.app.webnongsan.util.exception.DuplicateResourceException;
import com.app.webnongsan.util.exception.ResourceInvalidException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final PaginationHelper paginationHelper;
    private final UserService userService;
    public Wishlist addWishlist(Wishlist w){
        long uid = SecurityUtil.getUserId();
        User u = this.userService.getUserById(uid);
        Product p = this.productRepository.findById(w.getId().getProductId()).orElseThrow(() -> new ResourceInvalidException("Product không tồn tại"));
        boolean exists = wishlistRepository.existsByUserIdAndProductId(u.getId(), p.getId());

        if (exists) {
            throw new DuplicateResourceException("Sản phẩm đã có trong danh sách yêu thích");
        }

        w.setUser(u);
        w.setProduct(p);
        return this.wishlistRepository.save(w);
    }

    public void deleteWishlist(Long productId){
        long uid = SecurityUtil.getUserId();
        User user = this.userService.getUserById(uid);
        boolean exists = wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
        if (!exists) {
            throw new ResourceInvalidException("Sản phẩm không tồn tại trong danh sách yêu thích");
        }

        WishlistId wishlistId = new WishlistId(user.getId(), productId);
        wishlistRepository.deleteById(wishlistId);
    }

    public PaginationDTO getWishlistsByCurrentUser( Pageable pageable) throws ResourceInvalidException {
        long uid = SecurityUtil.getUserId();
        User user = this.userService.getUserById(uid);
        Page<WishlistItemDTO> wishlistItems = this.wishlistRepository.findWishlistItemsByUserId(user.getId(), pageable);
        return this.paginationHelper.fetchAllEntities(wishlistItems);
    }
}
