package com.store.grocery.dto.request.auth;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    @Email(message = "Email không hợp lệ")
    private String email;
}
