package com.store.grocery.controller;

import com.store.grocery.domain.response.order.OrderDetailDTO;
import com.store.grocery.service.OrderDetailService;
import com.store.grocery.util.annotation.ApiMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v2")
@AllArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;

    @GetMapping("order-detail/{orderId}")
    @ApiMessage("Get all Order Details by Order ID")
    public ResponseEntity<List<OrderDetailDTO>> getAllOrderDetailsByOrderId(@PathVariable("orderId") long orderId) {
        return ResponseEntity.ok(this.orderDetailService.getOrderDetailById(orderId));
    }
}
