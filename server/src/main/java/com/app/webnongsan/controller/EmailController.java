package com.app.webnongsan.controller;

import com.app.webnongsan.domain.User;
import com.app.webnongsan.domain.request.CheckoutRequestDTO;
import com.app.webnongsan.domain.response.RestResponse;
import com.app.webnongsan.domain.response.order.OrderDetailDTO;
import com.app.webnongsan.repository.UserRepository;
import com.app.webnongsan.service.EmailService;
import com.app.webnongsan.util.SecurityUtil;
import com.app.webnongsan.util.annotation.ApiMessage;
import com.app.webnongsan.util.exception.ResourceInvalidException;
import com.app.webnongsan.util.exception.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v2")
@AllArgsConstructor
public class EmailController {
    private final EmailService emailService;
    private final UserRepository userRepository;

    @PostMapping("checkout/email")
    @ApiMessage("Create a checkout email")
    public ResponseEntity<Void> sendOrderEmail(@RequestBody CheckoutRequestDTO checkoutRequestDTO) {
        try {
            long uid = SecurityUtil.getUserId();
            User u = this.userRepository.findById(uid)
                    .orElseThrow(() -> new UserNotFoundException("User không tồn tại"));

            String templateName = "checkout";
            String subject = "Thông tin đơn hàng";

            emailService.sendEmailFromTemplateSyncCheckout(
                    u.getEmail(), subject, templateName, u.getName(),
                    checkoutRequestDTO.getAddress(), checkoutRequestDTO.getPhone(),
                    checkoutRequestDTO.getPaymentMethod(), checkoutRequestDTO.getTotalPrice(),
                    checkoutRequestDTO.getItems()
            );

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
