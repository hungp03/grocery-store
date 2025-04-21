package com.store.grocery.mapper;

import com.store.grocery.domain.Order;
import com.store.grocery.dto.response.order.MyOrderResponse;
import com.store.grocery.dto.response.order.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderTime(order.getOrderTime());
        response.setDeliveryTime(order.getDeliveryTime());
        response.setStatus(order.getStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setAddress(order.getAddress());
        response.setPhone(order.getPhone());
        response.setTotalPrice(order.getTotalPrice());

        if (order.getUser() != null) {
            response.setUserEmail(order.getUser().getEmail());
            response.setUserName(order.getUser().getName());
            response.setUserId(order.getUser().getId());
        }

        return response;
    }

    public MyOrderResponse toMyOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        MyOrderResponse response = new MyOrderResponse();
        response.setId(order.getId());
        response.setOrderTime(order.getOrderTime());
        response.setDeliveryTime(order.getDeliveryTime());
        response.setStatus(order.getStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setAddress(order.getAddress());
        response.setPhone(order.getPhone());
        response.setTotalPrice(order.getTotalPrice());
        return response;
    }
}

