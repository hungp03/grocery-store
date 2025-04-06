package com.store.grocery.service;

import com.store.grocery.dto.request.order.CheckoutRequest;

public interface EmailService {
    //    void sendMail(String email);
    void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml);
    void sendEmailFromTemplateSync(String to, String subject, String templateName, String username, Object o);
    void sendOrderEmail(long uid, CheckoutRequest checkoutRequestDTO);
}
