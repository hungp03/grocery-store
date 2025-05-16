package com.store.grocery.dto.response.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse {
    private long id;
    private String productName;
    private double price;
    private String imageUrl;
    private int quantity;
    private String unit;
    private int sold;
    private double rating;
    private String description;
    private String category;
}
