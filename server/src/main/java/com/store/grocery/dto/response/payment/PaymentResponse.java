package com.store.grocery.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;

public abstract class PaymentResponse {
    @Builder
    @AllArgsConstructor
    public static class VNPayResponse {
        public String code;
        public String message;
        public String paymentUrl;
    }
}
