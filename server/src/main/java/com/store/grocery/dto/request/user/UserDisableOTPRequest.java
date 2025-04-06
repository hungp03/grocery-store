package com.store.grocery.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDisableOTPRequest {
    private String otpCode;
}
