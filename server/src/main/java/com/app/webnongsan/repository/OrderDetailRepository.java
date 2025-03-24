package com.app.webnongsan.repository;

import com.app.webnongsan.domain.OrderDetail;

import com.app.webnongsan.domain.OrderDetailId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId>, JpaSpecificationExecutor<OrderDetail> {
    List<OrderDetail> findByOrderId(Long orderId);
//    Page<OrderDetail> findByOrderId(Long orderId, Pageable pageable);
}

