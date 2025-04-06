package com.store.grocery.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    @NotBlank(message = "Tên không được để trống")
    private String name;
    @NotBlank(message = "Không được để trống email")
    @Email(message = "Email không hợp lệ")
    private String email;
    private String password;
}
