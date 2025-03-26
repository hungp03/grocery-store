package com.store.grocery.service.impl;

import com.store.grocery.config.CustomGoogleUserDetails;
import com.store.grocery.domain.OTPCode;
import com.store.grocery.domain.Role;
import com.store.grocery.domain.User;
import com.store.grocery.domain.UserToken;
import com.store.grocery.domain.request.auth.GoogleTokenRequest;
import com.store.grocery.domain.request.auth.LoginDTO;
import com.store.grocery.domain.request.auth.ResetPasswordDTO;
import com.store.grocery.domain.response.user.CreateUserDTO;
import com.store.grocery.domain.response.user.ResLoginDTO;
import com.store.grocery.repository.OTPCodeRepository;
import com.store.grocery.repository.UserTokenRepository;
import com.app.webnongsan.service.*;
import com.store.grocery.service.AuthService;
import com.store.grocery.service.CartService;
import com.store.grocery.service.EmailService;
import com.store.grocery.service.UserService;
import com.store.grocery.util.DeviceUtil;
import com.store.grocery.util.SecurityUtil;
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
    private final CartService cartService;
    private final EmailService emailService;
    private final SecurityUtil securityUtil;
    private final UserTokenRepository userTokenRepository;
    private final CustomOAuth2UserService oAuth2UserService;

    @Override
    public void storeOTP(String otp, String email) {
        OTPCode otpCode = otpCodeRepository.findByEmail(email)
                .orElse(new OTPCode());
        otpCode.setEmail(email);

        otpCode.setOtpCode(otp);
        otpCodeRepository.save(otpCode);
    }

    @Override
    public ResLoginDTO.UserGetAccount getAccount() {
        long uid = SecurityUtil.getUserId();
        User currentUserDB = this.userService.getUserById(uid);
        this.userService.checkAccountBanned(currentUserDB);

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        userLogin.setId(currentUserDB.getId());
        userLogin.setEmail(currentUserDB.getEmail());
        userLogin.setName(currentUserDB.getName());
        userLogin.setRole(currentUserDB.getRole());
        userGetAccount.setUser(userLogin);
        userGetAccount.setCartLength(cartService.countProductInCart(currentUserDB.getId()));
        return userGetAccount;
    }

    @Override
    public void logout(String deviceHash) {
       long uid = SecurityUtil.getUserId();
        Optional<UserToken> userTokenOpt = this.userTokenRepository.findByUserIdAndDeviceHash(uid, deviceHash);
        if (userTokenOpt.isEmpty()) {
            throw new ResourceInvalidException("Không tìm thấy phiên đăng nhập trên thiết bị này.");
        }
        userTokenRepository.delete(userTokenOpt.get());
    }

    @Override
    public CreateUserDTO register(User user) {
        if (this.userService.isExistedEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email " + user.getEmail() + " đã tồn tại");
        }
        Role r = new Role();
        r.setId(2);
        user.setRole(r);
        User newUser = this.userService.create(user);
        return this.userService.convertToCreateDTO(newUser);
    }

    @Override
    public void forgotPassword(String email) {
        if (!userService.isExistedEmail(email)) {
            throw new UserNotFoundException("Email " + email + " không tồn tại");
        }
        String otp = String.format("%06d", new Random().nextInt(1000000));
        this.storeOTP(otp, email);
        this.emailService.sendEmailFromTemplateSync(email, "Reset password", "forgotPassword", email, otp);
    }

    @Override
    @Transactional
    public Map<String, String> verifyOtp(String email, String inputOtp) {
        return otpCodeRepository.findByEmail(email)
                .filter(otp -> otp.getOtpCode().equals(inputOtp) && otp.getExpiresAt().isAfter(Instant.now()))
                .map(otp -> {
                    String tempToken = securityUtil.createResetToken(email);
                    otpCodeRepository.deleteByEmail(email); // Xóa OTP sau khi xác thực
                    return Map.of("tempToken", tempToken);
                })
                .orElseThrow(() -> new ResourceInvalidException("Mã OTP không hợp lệ hoặc đã hết hạn."));
    }

    @Override
    public void resetPassword(String token, ResetPasswordDTO request) {
        Jwt decodedToken = this.securityUtil.checkValidToken(token);
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResourceInvalidException("Mật khẩu xác nhận không khớp.");
        }
        String email = decodedToken.getSubject();
        String type = decodedToken.getClaim("type");
        if (!"RESET_PASSWORD".equals(type)) {
            throw new ResourceInvalidException("Token không hợp lệ.");
        }
        this.userService.updatePassword(email, request.getNewPassword());
    }

    @Override
    public Map<String, Object> login(LoginDTO loginDTO, String userAgent) {
        User currentUserDB = this.userService.getUserByUsername(loginDTO.getEmail());
        this.userService.checkAccountBanned(currentUserDB);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ResLoginDTO res = new ResLoginDTO();

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.getRole());
        res.setUser(userLogin);
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(accessToken);
        String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getEmail(), res);
        String deviceHash = DeviceUtil.generateDeviceHash(userAgent);
        this.userService.storeUserToken(currentUserDB, refreshToken, userAgent, deviceHash);

        return Map.of(
                "userInfo", res,
                "refreshToken", refreshToken,
                "device", deviceHash
        );
    }

    @Override
    public Map<String, Object> getNewRefreshToken(String refreshToken, String deviceHash) {
        if ("none".equals(refreshToken)) {
            throw new ResourceInvalidException("Vui lòng đăng nhập");
        }

        // Kiểm tra refresh token hợp lệ
        Jwt decodedToken = this.securityUtil.checkValidToken(refreshToken);
        String email = decodedToken.getSubject();

        // Tìm token trong bảng user_tokens theo user và deviceInfo
        Optional<UserToken> userTokenOpt = this.userTokenRepository.findByRefreshTokenAndDeviceHash(refreshToken, deviceHash);
        if (userTokenOpt.isEmpty() || !userTokenOpt.get().getUser().getEmail().equals(email)) {
            throw new ResourceInvalidException("Refresh token không hợp lệ hoặc không khớp với thiết bị.");
        }

        User currentUser = userTokenOpt.get().getUser();
        this.userService.checkAccountBanned(currentUser);

        ResLoginDTO res = new ResLoginDTO();
        res.setUser(new ResLoginDTO.UserLogin(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName(),
                currentUser.getRole()
        ));

        // Tạo Access Token mới
        String accessToken = this.securityUtil.createAccessToken(email, res);
        res.setAccessToken(accessToken);

        // Tạo Refresh Token mới
        String newRefreshToken = this.securityUtil.createRefreshToken(email, res);

        // Cập nhật lại refresh token trong DB cho thiết bị đó
        UserToken userToken = userTokenOpt.get();
        userToken.setRefreshToken(newRefreshToken);
        userTokenRepository.save(userToken);

        return Map.of(
                "userInfo", res,
                "refreshToken", newRefreshToken
        );
    }


    @Override
    public Map<String, Object> loginGoogle(GoogleTokenRequest request, String userAgent) throws IOException, GeneralSecurityException {
        // Xử lý token từ Google
        OAuth2User oauth2User = oAuth2UserService.processOAuth2User(request.getCredential());
        CustomGoogleUserDetails userDetails = (CustomGoogleUserDetails) oauth2User;
        User currentUserDB = userDetails.getUser();

        // Kiểm tra tài khoản bị khóa
        this.userService.checkAccountBanned(currentUserDB);

        // Tạo authentication với CustomGoogleUserDetails
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo response
        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.getRole());
        res.setUser(userLogin);

        // Tạo access token
        String accessToken = securityUtil.createAccessToken(currentUserDB.getEmail(), res);
        res.setAccessToken(accessToken);

        // Tạo refresh token
        String refresh_token = securityUtil.createRefreshToken(currentUserDB.getEmail(), res);
        String deviceHash = DeviceUtil.generateDeviceHash(userAgent);
        this.userService.storeUserToken(currentUserDB, refresh_token, userAgent, deviceHash);

        return Map.of(
                "userInfo", res,
                "refreshToken", refresh_token,
                "device", deviceHash
        );
    }
}
