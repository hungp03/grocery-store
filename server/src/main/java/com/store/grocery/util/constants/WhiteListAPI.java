package com.store.grocery.util.constants;

public class WhiteListAPI {
    public static final String[] whiteList = {
            "/", "api/v2/ping" ,"/api/v2/auth/login",
            "/api/v2/auth/refresh",
            "/api/v2/auth/register",
            "/api/v2/auth/forgot",
            "/api/v2/auth/reset-password",
            "/api/v2/auth/otp/verify",
            "/api/v2/auth/google-login",
            "/storage/**",
            "/oauth2/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };
}
