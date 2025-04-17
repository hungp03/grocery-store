package com.store.grocery.mapper;

import com.store.grocery.domain.Product;
import com.store.grocery.dto.response.product.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "productName", target = "product_name")
    @Mapping(source = "category.slug", target = "category")
    ProductResponse toProductResponse(Product product);
}

