package com.store.grocery.domain.request.auth;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequestDTO {
    @Email(message = "Email không hợp lệ")
    private String email;
}
