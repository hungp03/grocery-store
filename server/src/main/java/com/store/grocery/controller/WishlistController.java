package com.store.grocery.controller;

import com.store.grocery.domain.Wishlist;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.service.WishlistService;
import com.store.grocery.util.annotation.ApiMessage;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v2")
@AllArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    @PostMapping("wishlist")
    @ApiMessage("Add wishlist")
    public ResponseEntity<Wishlist> add(@RequestBody Wishlist w){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.wishlistService.addWishlist(w));
    }

    @DeleteMapping("wishlist/{pid}")
    @ApiMessage("Delete wishlist")
    public ResponseEntity<Void> delete(@PathVariable("pid") long productId){
        this.wishlistService.deleteWishlist(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("wishlist")
    @ApiMessage("Get list wishlist")
    public ResponseEntity<PaginationDTO> getAll(Pageable pageable){
        return ResponseEntity.ok(this.wishlistService.getWishlistsByCurrentUser(pageable));
    }
}
