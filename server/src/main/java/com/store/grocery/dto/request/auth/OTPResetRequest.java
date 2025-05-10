package com.store.grocery.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OTPResetRequest {
    @NotBlank(message = "Không được để trống email")
    @Email(message = "Email không hợp lệ")
    private String email;
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải có đúng 6 chữ số")
    private String otp;
}
