package com.store.grocery.controller;

import com.store.grocery.domain.Order;

import com.store.grocery.dto.request.order.OrderRequest;
import com.store.grocery.dto.request.order.OrderStatusUpdateRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.order.OrderResponse;
import com.store.grocery.dto.response.order.WeeklyRevenueResponse;
import com.store.grocery.service.OrderService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.store.grocery.util.annotation.ApiMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import com.turkraft.springfilter.boot.Filter;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("api/v2")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("orders")
    @ApiMessage("Get all Orders")
    public ResponseEntity<PaginationResponse> getAllOrder(@Filter Specification<Order> spec, Pageable pageable) {
        return ResponseEntity.ok(this.orderService.getAllOrder(spec, pageable));
    }

    @GetMapping("orders/me")
    @ApiMessage("Get orders by user")
    public ResponseEntity<PaginationResponse> getMyOrders(
            Pageable pageable,
            @RequestParam(value = "status", required = false) Integer status){
        return ResponseEntity.ok(this.orderService.getMyOrders(status, pageable));
    }

    @GetMapping("orders/{orderId}/info")
    @ApiMessage("Get order information")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable("orderId") long orderId) {
        return ResponseEntity.ok(this.orderService.findOrder(orderId));
    }

    @PatchMapping("/orders/{orderId}/status")
    @ApiMessage("Update order status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequest request) {
        orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/checkout")
    @ApiMessage("Create a order")
    public ResponseEntity<Void> createOrder(@RequestBody @Valid OrderRequest request) {
        orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/reports/revenue/monthly")
    @ApiMessage("Get data for monthly revenue chart")
    public ResponseEntity<List<WeeklyRevenueResponse>> getMonthlyRevenue(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(this.orderService.getMonthlyRevenue(month, year));
    }

    @GetMapping("/admin/overview")
    @ApiMessage("Get admin overview")
    public ResponseEntity<List<Object>> getAdminOverview() {
        return ResponseEntity.of(Optional.ofNullable(this.orderService.getOverviewStats()));
    }
}
