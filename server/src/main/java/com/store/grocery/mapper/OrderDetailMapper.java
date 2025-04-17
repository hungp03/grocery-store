package com.store.grocery.mapper;

import com.store.grocery.domain.OrderDetail;
import com.store.grocery.dto.response.order.OrderDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "product.price", target = "unit_price")
    @Mapping(source = "product.imageUrl", target = "imageUrl")
    OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail);
}

