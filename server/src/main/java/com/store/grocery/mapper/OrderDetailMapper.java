package com.store.grocery.mapper;

import com.store.grocery.domain.OrderDetail;
import com.store.grocery.dto.response.order.OrderDetailResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderDetailMapper {

    public OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail) {
        if (orderDetail == null) {
            return null;
        }
        OrderDetailResponse response = new OrderDetailResponse();
        response.setProductId(orderDetail.getProduct().getId());
        response.setProductName(orderDetail.getProduct().getProductName());
        response.setQuantity(orderDetail.getQuantity());
        response.setUnitPrice(orderDetail.getUnitPrice());
        response.setImageUrl(orderDetail.getProduct().getImageUrl());
        return response;
    }
}
