package com.store.grocery.controller;

import com.store.grocery.dto.response.order.OrderDetailResponse;
import com.store.grocery.service.OrderDetailService;
import com.store.grocery.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v2")
@RequiredArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;

    @GetMapping("order-detail/{orderId}")
    @ApiMessage("Get order detail")
    public ResponseEntity<List<OrderDetailResponse>> getAllOrderDetailsByOrderId(@PathVariable("orderId") long orderId) {
        return ResponseEntity.ok(this.orderDetailService.getOrderDetailById(orderId));
    }
}
