package com.store.grocery.service;

import com.store.grocery.dto.request.order.OrderRequest;
import com.store.grocery.dto.response.payment.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentResponse.VNPayResponse createVnPayPayment(HttpServletRequest request);
    OrderRequest getOrderData(String orderData);
}

