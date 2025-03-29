package com.store.grocery.domain.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDisableOTP {
    private String otpCode;
}
