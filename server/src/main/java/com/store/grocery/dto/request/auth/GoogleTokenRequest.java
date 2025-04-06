package com.store.grocery.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleTokenRequest {
    @NotBlank(message = "Token invalid")
    private String credential; // Token từ Google
}
