package com.app.webnongsan.controller;

import com.app.webnongsan.domain.User;
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
    @ApiMessage("Create a checkout payment")
    public ResponseEntity<RestResponse<Long>> create(
            @RequestParam("userId") Long userId,
            @RequestParam("address") String address,
            @RequestParam("phone") String phone,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("totalPrice") Double totalPrice,
            @RequestPart("items") List<OrderDetailDTO> items
    ) throws ResourceInvalidException {
        RestResponse<Long> response = new RestResponse<>();
        try {
            long uid = SecurityUtil.getUserId();
            User u = this.userRepository.findById(uid).orElseThrow(() -> new UserNotFoundException("User không tồn tại"));
            String templateName = "checkout";

            // Gửi email sau khi thanh toán thành công
            String subject = "Thông tin đơn hàng";

            emailService.sendEmailFromTemplateSyncCheckout(u.getEmail(), subject, templateName, u.getName(), address, phone, paymentMethod, totalPrice,items);
            response.setStatusCode(HttpStatus.CREATED.value());
            response.setMessage("Gửi email thành công");

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError(e.getMessage());
            response.setMessage("Có lỗi xảy ra trong quá trình thanh toán");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
