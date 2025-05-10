package com.store.grocery.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDisableOTPRequest {
    @NotBlank(message = "Mã OTP không được để trống")
    @Pattern(regexp = "\\d{6}", message = "Mã OTP phải gồm 6 chữ số")
    private String otpCode;
}
