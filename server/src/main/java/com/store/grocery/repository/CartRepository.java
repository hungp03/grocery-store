package com.store.grocery.repository;

import com.store.grocery.domain.Cart;
import com.store.grocery.domain.CartId;
import com.store.grocery.dto.response.cart.CartItemResponse;
import com.store.grocery.dto.response.cart.PreOrderCartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId>, JpaSpecificationExecutor<Cart> {
    @EntityGraph(attributePaths = {"product", "product.category"})
    Page<Cart> findAll(Specification<Cart> spec, Pageable pageable);

    void deleteByIdIn(List<CartId> cartIds);

    @Query("SELECT new com.store.grocery.dto.response.cart.CartItemResponse(" +
            "p.id, p.productName, p.price, c.quantity, p.imageUrl, cate.slug, p.quantity, p.isActive) " +
            "FROM Cart c JOIN c.product p JOIN p.category cate " +
            "WHERE c.user.id = :userId " +
            "ORDER BY c.timestamp DESC")
    Page<CartItemResponse> findCartItemsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.store.grocery.dto.response.cart.PreOrderCartItem" +
            "(p.id, p.productName, p.price, c.quantity) " +
            "FROM Cart c JOIN c.product p " +
            "WHERE c.user.id = :userId AND p.id IN :productIds AND p.isActive = true " +
            "ORDER BY c.timestamp DESC")
    List<PreOrderCartItem> findCartItemsByUserIdAndProductId(@Param("userId") Long userId, @Param("productIds") List<Long> productIds, Pageable pageable);

}
