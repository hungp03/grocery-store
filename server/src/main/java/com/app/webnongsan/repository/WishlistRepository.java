package com.app.webnongsan.repository;

import com.app.webnongsan.domain.Category;
import com.app.webnongsan.domain.Product;
import com.app.webnongsan.domain.Wishlist;
import com.app.webnongsan.domain.WishlistId;
import com.app.webnongsan.domain.response.wishlist.WishlistItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId>, JpaSpecificationExecutor<Wishlist> {
    boolean existsById_UserIdAndId_ProductId(Long userId, Long productId);

    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Wishlist> findAll(Specification<Wishlist> spec, Pageable pageable);
    
    default Page<WishlistItemDTO> findWishlistItemsByUserId(Long userId, Pageable pageable) {
        Specification<Wishlist> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("id").get("userId"), userId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return findAll(spec, pageable).map(wishlist -> {
            Product product = wishlist.getProduct();
            return new WishlistItemDTO(
                    product.getId(),
                    product.getProductName(),
                    product.getPrice(),
                    product.getImageUrl(),
                    product.getCategory().getName()
            );
        });
    }

//    @Query("SELECT new com.app.webnongsan.domain.response.wishlist.WishlistItemDTO" +
//            "(p.id, p.productName, p.price, p.imageUrl, c.name) " +
//            "FROM Wishlist w JOIN w.product p JOIN p.category c WHERE w.user.id = :userId")
//    Page<WishlistItemDTO> findWishlistItemsByUserId(@Param("userId") Long userId, Pageable pageable);
}
