package com.store.grocery.dto.response.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchProductResponse {
    private long id;
    private String product_name;
    private double price;
    private String imageUrl;
    private String category;
    private double rating;
    private int quantity;

    public SearchProductResponse(long id, String product_name, double price, String imageUrl, String category) {
        this.id = id;
        this.product_name = product_name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public SearchProductResponse(long id, String product_name, double price, String imageUrl, String category, double rating, int quantity) {
        this.id = id;
        this.product_name = product_name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.rating = rating;
        this.quantity = quantity;
    }
}
