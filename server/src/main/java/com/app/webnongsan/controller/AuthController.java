package com.app.webnongsan.controller;

import com.app.webnongsan.domain.Role;
import com.app.webnongsan.domain.User;
import com.app.webnongsan.domain.request.*;
import com.app.webnongsan.domain.response.user.CreateUserDTO;
import com.app.webnongsan.domain.response.user.ResLoginDTO;
import com.app.webnongsan.service.*;
import com.app.webnongsan.util.SecurityUtil;
import com.app.webnongsan.util.annotation.ApiMessage;
import com.app.webnongsan.util.exception.AuthException;
import com.app.webnongsan.util.exception.ResourceInvalidException;
import com.app.webnongsan.util.exception.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@RestController
@RequestMapping("api/v2")
@Slf4j
public class AuthController {
    private final UserService userService;
    private final FileService fileService;
    private final AuthService authService;
    private final CartService cartService;
    @Value("${jwt.refreshtoken-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(UserService userService, AuthService authService, CartService cartService, FileService fileService) {
        this.userService = userService;
        this.authService = authService;
        this.fileService = fileService;
        this.cartService = cartService;
    }

    @PostMapping("auth/login")
    @ApiMessage("Login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) throws AuthException, UserNotFoundException {
        Map<String, Object> response = this.authService.login(loginDTO);
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", (String) response.get("refreshToken"))
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("Lax")
                .build();

        log.info("User {} logged in successfully", loginDTO.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body((ResLoginDTO) response.get("userInfo"));
    }


    @GetMapping("auth/account")
    @ApiMessage("Get user")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() throws AuthException, UserNotFoundException {
        return ResponseEntity.ok(this.authService.getAccount());
    }

    @GetMapping("auth/refresh")
    @ApiMessage("Get new token")
    public ResponseEntity<ResLoginDTO> getNewRefreshToken(@CookieValue(name = "refresh_token", defaultValue = "none") String refreshToken) throws ResourceInvalidException, AuthException, UserNotFoundException {
        Map<String, Object> response = this.authService.getNewRefreshToken(refreshToken);
        // set cookies
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", (String) response.get("refreshToken"))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body((ResLoginDTO) response.get("userInfo"));
    }

    @PostMapping("auth/logout")
    @ApiMessage("Logout")
    public ResponseEntity<Void> logout() throws ResourceInvalidException, UserNotFoundException {
        this.authService.logout();
        ResponseCookie deleteCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString()).body(null);
    }

    @PostMapping("auth/register")
    @ApiMessage("Register a user")
    public ResponseEntity<CreateUserDTO> register(@Valid @RequestBody User user) throws ResourceInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.authService.register(user));
    }

    @PostMapping("auth/forgot")
    @ApiMessage("Forgot password - OTP")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody EmailRequestDTO emailRequest) throws UserNotFoundException {
        this.authService.forgotPassword(emailRequest.getEmail());
        return ResponseEntity.ok(null);
    }

    @PostMapping("auth/validate-otp")
    @ApiMessage("Validate OTP")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody OTPDto request) throws ResourceInvalidException {
        return ResponseEntity.ok(this.authService.verifyOtp(request));
    }

    @PutMapping("auth/reset-password")
    @ApiMessage("Reset password")
    public ResponseEntity<Void> resetPassword(
            @RequestParam("token") String token,
            @RequestBody ResetPasswordDTO request) throws ResourceInvalidException, UserNotFoundException {
        this.authService.resetPassword(token, request);
        return ResponseEntity.ok(null);
    }

    @PutMapping("auth/account")
    @ApiMessage("Update user information")
    public ResponseEntity<ResLoginDTO.UserGetAccount> updateUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam(value = "avatarUrl", required = false) MultipartFile avatar) throws IOException, UserNotFoundException {

        String emailLoggedIn = SecurityUtil.getCurrentUserLogin().orElse("");
        log.info("User {} is updating their information", emailLoggedIn);

        // Kiểm tra người dùng có tồn tại không
        User currentUserDB = userService.getUserByUsername(emailLoggedIn);

        log.debug("Updating user: name={}, email={}, phone={}, address={}, hasAvatar={}",
                name, email, phone, address, (avatar != null && !avatar.isEmpty()));

        // Cập nhật thông tin người dùng
        currentUserDB.setName(name);
        currentUserDB.setEmail(email);
        currentUserDB.setPhone(phone);
        currentUserDB.setAddress(address);

        // Nếu có avatar mới, lưu ảnh vào server
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String avatarUrl = fileService.store(avatar, "avatar");
                currentUserDB.setAvatarUrl(avatarUrl);
                log.info("User {} uploaded a new avatar: {}", emailLoggedIn, avatarUrl);
            } catch (IOException e) {
                log.error("Error while saving avatar for user {}: {}", emailLoggedIn, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        // Lưu thông tin cập nhật vào DB
        userService.update(currentUserDB);
        log.info("User {} updated their profile successfully", emailLoggedIn);

        // Chuẩn bị dữ liệu phản hồi
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.getRole());

        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        userGetAccount.setUser(userLogin);
        userGetAccount.setCartLength(cartService.countProductInCart(currentUserDB.getId()));

        return ResponseEntity.ok(userGetAccount);
    }


    @PostMapping("auth/signin/google")
    @ApiMessage("Login with Google")
    public ResponseEntity<ResLoginDTO> loginWithGoogle(@RequestBody GoogleTokenRequest request) throws AuthException, GeneralSecurityException, IOException, UserNotFoundException {
        Map<String, Object> response = this.authService.loginGoogle(request);
        // Tạo cookie cho refresh token
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", (String) response.get("refreshToken"))
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body((ResLoginDTO) response.get("userInfo"));
    }
}
