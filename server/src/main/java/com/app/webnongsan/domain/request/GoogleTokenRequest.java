package com.app.webnongsan.domain.request;

import com.app.webnongsan.util.DeviceUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GoogleTokenRequest {
    @NotBlank(message = "Token invalid")
    private String credential; // Token từ Google
}
