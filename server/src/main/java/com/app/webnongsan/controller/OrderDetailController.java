package com.app.webnongsan.controller;

import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.service.OrderDetailService;
import com.app.webnongsan.util.annotation.ApiMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;
@RestController
@RequestMapping("api/v2")
@AllArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;

    @GetMapping("OrderDetails/{orderId}")
    @ApiMessage("Get all Order Details by Order ID")
    public ResponseEntity<PaginationDTO> getAllOrderDetailsByOrderId(@PathVariable("orderId") long orderId, Pageable pageable) {
        PaginationDTO paginationDTO = this.orderDetailService.getOrderDetailById(pageable, orderId);
        return ResponseEntity.ok(paginationDTO);
    }
}
