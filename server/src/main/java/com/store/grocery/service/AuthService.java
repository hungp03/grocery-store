package com.store.grocery.service;

import com.store.grocery.domain.User;
import com.store.grocery.domain.request.auth.GoogleTokenRequest;
import com.store.grocery.domain.request.auth.LoginDTO;
import com.store.grocery.domain.request.auth.ResetPasswordDTO;
import com.store.grocery.domain.response.user.CreateUserDTO;
import com.store.grocery.domain.response.user.ResLoginDTO;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

public interface AuthService {
    void storeOTP(String otp, String email);
    ResLoginDTO.UserGetAccount getAccount();
    void logout(String deviceHash);
    CreateUserDTO register(User user);
    void forgotPassword(String email);
    Map<String, String> verifyOtp(String email, String inputOtp);
    void resetPassword(String token, ResetPasswordDTO request);
    Map<String, Object> login(LoginDTO loginDTO, String userAgent);
    Map<String, Object> getNewRefreshToken(String refreshToken, String deviceHash);
    Map<String, Object> loginGoogle(GoogleTokenRequest request, String userAgent)throws IOException, GeneralSecurityException;
}