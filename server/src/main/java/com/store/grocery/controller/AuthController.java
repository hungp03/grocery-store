package com.store.grocery.controller;

import com.store.grocery.domain.User;
import com.store.grocery.domain.request.auth.*;
import com.store.grocery.domain.request.auth.*;
import com.store.grocery.domain.response.user.CreateUserDTO;
import com.store.grocery.domain.response.user.ResLoginDTO;
import com.store.grocery.service.*;
import com.store.grocery.service.AuthService;
import com.store.grocery.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@RestController
@RequestMapping("api/v2")
public class AuthController {

    private final AuthService authService;
    @Value("${jwt.refreshtoken-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("auth/login")
    @ApiMessage("Login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO, @RequestHeader("User-Agent") String userAgent) {
        Map<String, Object> response = this.authService.login(loginDTO, userAgent);
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", (String) response.get("refreshToken"))
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("Lax")
                .build();
        ResponseCookie deviceCookie = ResponseCookie.from("device", (String) response.get("device"))
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deviceCookie.toString())
                .body((ResLoginDTO) response.get("userInfo"));
    }


    @GetMapping("auth/account")
    @ApiMessage("Get user")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        return ResponseEntity.ok(this.authService.getAccount());
    }

    @GetMapping("auth/refresh")
    @ApiMessage("Get new token")
    public ResponseEntity<ResLoginDTO> getNewRefreshToken(@CookieValue(name = "refresh_token", defaultValue = "none") String refreshToken,
                                                          @CookieValue(name = "device", defaultValue = "none") String deviceHash
                                                          ) {
        Map<String, Object> response = this.authService.getNewRefreshToken(refreshToken, deviceHash);
        // set cookies
        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", (String) response.get("refreshToken"))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        ResponseCookie deviceCookie = ResponseCookie
                .from("device", deviceHash)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration) // Đồng bộ với refresh token
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deviceCookie.toString())
                .body((ResLoginDTO) response.get("userInfo"));
    }

    @PostMapping("auth/logout")
    @ApiMessage("Logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "device", defaultValue = "none") String deviceHash) {
        this.authService.logout(deviceHash);
        ResponseCookie deleteCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        ResponseCookie deviceCookie = ResponseCookie.from("device", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deviceCookie.toString())
                .build();
    }

    @PostMapping("auth/register")
    @ApiMessage("Register a user")
    public ResponseEntity<CreateUserDTO> register(@Valid @RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.authService.register(user));
    }

    @PostMapping("auth/forgot")
    @ApiMessage("Forgot password - OTP")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody EmailRequestDTO emailRequest) {
        this.authService.forgotPassword(emailRequest.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("auth/validate-otp")
    @ApiMessage("Validate OTP")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody OTPDto request) {
        return ResponseEntity.ok(this.authService.verifyOtp(request.getEmail(), request.getOtp()));
    }

    @PutMapping("auth/reset-password")
    @ApiMessage("Reset password")
    public ResponseEntity<Void> resetPassword(
            @RequestParam("token") String token,
            @Valid @RequestBody ResetPasswordDTO request) {
        this.authService.resetPassword(token, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("auth/signin/google")
    @ApiMessage("Login with Google")
    public ResponseEntity<ResLoginDTO> loginWithGoogle(@Valid @RequestBody GoogleTokenRequest request,
                                                       @RequestHeader("User-Agent") String userAgent) throws GeneralSecurityException, IOException {
        Map<String, Object> response = this.authService.loginGoogle(request, userAgent);
        // Tạo cookie cho refresh token
        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", (String) response.get("refreshToken"))
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("Lax")
                .build();

        ResponseCookie deviceCookie = ResponseCookie.from("device", (String) response.get("device"))
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deviceCookie.toString())
                .body((ResLoginDTO) response.get("userInfo"));
    }
}
