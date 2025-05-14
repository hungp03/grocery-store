package com.store.grocery.controller;

import com.store.grocery.domain.Wishlist;
import com.store.grocery.dto.request.wishlist.AddWishlistRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.wishlist.WishlistStatusResponse;
import com.store.grocery.service.WishlistService;
import com.store.grocery.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v2")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    @PostMapping("wishlist")
    @ApiMessage("Add wishlist")
    public ResponseEntity<Wishlist> add(@Valid @RequestBody AddWishlistRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.wishlistService.addWishlist(request));
    }

    @DeleteMapping("wishlist/{pid}")
    @ApiMessage("Delete wishlist")
    public ResponseEntity<Void> delete(@PathVariable("pid") long productId){
        this.wishlistService.deleteWishlist(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("wishlist")
    @ApiMessage("Get list wishlist")
    public ResponseEntity<PaginationResponse> getAll(Pageable pageable){
        return ResponseEntity.ok(this.wishlistService.getWishlistsByCurrentUser(pageable));
    }

    @GetMapping("wishlist/status/{pid}")
    @ApiMessage("Get wishlist status of a product")
    public ResponseEntity<WishlistStatusResponse> getWishlistStatus(@PathVariable("pid") long productId) {
        boolean isWishlisted = this.wishlistService.isProductWishlisted(productId);
        return ResponseEntity.ok(new WishlistStatusResponse(productId, isWishlisted));
    }

}
