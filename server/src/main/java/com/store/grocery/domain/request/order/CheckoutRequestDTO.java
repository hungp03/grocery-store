package com.store.grocery.domain.request.order;

import com.store.grocery.domain.response.order.OrderDetailDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckoutRequestDTO {
    private String address;
    private String phone;
    private String paymentMethod;
    private Double totalPrice;
    private List<OrderDetailDTO> items;
}
