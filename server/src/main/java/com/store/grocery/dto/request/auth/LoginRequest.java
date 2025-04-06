package com.store.grocery.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Không được để trống mật khẩu")
    private String password;
}
