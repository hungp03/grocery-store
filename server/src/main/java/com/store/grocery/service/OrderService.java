package com.store.grocery.service;

import com.store.grocery.domain.Order;
import com.store.grocery.dto.request.order.OrderRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.order.OrderResponse;
import com.store.grocery.dto.response.order.WeeklyRevenueResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;


public interface OrderService {
    OrderResponse findOrder(long id);
    PaginationResponse getAllOrder(Specification<Order> spec, Pageable pageable);
    void updateOrderStatus(Long orderId, int status);
    Long create(OrderRequest request);
    PaginationResponse getMyOrders(Integer status, Pageable pageable);
    List<WeeklyRevenueResponse> getMonthlyRevenue(int month, int year);
    List<Object> getOverviewStats();
}
