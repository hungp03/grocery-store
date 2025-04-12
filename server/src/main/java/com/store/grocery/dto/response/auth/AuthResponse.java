package com.store.grocery.dto.response.auth;

import com.store.grocery.dto.response.user.UserLoginResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private UserLoginResponse userLoginResponse;
    private String refreshToken;
    private String device;

    public AuthResponse(UserLoginResponse userLoginResponse, String refreshToken){
        this.userLoginResponse = userLoginResponse;
        this.refreshToken = refreshToken;
    }
}
