package com.store.grocery.repository;

import com.store.grocery.domain.OrderDetail;

import com.store.grocery.domain.OrderDetailId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId>, JpaSpecificationExecutor<OrderDetail> {
    List<OrderDetail> findByOrderId(Long orderId);
}

