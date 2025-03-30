package com.store.grocery.controller;

import com.store.grocery.domain.Order;

import com.store.grocery.domain.request.order.CheckoutRequestDTO;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.order.OrderDTO;
import com.store.grocery.domain.response.order.WeeklyRevenue;
import com.store.grocery.service.EmailService;
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
    private final EmailService emailService;

    @GetMapping("all-orders")
    @ApiMessage("Get all Orders")
    public ResponseEntity<PaginationDTO> getAll(@Filter Specification<Order> spec, Pageable pageable) {
        return ResponseEntity.ok(this.orderService.getAll(spec, pageable));
    }

    @GetMapping("order-info/{orderId}")
    @ApiMessage("Get order information")
    public ResponseEntity<Optional<OrderDTO>> getOrderInfo(@PathVariable("orderId") long orderId) {
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
    public ResponseEntity<Void> create(@RequestBody @Valid CheckoutRequestDTO request) {
        orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("orders")
    @ApiMessage("Get orders by user")
    public ResponseEntity<PaginationDTO> getOrderByUser(
            Pageable pageable,
            @RequestParam(value = "status", required = false) Integer status
    ) throws ResourceInvalidException {
        return ResponseEntity.ok(this.orderService.getOrdersByCurrentUser(status, pageable));
    }

    @GetMapping("/monthly-orders-revenue")
    @ApiMessage("Get data for monthly revenue chart")
    public ResponseEntity<List<WeeklyRevenue>> getMonthlyRevenue(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(this.orderService.getMonthlyRevenue(month, year));
    }

    @GetMapping("/admin/summary")
    @ApiMessage("Get ...")
    public ResponseEntity<List<Object>> getOverview() {
        return ResponseEntity.of(Optional.ofNullable(this.orderService.getOverviewStats()));
    }
}
