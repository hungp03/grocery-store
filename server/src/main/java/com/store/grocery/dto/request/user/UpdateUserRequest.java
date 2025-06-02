package com.store.grocery.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    @NotBlank(message = "Tên không được để trống")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "Tên chỉ được chứa chữ cái và khoảng trắng")
    private String name;
    @Pattern(regexp = "^$|^0\\d{9}$", message = "Số điện thoại phải gồm 10 số, bắt đầu bằng 0")
    private String phone;
    private String address;
    private String avatarUrl;
}
