package com.app.webnongsan.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleTokenRequest {
    @NotBlank(message = "Token invalid")
    private String idToken; // Token từ Google
}
