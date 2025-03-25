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
public class AuthController {

    private final AuthService authService;
    @Value("${jwt.refreshtoken-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("auth/login")
    @ApiMessage("Login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> response = this.authService.login(loginDTO);
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


    @GetMapping("auth/account")
    @ApiMessage("Get user")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        return ResponseEntity.ok(this.authService.getAccount());
    }

    @GetMapping("auth/refresh")
    @ApiMessage("Get new token")
    public ResponseEntity<ResLoginDTO> getNewRefreshToken(@CookieValue(name = "refresh_token", defaultValue = "none") String refreshToken) {
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
    public ResponseEntity<Void> logout() {
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
    public ResponseEntity<ResLoginDTO> loginWithGoogle(@Valid @RequestBody GoogleTokenRequest request) throws GeneralSecurityException, IOException {
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
