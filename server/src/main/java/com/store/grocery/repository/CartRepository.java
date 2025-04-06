package com.store.grocery.repository;

import com.store.grocery.domain.Cart;
import com.store.grocery.domain.CartId;
import com.store.grocery.domain.response.cart.CartItemDTO;
import com.store.grocery.domain.response.cart.SelectedProductDTO;
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
            assert query != null;
            query.orderBy(cb.desc(root.get("timestamp")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable).map(cart -> new CartItemDTO(
                cart.getProduct().getId(),
                cart.getProduct().getProductName(),
                cart.getProduct().getPrice(),
                cart.getQuantity(),
                cart.getProduct().getImageUrl(),
                cart.getProduct().getCategory().getSlug(),
                cart.getProduct().getQuantity()
        ));
    }

    default List<SelectedProductDTO> findCartItemsByUserIdAndProductId(Long userId, List<Long> productIds, Pageable pageable) {
        Specification<Cart> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("id").get("userId"), userId));
            predicates.add(root.get("product").get("id").in(productIds));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable).getContent().stream()
                .map(cart -> new SelectedProductDTO(
                        cart.getProduct().getId(),
                        cart.getProduct().getProductName(),
                        cart.getQuantity(),
                        cart.getProduct().getPrice()
                ))
                .toList();
    }

    void deleteByIdIn(List<CartId> cartIds);

//    @Query("SELECT new com.store.grocery.domain.response.cart.CartItemDTO" +
//            "(p.id, p.productName, p.price, c.quantity, p.imageUrl, cate.name, p.quantity) " +
//            "FROM Cart c JOIN c.product p JOIN p.category cate " +
//            "WHERE c.user.id = :userId " +
//            "ORDER BY c.timestamp DESC")
//    Page<CartItemDTO> findCartItemsByUserId(@Param("userId") Long userId, Pageable pageable);
//    @Query("SELECT new com.store.grocery.domain.response.cart.CartItemDTO" +
//            "(p.id, p.productName, p.price, c.quantity, p.imageUrl, cate.name, p.quantity) " +
//            "FROM Cart c JOIN c.product p JOIN p.category cate " +
//            "WHERE c.user.id = :userId AND p.id IN :productIds " +
//            "ORDER BY c.timestamp DESC")
//    List<CartItemDTO> findCartItemsByUserIdAndProductId(@Param("userId") Long userId, @Param("productIds") List<Long> productIds, Pageable pageable);

}
