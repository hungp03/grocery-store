package com.store.grocery.mapper;

import com.store.grocery.domain.Product;
import com.store.grocery.dto.response.product.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setProduct_name(product.getProductName());
        response.setPrice(product.getPrice());
        response.setImageUrl(product.getImageUrl());
        response.setQuantity(product.getQuantity());
        response.setUnit(product.getUnit());
        response.setSold(product.getSold());
        response.setRating(product.getRating());
        response.setDescription(product.getDescription());

        if (product.getCategory() != null) {
            response.setCategory(product.getCategory().getSlug());
        }

        return response;
    }
}
