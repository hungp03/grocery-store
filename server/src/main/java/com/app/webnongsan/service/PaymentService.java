package com.app.webnongsan.service;


import com.app.webnongsan.domain.response.payment.PaymentDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request);
}

