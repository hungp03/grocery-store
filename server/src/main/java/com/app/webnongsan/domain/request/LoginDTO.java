package com.app.webnongsan.domain.request;

import com.app.webnongsan.util.HashUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LoginDTO {
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Không được để trống mật khẩu")
    private String password;

    private String deviceInfo;

    public String getDeviceInfo() {
        return (deviceInfo == null || deviceInfo.trim().isEmpty())
                ? "Unknown-" + UUID.randomUUID()
                : deviceInfo;
    }

    public String getDeviceHash() {
        String rawDeviceInfo = (deviceInfo == null || deviceInfo.trim().isEmpty())
                ? "Unknown-" + UUID.randomUUID()
                : deviceInfo;
        return HashUtil.generateDeviceHash(rawDeviceInfo);
    }
}
