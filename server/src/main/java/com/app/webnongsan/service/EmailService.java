package com.app.webnongsan.service;

import com.app.webnongsan.domain.request.CheckoutRequestDTO;
import com.app.webnongsan.domain.response.order.OrderDetailDTO;

import java.util.List;

public interface EmailService {
    //    void sendMail(String email);
    void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml);
    void sendEmailFromTemplateSync(String to, String subject, String templateName, String username, Object o);
    void sendEmailFromTemplateSyncCheckout(String to, String subject, String templateName, String username, String address, String phone, String paymentMethod, Double totalPrice, List<OrderDetailDTO> items);
    void sendOrderEmail(CheckoutRequestDTO checkoutRequestDTO);
}
