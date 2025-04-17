package com.store.grocery.service.impl;

import com.store.grocery.domain.OrderDetail;
import com.store.grocery.dto.response.order.OrderDetailResponse;
import com.store.grocery.mapper.OrderDetailMapper;
import com.store.grocery.repository.OrderDetailRepository;
import com.store.grocery.service.OrderDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {
    private final OrderDetailRepository orderDetailRepository;
    private final OrderDetailMapper orderDetailMapper;
    @Override
    public List<OrderDetailResponse> getOrderDetailById(long orderId) {
        log.info("Get order detail by order id: {}", orderId);
        List<OrderDetail> orderDetails = this.orderDetailRepository.findByOrderId(orderId);
        return orderDetails.stream()
                .map(orderDetailMapper::toOrderDetailResponse)
                .toList();
    }
}
