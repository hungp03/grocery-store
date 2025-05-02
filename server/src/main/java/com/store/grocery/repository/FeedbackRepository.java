package com.store.grocery.repository;

import com.store.grocery.domain.Feedback;
import com.store.grocery.dto.response.feedback.FeedbackResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback,Long>, JpaSpecificationExecutor<Feedback> {
    long countByProductId(Long productId);
    @Query("SELECT new com.store.grocery.dto.response.feedback.FeedbackResponse(" +
            "f.id, u.name, u.avatarUrl, " +
            "p.productName," +
            "f.ratingStar, f.description, f.status, f.updatedAt) " +
            "FROM Feedback f " +
            "JOIN f.user u " +
            "JOIN f.product p " +
            "WHERE (:status IS NULL OR f.status = :status)")
    Page<FeedbackResponse> findByStatus(Boolean status, Pageable pageable);
    @Query("SELECT new com.store.grocery.dto.response.feedback.FeedbackResponse(f.id, u.name, u.avatarUrl, p.productName, f.ratingStar, f.description, f.status, f.updatedAt) " +
            "FROM Feedback f JOIN f.user u JOIN f.product p " +
            "WHERE p.id = :productId AND f.status = true AND p.isActive = true")
    Page<FeedbackResponse> findByProductId(@Param("productId") Long productId, Pageable pageable);
    @Query("SELECT AVG(f.ratingStar) FROM Feedback f WHERE f.product.id = :productId")
    double calculateAverageRatingByProductId(@Param("productId") Long productId);
    Optional<Feedback> findByUserIdAndProductId(long userId, long productId);
}
