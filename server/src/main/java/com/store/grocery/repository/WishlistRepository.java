package com.store.grocery.repository;

import com.store.grocery.domain.Wishlist;
import com.store.grocery.domain.WishlistId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId>, JpaSpecificationExecutor<Wishlist> {
    boolean existsById_UserIdAndId_ProductId(Long userId, Long productId);

    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Wishlist> findAll(Specification<Wishlist> spec, Pageable pageable);

//    @Query("SELECT new com.store.grocery.domain.response.wishlist.WishlistItemDTO" +
//            "(p.id, p.productName, p.price, p.imageUrl, c.name) " +
//            "FROM Wishlist w JOIN w.product p JOIN p.category c WHERE w.user.id = :userId")
//    Page<WishlistItemDTO> findWishlistItemsByUserId(@Param("userId") Long userId, Pageable pageable);
}
