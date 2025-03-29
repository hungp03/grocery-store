package com.store.grocery.service;

import com.store.grocery.domain.request.order.CheckoutRequestDTO;
import com.store.grocery.domain.response.payment.PaymentDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request);
    CheckoutRequestDTO getOrderData(String orderData);
}

