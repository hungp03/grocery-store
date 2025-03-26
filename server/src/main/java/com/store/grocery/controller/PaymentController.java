package com.store.grocery.controller;

import com.store.grocery.domain.response.ResponseObject;
import com.store.grocery.domain.response.payment.PaymentDTO;
import com.store.grocery.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v2/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @GetMapping("/vn-pay")
    public ResponseObject<PaymentDTO.VNPayResponse> pay(HttpServletRequest request) {
        return new ResponseObject<>(HttpStatus.OK, "Success", paymentService.createVnPayPayment(request));
    }

//    @GetMapping("/vn-pay-callback")
//    public ResponseEntity<?> payCallbackHandler(HttpServletRequest request) {
//        String status = request.getParameter("vnp_ResponseCode");
//        if (status.equals("00")) {
//            return ResponseEntity.status(HttpStatus.FOUND).header("Location", "http://localhost:port/payment-success") // URL của trang thành công trên client
//                    .build();
//        } else {
//            return ResponseEntity.status(HttpStatus.FOUND)
//                    .header("Location", "http://localhost:port/payment-failure") // URL của trang thất bại trên client
//                    .build();
//        }
//    }
}

