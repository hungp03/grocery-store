package com.app.webnongsan.domain.request;

import com.app.webnongsan.domain.response.order.OrderDetailDTO;
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
