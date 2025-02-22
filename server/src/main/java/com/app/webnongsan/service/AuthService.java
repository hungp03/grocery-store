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
import com.app.webnongsan.util.exception.DuplicateResourceException;
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
        this.verificationRepository.save(authOTP);
    }

    public boolean isOTPValid(String otp, String email){
        VerificationCode authOTP = this.verificationRepository.findByOtpAndEmail(otp, email);
        return authOTP != null && authOTP.getExpiryTime().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void deleteOtp(String otp, String email){
        this.verificationRepository.deleteByOtpAndEmail(otp, email);
    }

    public ResLoginDTO.UserGetAccount getAccount(){
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        // Lấy thông tin người dùng trong db
        User currentUserDB = this.userService.getUserByUsername(email);
        if (currentUserDB != null && currentUserDB.getStatus() == 0) {
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
        return userGetAccount;
    }

    public void logout(){
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        if (email.isEmpty()) {
            throw new ResourceInvalidException("Accesstoken không hợp lệ");
        }
        this.userService.updateUserToken(null, email);
    }

    public CreateUserDTO register(User user){
        if (this.userService.isExistedEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email " + user.getEmail() + " đã tồn tại");
        }
        Role r = new Role();
        r.setId(2);
        user.setRole(r);
        User newUser = this.userService.create(user);
        return this.userService.convertToCreateDTO(newUser);
    }

    public void forgotPassword(String email){
        if (!userService.isExistedEmail(email)) {
            throw new UserNotFoundException("Email " + email + " không tồn tại");
        }
        String otp = String.format("%06d", new Random().nextInt(1000000));
        this.storeOTP(otp, email);
        this.emailService.sendEmailFromTemplateSync(email, "Reset password", "forgotPassword", email, otp);
    }

    // Không gọi trực tiếp deleteOtp() vì @Transactional không hoạt động khi gọi trong cùng một class
    // Lấy bean từ ApplicationContext để Spring áp dụng proxy và quản lý transaction đúng cách
    // No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call
    public Map<String, String> verifyOtp(OTPDto request){
        if (this.isOTPValid(request.getOtp(), request.getEmail())) {
            String tempToken = securityUtil.createResetToken(request.getEmail());
            context.getBean(AuthService.class).deleteOtp(request.getOtp(), request.getEmail());
            return Map.of("tempToken", tempToken);
        } else {
            throw new ResourceInvalidException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
    }

    public void resetPassword(String token, ResetPasswordDTO request){
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

    public Map<String, Object> login(LoginDTO loginDTO){
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
        String email = oauth2User.getAttribute("email");
        User currentUserDB = userService.getUserByUsername(email);
        this.userService.checkAccountBanned(currentUserDB);
        //khi đăng nhập bằng mật khẩu, Spring Security tự động xử lý authorities từ UserDetailsService
        //trong khi đăng nhập Google không có sẵn
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + currentUserDB.getRole().getRoleName()));
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
        return Map.of(
                "userInfo", res,
                "refreshToken", refresh_token
        );
    }
}
