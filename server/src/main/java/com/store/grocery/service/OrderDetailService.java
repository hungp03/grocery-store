package com.store.grocery.service;
import com.store.grocery.domain.response.order.OrderDetailDTO;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetailDTO> getOrderDetailById(long orderId);
}
