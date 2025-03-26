package com.store.grocery.domain.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTO {
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}
