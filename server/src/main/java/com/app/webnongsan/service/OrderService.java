package com.app.webnongsan.service;

import com.app.webnongsan.domain.Order;
import com.app.webnongsan.domain.request.CheckoutRequestDTO;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.domain.response.order.OrderDTO;
import com.app.webnongsan.domain.response.order.WeeklyRevenue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;


public interface OrderService {
    Optional<OrderDTO> findOrder(long id);
    PaginationDTO getAll(Specification<Order> spec, Pageable pageable);
    void updateOrderStatus(Long orderId, int status);
    void create(CheckoutRequestDTO request);
    PaginationDTO getOrdersByCurrentUser(Integer status, Pageable pageable);
    List<WeeklyRevenue> getMonthlyRevenue(int month, int year);
    List<Object> getOverviewStats();
}
