package com.store.grocery.service;

import com.store.grocery.dto.request.order.CheckoutRequest;
import com.store.grocery.dto.response.payment.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentResponse.VNPayResponse createVnPayPayment(HttpServletRequest request);
    CheckoutRequest getOrderData(String orderData);
}

