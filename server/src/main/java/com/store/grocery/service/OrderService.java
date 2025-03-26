package com.store.grocery.service;

import com.store.grocery.domain.Order;
import com.store.grocery.domain.request.order.CheckoutRequestDTO;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.order.OrderDTO;
import com.store.grocery.domain.response.order.WeeklyRevenue;
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
