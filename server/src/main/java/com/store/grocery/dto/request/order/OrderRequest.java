package com.store.grocery.dto.request.order;

import com.store.grocery.dto.response.order.OrderDetailResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {
    private String address;
    private String phone;
    private String paymentMethod;
    private Double totalPrice;
    private List<OrderDetailResponse> items;
}
