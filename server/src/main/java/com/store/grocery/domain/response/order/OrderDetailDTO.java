package com.store.grocery.domain.response.order;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.NumberFormat;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {
    private long productId;
    private String productName;
    private int quantity;
    private Double unit_price;
    private String imageUrl;

}
