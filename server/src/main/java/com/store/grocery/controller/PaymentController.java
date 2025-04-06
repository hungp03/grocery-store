package com.store.grocery.controller;

import com.store.grocery.dto.response.ResponseObject;
import com.store.grocery.dto.response.payment.PaymentResponse;
import com.store.grocery.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v2/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Value("${vnpay.success-url}")
    private String sucessUrl;
    @Value("${vnpay.failure-url}")
    private String failureUrl;

    @PostMapping("/vn-pay")
    public ResponseObject<PaymentResponse.VNPayResponse> pay(HttpServletRequest request) {
        return new ResponseObject<>(HttpStatus.OK, "Success", paymentService.createVnPayPayment(request));
    }

    @GetMapping("/vn-pay-callback")
    public ResponseEntity<?> payCallbackHandler(HttpServletRequest request) {
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")) {
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", sucessUrl)
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", failureUrl)
                    .build();
        }
    }
}

