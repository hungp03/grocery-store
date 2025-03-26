package com.store.grocery.repository;
import com.store.grocery.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByUserIdAndStatus(Long userId, int status, Pageable pageable);
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Procedure
    List<Object[]> GetRevenueByWeekCycle(
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("SELECT SUM(o.total_price) FROM Order o WHERE o.status = :status")
    double sumTotalPriceByStatus(@Param("status") int status);
}

