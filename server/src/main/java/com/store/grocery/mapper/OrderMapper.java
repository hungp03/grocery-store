package com.store.grocery.mapper;

import com.store.grocery.domain.Order;
import com.store.grocery.dto.response.order.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    OrderResponse toOrderResponse(Order order);
}

