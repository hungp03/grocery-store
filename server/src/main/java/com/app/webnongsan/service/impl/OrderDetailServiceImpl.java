package com.app.webnongsan.service.impl;

import com.app.webnongsan.domain.OrderDetail;
import com.app.webnongsan.domain.response.order.OrderDetailDTO;
import com.app.webnongsan.repository.OrderDetailRepository;
import com.app.webnongsan.service.OrderDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public List<OrderDetailDTO> getOrderDetailById(long orderId) {
        List<OrderDetail> orderDetails = this.orderDetailRepository.findByOrderId(orderId);
        return orderDetails.stream()
                .map(this::convertToOrderDetailDTO)
                .toList();
    }

    private OrderDetailDTO convertToOrderDetailDTO(OrderDetail orderDetail) {
        OrderDetailDTO res = new OrderDetailDTO();
        res.setProductId(orderDetail.getProduct().getId());
        res.setQuantity(orderDetail.getQuantity());
        res.setProductName(orderDetail.getProduct().getProductName());
        res.setUnit_price(orderDetail.getProduct().getPrice());
        res.setImageUrl(orderDetail.getProduct().getImageUrl());
        return res;
    }
}
