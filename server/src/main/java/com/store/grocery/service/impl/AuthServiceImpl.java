package com.store.grocery.service.impl;

import com.store.grocery.config.CustomGoogleUserDetails;
import com.store.grocery.domain.OTPCode;
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
import com.store.grocery.repository.OTPCodeRepository;
import com.store.grocery.repository.UserTokenRepository;
import com.store.grocery.service.AuthService;
import com.store.grocery.service.EmailService;
import com.store.grocery.service.UserService;
import com.store.grocery.service.UserTokenService;
import com.store.grocery.util.Utils;
import com.store.grocery.util.SecurityUtil;
import com.store.grocery.util.enums.OTPType;
import com.store.grocery.util.exception.DuplicateResourceException;
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
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final OTPCodeRepository otpCodeRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final SecurityUtil securityUtil;
    private final UserTokenService userTokenService;
    private final CustomOAuth2UserService oAuth2UserService;

    @Override
    public void storeOTP(String otp, String email, OTPType otpType) {
        log.info("Storing OTP for email: {} with type: {}", email, otpType);
        OTPCode otpCode = otpCodeRepository.findByEmailAndType(email, otpType).orElse(new OTPCode());
        otpCode.setEmail(email);
        otpCode.setOtpCode(otp);
        otpCode.setType(otpType);
        otpCodeRepository.save(otpCode);
        log.info("OTP successfully stored for email: {}", email);
    }

    @Override
    public UserLoginResponse.UserGetAccount getAccount() {
        long uid = SecurityUtil.getUserId();
        log.info("Fetching basic data for user ID: {}", uid);
        User currentUserDB = this.userService.getUserById(uid);
        log.debug("Fetched user details from DB - ID: {}, Email: {}", currentUserDB.getId(), currentUserDB.getEmail());
        this.userService.checkAccountBanned(currentUserDB);
        UserLoginResponse.UserGetAccount userGetAccount = new UserLoginResponse.UserGetAccount(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.isStatus(),
                currentUserDB.getPhone(),
                currentUserDB.getAddress(),
                currentUserDB.getAvatarUrl(),
                currentUserDB.getRole());
        log.info("Successfully retrieved account info for user ID: {}", uid);
        return userGetAccount;
    }

    @Override
    public void logout(String deviceHash) {
        long uid = SecurityUtil.getUserId();
        UserToken userToken = this.userTokenService.findByfindByUserAndDeviceHash(uid, deviceHash);
        log.info("User ID: {} has successfully logged out from device: {}", uid, deviceHash);
        userTokenService.deleteToken(userToken);
    }

    @Override
    public CreateUserResponse register(UserRegisterRequest user) {
        log.info("Attempting to register new user with email: {}", user.getEmail());
        if (this.userService.isExistedEmail(user.getEmail())) {
            log.warn("Registration failed: Email {} already exists", user.getEmail());
            throw new DuplicateResourceException("Email " + user.getEmail() + " đã tồn tại");
        }
        CreateUserResponse newUser = this.userService.create(user);
        log.info("User registered successfully with ID: {}", newUser.getId());
        return newUser;
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Received forgot password request for email: {}", email);
        if (!userService.isExistedEmail(email)) {
            log.warn("Forgot password request failed: Email {} does not exist", email);
            throw new UserNotFoundException("Email " + email + " không tồn tại");
        }
        String otp = String.format("%06d", new Random().nextInt(1000000));
        this.storeOTP(otp, email, OTPType.RESET_PASSWORD);
        this.emailService.sendEmailFromTemplateSync(email, "Reset password", "forgotPassword", email, otp);
        log.info("OTP for password reset sent to email: {}", email);
    }

    @Override
    @Transactional
    public OtpVerificationResponse verifyOtp(String email, String inputOtp) {
        log.info("Verifying OTP for email: {}", email);
        return otpCodeRepository.findByEmailAndType(email, OTPType.RESET_PASSWORD)
                .filter(otp -> otp.getOtpCode().equals(inputOtp) && otp.getExpiresAt().isAfter(Instant.now()))
                .map(otp -> {
                    String tempToken = securityUtil.createResetToken(email);
                    otpCodeRepository.deleteByEmailAndType(email, OTPType.RESET_PASSWORD);
                    log.info("OTP verified successfully for email: {}. Generated temp token.", email);
                    return new OtpVerificationResponse(tempToken);
                })
                .orElseThrow(() -> {
                    log.warn("Failed OTP verification for email: {}. Invalid or expired OTP.", email);
                    return new ResourceInvalidException("Mã OTP không hợp lệ hoặc đã hết hạn.");
                });
    }

    @Override
    public void resetPassword(String token, ResetPasswordRequest request) {
        log.info("Reset password requested using token.");
        Jwt decodedToken = this.securityUtil.checkValidToken(token);
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
        this.userService.updatePassword(email, request.getNewPassword());
        log.info("Password reset successfully for email: {}", email);
    }

    @Override
    public AuthResponse login(LoginRequest loginDTO, String userAgent) {
        log.info("Login attempt for email: {}", loginDTO.getEmail());
        User currentUserDB = this.userService.getUserByUsername(loginDTO.getEmail());
        log.info("User found: id={}, email={}", currentUserDB.getId(), currentUserDB.getEmail());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Authentication successful for user: {}", loginDTO.getEmail());
        UserLoginResponse res = new UserLoginResponse();
        UserLoginResponse.UserLogin userLogin = new UserLoginResponse.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.getRole());
        res.setUser(userLogin);
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), res);
        log.info("Access token created for user: {}", loginDTO.getEmail());
        res.setAccessToken(accessToken);
        String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getEmail(), res);
        log.info("Refresh token created for user: {}", loginDTO.getEmail());
        String deviceHash = Utils.generateDeviceHash(userAgent);
        this.userTokenService.storeUserToken(currentUserDB, refreshToken, userAgent, deviceHash);
        log.info("Stored refresh token for user: {}", loginDTO.getEmail());
        return new AuthResponse(res, refreshToken, deviceHash);
    }

    @Override
    public AuthResponse getNewRefreshToken(String refreshToken, String deviceHash) {
        log.info("Request to get new token. DeviceHash: {}", deviceHash);
        if ("none".equals(refreshToken)) {
            log.warn("Invalid refresh token: none");
            throw new ResourceInvalidException("Vui lòng đăng nhập");
        }

        UserToken userToken = this.userTokenService.validateRefreshToken(refreshToken, deviceHash);
        User currentUser = userToken.getUser();
        log.info("User found for refresh token: id={}, email={}", currentUser.getId(), currentUser.getEmail());
        this.userService.checkAccountBanned(currentUser);

        UserLoginResponse res = new UserLoginResponse();
        res.setUser(new UserLoginResponse.UserLogin(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName(),
                currentUser.getRole()
        ));

        // Tạo Access Token mới
        String accessToken = this.securityUtil.createAccessToken(currentUser.getEmail(), res);
        res.setAccessToken(accessToken);
        log.info("New access token created for user: {}", currentUser.getEmail());
        // Tạo Refresh Token mới
        String newRefreshToken = this.securityUtil.createRefreshToken(currentUser.getEmail(), res);
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
        // Xử lý token từ Google
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
        UserLoginResponse res = new UserLoginResponse();
        UserLoginResponse.UserLogin userLogin = new UserLoginResponse.UserLogin(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName(),
                currentUser.getRole());
        res.setUser(userLogin);

        // Tạo access token
        String accessToken = securityUtil.createAccessToken(currentUser.getEmail(), res);
        res.setAccessToken(accessToken);
        log.info("Access token created for Google user: {}", currentUser.getEmail());
        // Tạo refresh token
        String refresh_token = securityUtil.createRefreshToken(currentUser.getEmail(), res);
        log.info("Refresh token created for Google user: {}", currentUser.getEmail());
        String deviceHash = Utils.generateDeviceHash(userAgent);
        this.userTokenService.storeUserToken(currentUser, refresh_token, userAgent, deviceHash);
        log.info("Stored Google refresh token for user: {}", currentUser.getEmail());
        return new AuthResponse(res, refresh_token, deviceHash);
    }
}
