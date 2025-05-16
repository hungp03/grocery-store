package com.store.grocery.dto.response.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;


@Getter
@Setter
public class OrderResponse {
    private long id;
    private Instant orderTime;
    private Instant deliveryTime;
    private int status;
    private String paymentMethod;
    private String address;
    private String phone;
    private double totalPrice;
    private String userEmail;
    private String userName;
    private Long userId;
}
