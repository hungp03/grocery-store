package com.store.grocery.service;

import com.store.grocery.dto.request.auth.GoogleTokenRequest;
import com.store.grocery.dto.request.auth.LoginRequest;
import com.store.grocery.dto.request.auth.ResetPasswordRequest;
import com.store.grocery.dto.request.user.UserRegisterRequest;
import com.store.grocery.dto.response.user.CreateUserResponse;
import com.store.grocery.dto.response.user.LoginResponse;
import com.store.grocery.util.enums.OTPType;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

public interface AuthService {
    void storeOTP(String otp, String email, OTPType otpType);
    LoginResponse.UserGetAccount getAccount();
    void logout(String deviceHash);
    CreateUserResponse register(UserRegisterRequest user);
    void forgotPassword(String email);
    Map<String, String> verifyOtp(String email, String inputOtp);
    void resetPassword(String token, ResetPasswordRequest request);
    Map<String, Object> login(LoginRequest loginDTO, String userAgent);
    Map<String, Object> getNewRefreshToken(String refreshToken, String deviceHash);
    Map<String, Object> loginGoogle(GoogleTokenRequest request, String userAgent)throws IOException, GeneralSecurityException;
}