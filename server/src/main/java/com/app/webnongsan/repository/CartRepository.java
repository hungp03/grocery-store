package com.app.webnongsan.repository;

import com.app.webnongsan.domain.Cart;
import com.app.webnongsan.domain.CartId;
import com.app.webnongsan.domain.response.cart.CartItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId>, JpaSpecificationExecutor<Cart> {
    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Cart> findAll(Specification<Cart> spec, Pageable pageable);

    default Page<CartItemDTO> findCartItemsByUserId(Long userId, Pageable pageable) {
        Specification<Cart> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("id").get("userId"), userId));
            query.orderBy(cb.desc(root.get("timestamp")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable).map(cart -> {
            return new CartItemDTO(
                    cart.getProduct().getId(),
                    cart.getProduct().getProductName(),
                    cart.getProduct().getPrice(),
                    cart.getQuantity(),
                    cart.getProduct().getImageUrl(),
                    cart.getProduct().getCategory().getName(),
                    cart.getProduct().getQuantity()
            );
        });
    }

    default List<CartItemDTO> findCartItemsByUserIdAndProductId(Long userId, List<Long> productIds, Pageable pageable) {
        Specification<Cart> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("id").get("userId"), userId));
            predicates.add(root.get("product").get("id").in(productIds));
            query.orderBy(cb.desc(root.get("timestamp")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable).getContent().stream()
                .map(cart -> new CartItemDTO(
                        cart.getProduct().getId(),
                        cart.getProduct().getProductName(),
                        cart.getProduct().getPrice(),
                        cart.getQuantity(),
                        cart.getProduct().getImageUrl(),
                        cart.getProduct().getCategory().getName(),
                        cart.getProduct().getQuantity()
                ))
                .toList();
    }

    long countById_UserId(Long userId);

    void deleteByIdIn(List<CartId> cartIds);

//    @Query("SELECT new com.app.webnongsan.domain.response.cart.CartItemDTO" +
//            "(p.id, p.productName, p.price, c.quantity, p.imageUrl, cate.name, p.quantity) " +
//            "FROM Cart c JOIN c.product p JOIN p.category cate " +
//            "WHERE c.user.id = :userId " +
//            "ORDER BY c.timestamp DESC")
//    Page<CartItemDTO> findCartItemsByUserId(@Param("userId") Long userId, Pageable pageable);
//    @Query("SELECT new com.app.webnongsan.domain.response.cart.CartItemDTO" +
//            "(p.id, p.productName, p.price, c.quantity, p.imageUrl, cate.name, p.quantity) " +
//            "FROM Cart c JOIN c.product p JOIN p.category cate " +
//            "WHERE c.user.id = :userId AND p.id IN :productIds " +
//            "ORDER BY c.timestamp DESC")
//    List<CartItemDTO> findCartItemsByUserIdAndProductId(@Param("userId") Long userId, @Param("productIds") List<Long> productIds, Pageable pageable);

}
