package com.store.grocery.service.impl;

import com.store.grocery.config.CustomGoogleUserDetails;
import com.store.grocery.domain.User;
import com.store.grocery.domain.UserToken;
import com.store.grocery.dto.request.auth.GoogleTokenRequest;
import com.store.grocery.dto.request.auth.LoginRequest;
import com.store.grocery.dto.request.auth.ResetPasswordRequest;
import com.store.grocery.dto.request.user.UserRegisterRequest;
import com.store.grocery.dto.response.auth.AuthResponse;
import com.store.grocery.dto.response.auth.OtpVerificationResponse;
import com.store.grocery.dto.response.user.CreateUserResponse;
import com.store.grocery.dto.response.user.UserLoginResponse;
import com.store.grocery.service.*;
import com.store.grocery.util.JwtUtil;
import com.store.grocery.util.Utils;
import com.store.grocery.util.enums.OTPType;
import com.store.grocery.util.exception.ResourceInvalidException;
import com.store.grocery.util.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final OTPService otpService;
    private final UserService userService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final UserTokenService userTokenService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public UserLoginResponse.UserGetAccount getMyAccount() {
        long uid = JwtUtil.getUserId();
        log.info("Fetching basic data for user ID: {}", uid);
        User currentUserDB = this.userService.findById(uid);
        this.userService.checkAccountBanned(currentUserDB);
        UserLoginResponse.UserGetAccount userGetAccount = UserLoginResponse.UserGetAccount.from(currentUserDB);
        log.info("Successfully get account info, user ID: {}", uid);
        return userGetAccount;
    }

    @Override
    public void logout(String authHeader, String deviceHash) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResourceInvalidException("Token không hợp lệ");
        }
        String token = authHeader.substring(7);
        tokenBlacklistService.blacklistToken(token);
        long uid = JwtUtil.getUserId();
        UserToken userToken = this.userTokenService.findByfindByUserAndDeviceHash(uid, deviceHash);
        log.info("Logout - User ID: {} | device: {}", uid, deviceHash);
        userTokenService.deleteToken(userToken);
    }

    @Override
    public CreateUserResponse register(UserRegisterRequest user) {
        log.info("Register new user with email: {}", user.getEmail());
        CreateUserResponse newUser = this.userService.create(user);
        log.info("Registered successfully, user ID: {}", newUser.getId());
        return newUser;
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Forgot password request, email: {}", email);
        if (!userService.isExistedEmail(email)) {
            log.warn("Email {} does not exist", email);
            throw new UserNotFoundException("Email " + email + " không tồn tại");
        }
        String otp = otpService.generateOTP();
        otpService.storeOTP(otp, email, OTPType.RESET_PASSWORD);
        this.emailService.sendEmailFromTemplateSync(email, "Reset password", "forgotPassword", email, otp);
        log.info("OTP for password reset sent, email: {}", email);
    }

    @Override
    @Transactional
    public OtpVerificationResponse verifyOtp(String email, String inputOtp) {
        log.info("Verifying OTP for email: {}", email);
        boolean validOTP = otpService.verifyOTP(email, inputOtp, OTPType.RESET_PASSWORD);
        if (!validOTP){
            log.warn("Failed OTP verification for email: {}. Invalid or expired OTP.", email);
            throw new ResourceInvalidException("OTP không hợp lệ hoặc đã hết hạn");
        }
        otpService.deleteOtpByEmailAndType(email, OTPType.RESET_PASSWORD);
        return new OtpVerificationResponse(jwtService.createResetToken(email));
    }

    @Override
    public void resetPassword(String token, ResetPasswordRequest request) {
        log.info("Reset password requested using token.");
        Jwt decodedToken = this.jwtService.decodeToken(token);
        boolean isTokenRevoked = tokenBlacklistService.isTokenBlacklisted(token);
        if (isTokenRevoked){
            throw new ResourceInvalidException("Link đặt lại mật khẩu đã hết hạn, vui lòng yêu cầu lại");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password reset failed: Confirm password does not match.");
            throw new ResourceInvalidException("Mật khẩu xác nhận không khớp.");
        }
        String email = decodedToken.getSubject();
        String type = decodedToken.getClaim("type");
        if (!"RESET_PASSWORD".equals(type)) {
            log.warn("Password reset failed: Invalid token type.");
            throw new ResourceInvalidException("Token không hợp lệ.");
        }
        tokenBlacklistService.blacklistToken(token);
        this.userService.updatePassword(email, request.getNewPassword());
        log.info("Password reset successfully for email: {}", email);
    }

    @Override
    public AuthResponse login(LoginRequest loginDTO, String userAgent) {
        final String email = loginDTO.getEmail();
        log.info("Login attempt for email: {}", email);
        User currentUserDB = this.userService.getUserByUsername(email);
        log.info("User id={} | email={}", currentUserDB.getId(), currentUserDB.getEmail());
        if (currentUserDB.getPassword() == null || currentUserDB.getPassword().isEmpty()) {
            throw new ResourceInvalidException(
                    "Tài khoản của bạn chưa được thiết lập mật khẩu. Vui lòng đăng nhập bằng bên thứ 3 hoặc chọn quên mật khẩu để đăng nhập thủ công."
            );
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, loginDTO.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Authentication successful for user: {}", email);

        UserLoginResponse res = buildLoginResponse(currentUserDB, authentication);
        String refreshToken = this.jwtService.createRefreshToken(email, res);
        log.info("Refresh token created for user: {}", email);
        String deviceHash = Utils.generateDeviceHash(userAgent);
        this.userTokenService.storeUserToken(currentUserDB, refreshToken, userAgent, deviceHash);
        log.info("Stored refresh token for user: {}", email);

        return new AuthResponse(res, refreshToken, deviceHash);
    }

    @Override
    public AuthResponse renewToken(String refreshToken, String deviceHash) {
        if ("none".equals(refreshToken) || "none".equals(deviceHash)) {
            log.warn("Invalid refresh token or device: none");
            throw new ResourceInvalidException("Vui lòng đăng nhập");
        }

        UserToken userToken = this.userTokenService.validateRefreshToken(refreshToken, deviceHash);
        User currentUser = userToken.getUser();
        log.info("User found for refresh token: id={}, email={}", currentUser.getId(), currentUser.getEmail());
        this.userService.checkAccountBanned(currentUser);
        UserLoginResponse res = new UserLoginResponse();
        res.setUser(UserLoginResponse.UserLogin.from(currentUser));

        String accessToken = this.jwtService.createAccessToken(currentUser.getEmail(), res);
        res.setAccessToken(accessToken);
        log.info("New access token created for user: {}", currentUser.getEmail());

        String newRefreshToken = this.jwtService.createRefreshToken(currentUser.getEmail(), res);
        log.info("New refresh token created for user: {}", currentUser.getEmail());

        // Cập nhật lại refresh token trong DB cho thiết bị đó
        userToken.setRefreshToken(newRefreshToken);
        userTokenService.saveToken(userToken);
        log.info("Updated refresh token in database for user: {}", currentUser.getEmail());
        return new AuthResponse(res, newRefreshToken);
    }


    @Override
    public AuthResponse loginGoogle(GoogleTokenRequest request, String userAgent) throws IOException, GeneralSecurityException {
        log.info("Google login attempt with user agent: {}", userAgent);

        OAuth2User oauth2User = oAuth2UserService.processOAuth2User(request.getCredential());
        CustomGoogleUserDetails userDetails = (CustomGoogleUserDetails) oauth2User;
        User currentUser = userDetails.getUser();
        log.info("Google user authenticated: id={}, email={}", currentUser.getId(), currentUser.getEmail());
        userService.checkAccountBanned(currentUser);
        // Tạo authentication với CustomGoogleUserDetails
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserLoginResponse res = buildLoginResponse(currentUser, authentication);
        String refresh_token = jwtService.createRefreshToken(currentUser.getEmail(), res);
        log.info("Refresh token created for Google user: {}", currentUser.getEmail());
        String deviceHash = Utils.generateDeviceHash(userAgent);
        this.userTokenService.storeUserToken(currentUser, refresh_token, userAgent, deviceHash);
        log.info("Stored Google refresh token for user: {}", currentUser.getEmail());

        return new AuthResponse(res, refresh_token, deviceHash);
    }

    private UserLoginResponse buildLoginResponse(User user, Authentication auth) {
        UserLoginResponse response = new UserLoginResponse();
        response.setUser(UserLoginResponse.UserLogin.from(user));
        String accessToken = jwtService.createAccessToken(auth.getName(), response);
        response.setAccessToken(accessToken);
        log.info("Access tokens created for user: {}", user.getEmail());
        return response;
    }
}
