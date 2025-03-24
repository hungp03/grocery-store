package com.app.webnongsan.service;

import com.app.webnongsan.config.CustomGoogleUserDetails;
import com.app.webnongsan.domain.OTPCode;
import com.app.webnongsan.domain.Role;
import com.app.webnongsan.domain.User;
import com.app.webnongsan.domain.request.GoogleTokenRequest;
import com.app.webnongsan.domain.request.LoginDTO;
import com.app.webnongsan.domain.request.ResetPasswordDTO;
import com.app.webnongsan.domain.response.user.CreateUserDTO;
import com.app.webnongsan.domain.response.user.ResLoginDTO;
import com.app.webnongsan.repository.OTPCodeRepository;
import com.app.webnongsan.util.SecurityUtil;
import com.app.webnongsan.util.exception.DuplicateResourceException;
import com.app.webnongsan.util.exception.ResourceInvalidException;
import com.app.webnongsan.util.exception.UserNotFoundException;
import lombok.AllArgsConstructor;
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
import java.util.Random;

@Service
@AllArgsConstructor
public class AuthService {
    private static final long OTP_EXPIRY_SECOND = 300;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final OTPCodeRepository otpCodeRepository;
    private final UserService userService;
    private final CartService cartService;
    private final EmailService emailService;
    private final SecurityUtil securityUtil;
    private final CustomOAuth2UserService oAuth2UserService;

    public void storeOTP(String otp, String email) {
        OTPCode otpCode = otpCodeRepository.findByEmail(email)
                .orElse(new OTPCode());
        otpCode.setEmail(email);
        otpCode.setOtpCode(otp);
        otpCodeRepository.save(otpCode);
    }

    public ResLoginDTO.UserGetAccount getAccount() {
        long uid = SecurityUtil.getUserId();
        User currentUserDB = this.userService.getUserById(uid);
        this.userService.checkAccountBanned(currentUserDB);

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        if (currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(currentUserDB.getEmail());
            userLogin.setName(currentUserDB.getName());
            userLogin.setRole(currentUserDB.getRole());
            userGetAccount.setUser(userLogin);
            userGetAccount.setCartLength(cartService.countProductInCart(currentUserDB.getId()));
        }
        return userGetAccount;
    }

    public void logout() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        if (email.isEmpty()) {
            throw new ResourceInvalidException("Accesstoken không hợp lệ");
        }
        this.userService.updateUserToken(null, email);
    }

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

    public void forgotPassword(String email) {
        if (!userService.isExistedEmail(email)) {
            throw new UserNotFoundException("Email " + email + " không tồn tại");
        }
        String otp = String.format("%06d", new Random().nextInt(1000000));
        this.storeOTP(otp, email);
        this.emailService.sendEmailFromTemplateSync(email, "Reset password", "forgotPassword", email, otp);
    }

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
        this.userService.resetPassword(email, request.getNewPassword());
    }

    public Map<String, Object> login(LoginDTO loginDTO) {
        User currentUserDB = this.userService.getUserByUsername(loginDTO.getEmail());
        this.userService.checkAccountBanned(currentUserDB);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ResLoginDTO res = new ResLoginDTO();

        assert currentUserDB != null;
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.getRole());
        res.setUser(userLogin);
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(accessToken);
        String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getEmail(), res);
        this.userService.updateUserToken(refreshToken, loginDTO.getEmail());
        return Map.of(
                "userInfo", res,
                "refreshToken", refreshToken
        );
    }

    public Map<String, Object> getNewRefreshToken(String refreshToken) {
        if (refreshToken.equals("none")) {
            throw new ResourceInvalidException("Vui lòng đăng nhập");
        }

        // Check RFtoken hợp lệ
        Jwt decodedToken = this.securityUtil.checkValidToken(refreshToken);
        String email = decodedToken.getSubject();
        User currentUser = this.userService.getUserByRFTokenAndEmail(email, refreshToken);
        if (currentUser == null) {
            throw new ResourceInvalidException("Refresh token không hợp lệ");
        }
        this.userService.checkAccountBanned(currentUser);
        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName(),
                currentUser.getRole());
        res.setUser(userLogin);

        // create access token
        String access_token = this.securityUtil.createAccessToken(email, res);
        res.setAccessToken(access_token);

        // create refresh token
        String new_refresh_token = this.securityUtil.createRefreshToken(email, res);

        // update user
        this.userService.updateUserToken(new_refresh_token, email);
        return Map.of(
                "userInfo", res,
                "refreshToken", new_refresh_token
        );
    }

    public Map<String, Object> loginGoogle(GoogleTokenRequest request) throws IOException, GeneralSecurityException {
        // Xử lý token từ Google
        OAuth2User oauth2User = oAuth2UserService.processOAuth2User(request.getIdToken());
        CustomGoogleUserDetails userDetails = (CustomGoogleUserDetails) oauth2User;
        User currentUserDB = userDetails.getUser();

        // Kiểm tra tài khoản bị ban
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
        userService.updateUserToken(refresh_token, currentUserDB.getEmail());

        return Map.of(
                "userInfo", res,
                "refreshToken", refresh_token
        );
    }
}
