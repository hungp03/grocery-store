package com.store.grocery.service.impl;

import com.store.grocery.domain.User;
import com.store.grocery.domain.request.order.CheckoutRequestDTO;
import com.store.grocery.domain.response.order.OrderDetailDTO;
import com.store.grocery.repository.UserRepository;
import com.store.grocery.service.EmailService;
import com.store.grocery.util.SecurityUtil;
import com.store.grocery.util.exception.UserNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
//    private final MailSender mailSender;
    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

//    @Override
//    public void sendMail(String email){
//        SimpleMailMessage msg = new SimpleMailMessage();
//        msg.setTo(email);
//        msg.setSubject("Testing from Spring Boot");
//        msg.setText("Hello World from Spring Boot Email");
//        this.mailSender.send(msg);
//    }

    @Override
    public void sendEmailSync(String to, String subject, String content, boolean isMultipart,
                              boolean isHtml) {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
                    isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", to);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendEmailFromTemplateSync(String to, String subject, String
            templateName, String username, Object o) {
        Context context = new Context();
        context.setVariable("NAME", username);
        context.setVariable("OTP", o);
        String content = this.templateEngine.process(templateName, context);
        this.sendEmailSync(to, subject, content, false, true);
    }

    @Override
    @Async
    public void sendEmailFromTemplateSyncCheckout(String to, String subject, String templateName,
                                                  String username,String address, String phone, String paymentMethod, Double totalPrice, List<OrderDetailDTO> items) {

        String formattedTotalPrice = formatCurrency(totalPrice);
        // Lấy thời gian hiện tại và định dạng
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        String formattedDateTime = currentDateTime.format(formatter);

        Context context = new Context();
        context.setVariable("NAME", username);
        context.setVariable("TOTAL_PRICE", totalPrice);
        context.setVariable("customerName",username);
        context.setVariable("customerAddress",address);
        context.setVariable("customerPhone",phone);
        context.setVariable("customerCountry","Việt Nam");
        context.setVariable("paymentMethod",paymentMethod);
        context.setVariable("invoiceDate",formattedDateTime);
        context.setVariable("items", items);
        String content = this.templateEngine.process(templateName, context);
        this.sendEmailSync(to, subject, content, false, true);
    }

    @Override
    @Async
    public void sendOrderEmail(long uid, CheckoutRequestDTO checkoutRequestDTO) {
        try {
            User u = this.userRepository.findById(uid)
                    .orElseThrow(() -> new UserNotFoundException("User không tồn tại"));

            String templateName = "checkout";
            String subject = "Thông tin đơn hàng";

            sendEmailFromTemplateSyncCheckout(
                    u.getEmail(), subject, templateName, u.getName(),
                    checkoutRequestDTO.getAddress(), checkoutRequestDTO.getPhone(),
                    checkoutRequestDTO.getPaymentMethod(), checkoutRequestDTO.getTotalPrice(),
                    checkoutRequestDTO.getItems()
            );
            log.info("📧 Order email sent to uid {} for order total {}", uid, formatCurrency(checkoutRequestDTO.getTotalPrice()));
        } catch (Exception e) {
            log.info("Cannot send email: {}", e.getMessage());
        }
    }

    private String formatCurrency(Double amount) {
        Locale locale = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        return currencyFormatter.format(amount);
    }
}
