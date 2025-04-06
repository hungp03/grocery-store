package com.store.grocery.service;
import com.store.grocery.dto.response.order.OrderDetailResponse;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetailResponse> getOrderDetailById(long orderId);
}
