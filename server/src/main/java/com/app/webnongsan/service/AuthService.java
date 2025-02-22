package com.app.webnongsan.service;

import com.app.webnongsan.domain.VerificationCode;
import com.app.webnongsan.domain.Role;
import com.app.webnongsan.domain.User;
import com.app.webnongsan.domain.request.GoogleTokenRequest;
import com.app.webnongsan.domain.request.LoginDTO;
import com.app.webnongsan.domain.request.OTPDto;
import com.app.webnongsan.domain.request.ResetPasswordDTO;
import com.app.webnongsan.domain.response.user.CreateUserDTO;
import com.app.webnongsan.domain.response.user.ResLoginDTO;
import com.app.webnongsan.repository.VerificationRepository;
import com.app.webnongsan.util.SecurityUtil;
import com.app.webnongsan.util.exception.AuthException;
import com.app.webnongsan.util.exception.ResourceInvalidException;
import com.app.webnongsan.util.exception.UserNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {
    private static final long OTP_EXPIRY_MINUTES = 5;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final VerificationRepository verificationRepository;
    private final UserService userService;
    private final CartService cartService;
    private final EmailService emailService;
    private final SecurityUtil securityUtil;
    private final ApplicationContext context;
    private final CustomOAuth2UserService oAuth2UserService;

    public void storeOTP(String otp, String email) {
        VerificationCode authOTP = new VerificationCode();
        authOTP.setOtp(otp);
        authOTP.setEmail(email);
        authOTP.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        log.info("Store OTP {} for user {}", otp, email);
        this.verificationRepository.save(authOTP);
    }

    public boolean isOTPValid(String otp, String email){
        log.info("Check OTP for user {}", email);
        VerificationCode authOTP = this.verificationRepository.findByOtpAndEmail(otp, email);
        return authOTP != null && authOTP.getExpiryTime().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void deleteOtp(String otp, String email){
        log.info("Delete OTP for user {}", email);
        this.verificationRepository.deleteByOtpAndEmail(otp, email);
    }

    public ResLoginDTO.UserGetAccount getAccount() throws UserNotFoundException, AuthException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        log.info("User {} is requesting account information", email);
        // Lấy thông tin người dùng trong db
        User currentUserDB = this.userService.getUserByUsername(email);
        if (currentUserDB != null && currentUserDB.getStatus() == 0) {
            log.warn("User {} has been banned", email);
            throw new AuthException("Tài khoản của bạn đã bị khóa.");
        }

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
        log.info("User {} retrieved account information successfully", email);
        return userGetAccount;
    }

    public void logout() throws ResourceInvalidException, UserNotFoundException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        if (email.isEmpty()) {
            log.warn("Logout attempt failed due to invalid access token");
            throw new ResourceInvalidException("Accesstoken không hợp lệ");
        }
        this.userService.updateUserToken(null, email);
    }

    public CreateUserDTO register(User user) throws ResourceInvalidException {
        log.info("User {} is attempting to register", user.getEmail());
        if (this.userService.isExistedEmail(user.getEmail())) {
            log.warn("Registration failed: Email {} already exists", user.getEmail());
            throw new ResourceInvalidException("Email " + user.getEmail() + " đã tồn tại");
        }
        Role r = new Role();
        r.setId(2);
        user.setRole(r);
        User newUser = this.userService.create(user);
        log.info("User {} register successfully", user.getEmail());
        return this.userService.convertToCreateDTO(newUser);
    }

    public void forgotPassword(String email) throws UserNotFoundException {
        log.info("User {} requesting password reset", email);
        if (!userService.isExistedEmail(email)) {
            log.warn("Request failed: {} does not exist in database", email);
            throw new UserNotFoundException("Email " + email + " không tồn tại");
        }
        String otp = String.format("%06d", new Random().nextInt(1000000));
        this.storeOTP(otp, email);
        this.emailService.sendEmailFromTemplateSync(email, "Reset password", "forgotPassword", email, otp);
    }

    // Không gọi trực tiếp deleteOtp() vì @Transactional không hoạt động khi gọi trong cùng một class
    // Lấy bean từ ApplicationContext để Spring áp dụng proxy và quản lý transaction đúng cách
    // No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call
    public Map<String, String> verifyOtp(OTPDto request) throws ResourceInvalidException {
        log.info("Verifying OTP for email: {}", request.getEmail());
        if (this.isOTPValid(request.getOtp(), request.getEmail())) {
            String tempToken = securityUtil.createResetToken(request.getEmail());
            context.getBean(AuthService.class).deleteOtp(request.getOtp(), request.getEmail());
            log.info("Verify OPT successfully for user {}", request.getEmail());
            return Map.of("tempToken", tempToken);
        } else {
            throw new ResourceInvalidException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
    }

    public void resetPassword(String token, ResetPasswordDTO request) throws ResourceInvalidException, UserNotFoundException {
        log.info("Processing password reset request for token {}", token);
        Jwt decodedToken = this.securityUtil.checkValidToken(token);
        log.debug("Decoded token successfully: {}", token);
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password and confirm password not match!");
            throw new ResourceInvalidException("Mật khẩu xác nhận không khớp.");
        }
        String email = decodedToken.getSubject();
        String type = decodedToken.getClaim("type");
        if (!"RESET_PASSWORD".equals(type)) {
            throw new ResourceInvalidException("Token không hợp lệ.");
        }
        this.userService.resetPassword(email, request.getNewPassword());
        log.info("Resetting password for user {}", email);
        log.info("Token {} deleted after password reset", token);
        log.info("Password reset successfully for user {}", email);
    }

    public Map<String, Object> login(LoginDTO loginDTO) throws UserNotFoundException, AuthException {
        log.info("User {} started login process", loginDTO.getEmail());
        log.debug("Fetching user details from database for {}", loginDTO.getEmail());
        User currentUserDB = this.userService.getUserByUsername(loginDTO.getEmail());
        this.userService.checkAccountBanned(currentUserDB);

        log.debug("Authenticating user {}", loginDTO.getEmail());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Generating response for {}", loginDTO.getEmail());
        ResLoginDTO res = new ResLoginDTO();

        assert currentUserDB != null;
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.getRole());
        res.setUser(userLogin);

        log.debug("Creating access token for {}", loginDTO.getEmail());
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(accessToken);

        log.debug("Creating refresh token for {}", loginDTO.getEmail());
        String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getEmail(), res);
        this.userService.updateUserToken(refreshToken, loginDTO.getEmail());
        return Map.of(
                "userInfo", res,
                "refreshToken", refreshToken
        );
    }

    public Map<String, Object> getNewRefreshToken(String refreshToken) throws UserNotFoundException, ResourceInvalidException, AuthException {
        if (refreshToken.equals("none")) {
            log.warn("Refresh token is missing from the request");
            throw new ResourceInvalidException("Vui lòng đăng nhập");
        }

        // Check RFtoken hợp lệ
        Jwt decodedToken = this.securityUtil.checkValidToken(refreshToken);
        String email = decodedToken.getSubject();
        log.info("User {} is requesting a new refresh token", email);
        log.debug("Fetching user by refresh token and email: {}", email);
        User currentUser = this.userService.getUserByRFTokenAndEmail(email, refreshToken);
        if (currentUser == null) {
            log.warn("Invalid RF token from user {}", email);
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
        log.info("User {} successfully obtained a new refresh token", email);
        return Map.of(
                "userInfo", res,
                "refreshToken", new_refresh_token
        );
    }

    public Map<String, Object> loginGoogle(GoogleTokenRequest request) throws UserNotFoundException, GeneralSecurityException, IOException, AuthException {
        // Xử lý token từ Google
        log.info("Processing Google login for token: {}", request.getIdToken());
        OAuth2User oauth2User = oAuth2UserService.processOAuth2User(request.getIdToken());
        String email = oauth2User.getAttribute("email");
        User currentUserDB = userService.getUserByUsername(email);
        this.userService.checkAccountBanned(currentUserDB);
        //khi đăng nhập bằng mật khẩu, Spring Security tự động xử lý authorities từ UserDetailsService
        //trong khi đăng nhập Google không có sẵn
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + currentUserDB.getRole().getRoleName()));
        log.info("User {} has role {}", email, currentUserDB.getRole().getRoleName());
        // Tạo UserDetails sử dụng User của Spring Security
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email,
                "", // password trống vì xác thực qua Google
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );

        // Tạo authentication với UserDetails
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.getRole());
        res.setUser(userLogin);
        String accessToken = securityUtil.createAccessToken(email, res);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        res.setAccessToken(accessToken);
        // Tạo refresh token
        String refresh_token = securityUtil.createRefreshToken(email, res);
        userService.updateUserToken(refresh_token, email);
        log.info("Google login successful for email: {}", email);
        return Map.of(
                "userInfo", res,
                "refreshToken", refresh_token
        );
    }
}
