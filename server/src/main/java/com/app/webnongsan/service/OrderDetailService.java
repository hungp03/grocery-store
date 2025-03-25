package com.app.webnongsan.service;
import com.app.webnongsan.domain.response.order.OrderDetailDTO;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetailDTO> getOrderDetailById(long orderId);
}
