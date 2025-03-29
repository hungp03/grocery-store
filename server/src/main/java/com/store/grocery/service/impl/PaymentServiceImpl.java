package com.store.grocery.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.grocery.config.VNPAYConfig;
import com.store.grocery.domain.request.order.CheckoutRequestDTO;
import com.store.grocery.domain.response.payment.PaymentDTO;
import com.store.grocery.service.OrderService;
import com.store.grocery.service.PaymentService;
import com.store.grocery.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final VNPAYConfig vnPayConfig;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    @Override
    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request) {
        long amount = Integer.parseInt(request.getParameter("amount")) * 100L;
        String bankCode = request.getParameter("bankCode");
        String orderData = request.getParameter("orderData");
        String decodedData = URLDecoder.decode(new String(Base64.getDecoder().decode(orderData)), StandardCharsets.UTF_8);
        Long orderId = orderService.create(getOrderData(decodedData));
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang:" + orderId);
        //build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return PaymentDTO.VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
    }

    @Override
    public CheckoutRequestDTO getOrderData(String orderData) {
        try {
            return objectMapper.readValue(orderData, CheckoutRequestDTO.class);
        } catch (Exception e) {
            log.error("Error parsing order data: {}", e.getMessage());
            throw new RuntimeException("Invalid order data");
        }
    }
}
