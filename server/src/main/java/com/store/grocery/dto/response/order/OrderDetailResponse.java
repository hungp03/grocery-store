package com.store.grocery.dto.response.order;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    private long productId;
    private String productName;
    private int quantity;
    private Double unit_price;
    private String imageUrl;

}
