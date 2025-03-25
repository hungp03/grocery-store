package com.app.webnongsan.service;

import com.app.webnongsan.domain.User;
import com.app.webnongsan.domain.request.GoogleTokenRequest;
import com.app.webnongsan.domain.request.LoginDTO;
import com.app.webnongsan.domain.request.ResetPasswordDTO;
import com.app.webnongsan.domain.response.user.CreateUserDTO;
import com.app.webnongsan.domain.response.user.ResLoginDTO;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

public interface AuthService {
    void storeOTP(String otp, String email);
    ResLoginDTO.UserGetAccount getAccount();
    void logout();
    CreateUserDTO register(User user);
    void forgotPassword(String email);
    Map<String, String> verifyOtp(String email, String inputOtp);
    void resetPassword(String token, ResetPasswordDTO request);
    Map<String, Object> login(LoginDTO loginDTO);
    Map<String, Object> getNewRefreshToken(String refreshToken);
    Map<String, Object> loginGoogle(GoogleTokenRequest request)throws IOException, GeneralSecurityException;
}