package com.store.grocery.controller;

import com.store.grocery.domain.Order;

import com.store.grocery.dto.request.order.CheckoutRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.order.OrderResponse;
import com.store.grocery.dto.response.order.WeeklyRevenueResponse;
import com.store.grocery.service.OrderService;
import com.store.grocery.util.exception.ResourceInvalidException;
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

    @GetMapping("all-orders")
    @ApiMessage("Get all Orders")
    public ResponseEntity<PaginationResponse> getAll(@Filter Specification<Order> spec, Pageable pageable) {
        return ResponseEntity.ok(this.orderService.getAll(spec, pageable));
    }

    @GetMapping("order-info/{orderId}")
    @ApiMessage("Get order information")
    public ResponseEntity<Optional<OrderResponse>> getOrderInfo(@PathVariable("orderId") long orderId) {
        return ResponseEntity.ok(this.orderService.findOrder(orderId));
    }

    @PutMapping("update-order-status/{orderId}")
    @ApiMessage("Update order status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam("status") int status) {

        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("checkout")
    @ApiMessage("Create a checkout payment")
    public ResponseEntity<Void> create(@RequestBody @Valid CheckoutRequest request) {
        orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("orders")
    @ApiMessage("Get orders by user")
    public ResponseEntity<PaginationResponse> getOrderByUser(
            Pageable pageable,
            @RequestParam(value = "status", required = false) Integer status
    ) throws ResourceInvalidException {
        return ResponseEntity.ok(this.orderService.getOrdersByCurrentUser(status, pageable));
    }

    @GetMapping("/monthly-orders-revenue")
    @ApiMessage("Get data for monthly revenue chart")
    public ResponseEntity<List<WeeklyRevenueResponse>> getMonthlyRevenue(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(this.orderService.getMonthlyRevenue(month, year));
    }

    @GetMapping("/admin/summary")
    @ApiMessage("Get ...")
    public ResponseEntity<List<Object>> getOverview() {
        return ResponseEntity.of(Optional.ofNullable(this.orderService.getOverviewStats()));
    }
}
