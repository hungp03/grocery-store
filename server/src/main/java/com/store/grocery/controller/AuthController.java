package com.store.grocery.controller;

import com.store.grocery.dto.request.user.UserRegisterRequest;
import com.store.grocery.dto.response.auth.AuthResponse;
import com.store.grocery.dto.response.auth.OtpVerificationResponse;
import com.store.grocery.dto.response.user.CreateUserResponse;
import com.store.grocery.dto.response.user.UserLoginResponse;
import com.store.grocery.dto.request.auth.*;
import com.store.grocery.service.AuthService;
import com.store.grocery.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("api/v2")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @Value("${jwt.refreshtoken-validity-in-seconds}")
    private long refreshTokenExpiration;

    @PostMapping("auth/login")
    @ApiMessage("Login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody LoginRequest loginDTO, @RequestHeader("User-Agent") String userAgent) {
        AuthResponse response = this.authService.login(loginDTO, userAgent);
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("None")
                .build();
        ResponseCookie deviceCookie = ResponseCookie.from("device", response.getDevice())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite("None")
                .build();

        // Use HttpHeaders to set multiple cookies
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, deviceCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(response.getUserLoginResponse());
    }


    @GetMapping("auth/me")
    @ApiMessage("Get user")
    public ResponseEntity<UserLoginResponse.UserGetAccount> getMyAccount() {
        return ResponseEntity.ok(this.authService.getMyAccount());
    }

    @PostMapping("auth/refresh")
    @ApiMessage("Get new token")
    public ResponseEntity<UserLoginResponse> renewToken(@CookieValue(name = "refresh_token", defaultValue = "none") String refreshToken,
                                                                @CookieValue(name = "device", defaultValue = "none") String deviceHash) {
        AuthResponse response = this.authService.renewToken(refreshToken, deviceHash);
        // set cookies
        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        ResponseCookie deviceCookie = ResponseCookie
                .from("device", deviceHash)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(refreshTokenExpiration) // Đồng bộ với refresh token
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, deviceCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(response.getUserLoginResponse());
    }

    @PostMapping("auth/logout")
    @ApiMessage("Logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader,
            @CookieValue(name = "device", defaultValue = "none") String deviceHash) {
        this.authService.logout(authHeader, deviceHash);
        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
        ResponseCookie deviceCookie = ResponseCookie.from("device", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, deviceCookie.toString());

        return ResponseEntity
                .ok()
                .headers(headers)
                .build();
    }

    @PostMapping("auth/register")
    @ApiMessage("Register a user")
    public ResponseEntity<CreateUserResponse> register(@Valid @RequestBody UserRegisterRequest user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.authService.register(user));
    }

    @PostMapping("auth/forgot")
    @ApiMessage("Send forgot password email")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        this.authService.forgotPassword(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/otp/verify")
    @ApiMessage("Validate OTP")
    public ResponseEntity<OtpVerificationResponse> verifyOtp(@Valid @RequestBody OTPResetRequest request) {
        return ResponseEntity.ok(this.authService.verifyOtp(request.getEmail(), request.getOtp()));
    }

    @PostMapping("auth/reset-password")
    @ApiMessage("Reset password")
    public ResponseEntity<Void> resetPassword(
            @RequestParam("token") String token,
            @Valid @RequestBody ResetPasswordRequest request) {
        this.authService.resetPassword(token, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("auth/google-login")
    @ApiMessage("Login with Google")
    public ResponseEntity<UserLoginResponse> loginWithGoogle(@Valid @RequestBody GoogleTokenRequest request,
                                                             @RequestHeader("User-Agent") String userAgent) throws GeneralSecurityException, IOException {
        AuthResponse response = this.authService.loginGoogle(request, userAgent);
        // Tạo cookie cho refresh token
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        ResponseCookie deviceCookie = ResponseCookie.from("device", response.getDevice())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, deviceCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(response.getUserLoginResponse());
    }
}
