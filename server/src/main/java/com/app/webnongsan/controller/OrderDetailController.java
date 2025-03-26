package com.app.webnongsan.controller;

import com.app.webnongsan.domain.OrderDetail;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.domain.response.order.OrderDetailDTO;
import com.app.webnongsan.service.OrderDetailService;
import com.app.webnongsan.util.annotation.ApiMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;

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
