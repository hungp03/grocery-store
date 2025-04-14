package com.store.grocery.service;

import com.store.grocery.dto.request.auth.GoogleTokenRequest;
import com.store.grocery.dto.request.auth.LoginRequest;
import com.store.grocery.dto.request.auth.ResetPasswordRequest;
import com.store.grocery.dto.request.user.UserRegisterRequest;
import com.store.grocery.dto.response.auth.AuthResponse;
import com.store.grocery.dto.response.auth.OtpVerificationResponse;
import com.store.grocery.dto.response.user.CreateUserResponse;
import com.store.grocery.dto.response.user.UserLoginResponse;
import com.store.grocery.util.enums.OTPType;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

public interface AuthService {
    UserLoginResponse.UserGetAccount getAccount();
    void logout(String deviceHash);
    CreateUserResponse register(UserRegisterRequest user);
    void forgotPassword(String email);
    OtpVerificationResponse verifyOtp(String email, String inputOtp);
    void resetPassword(String token, ResetPasswordRequest request);
    AuthResponse login(LoginRequest loginDTO, String userAgent);
    AuthResponse getNewRefreshToken(String refreshToken, String deviceHash);
    AuthResponse loginGoogle(GoogleTokenRequest request, String userAgent)throws IOException, GeneralSecurityException;
}