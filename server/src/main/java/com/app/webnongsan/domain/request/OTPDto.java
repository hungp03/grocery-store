package com.app.webnongsan.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OTPDto {
    @Email(message = "Email không hợp lệ")
    private String email;
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải có đúng 6 chữ số")
    private String otp;
}
