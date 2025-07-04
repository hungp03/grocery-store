package com.store.grocery.service.impl;

import com.store.grocery.dto.response.user.UserLoginResponse;
import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class JwtService{
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    private final JwtEncoder jwtEncoder;

    @Value("${jwt.base64-secret}")
    private String jwtKey;

    @Value("${jwt.accesstoken-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${jwt.refreshtoken-validity-in-seconds}")
    private long refreshTokenExpiration;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    private String createToken(String email, Instant now, Instant validity, Map<String, Object> additionalClaims) {
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email);
        additionalClaims.forEach(claimsBuilder::claim);

        JwtClaimsSet claims = claimsBuilder.build();
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String createAccessToken(String email, UserLoginResponse resLoginDTO) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        UserLoginResponse.UserLogin userLogin = new UserLoginResponse.UserLogin();
        userLogin.setId(resLoginDTO.getUser().getId());
        userLogin.setEmail(resLoginDTO.getUser().getEmail());
        userLogin.setName(resLoginDTO.getUser().getName());
        if (resLoginDTO.getUser().getRole() != null) {
            userLogin.setRole(resLoginDTO.getUser().getRole());
        }

        assert resLoginDTO.getUser().getRole() != null;
        List<String> authorities = List.of("ROLE_" + resLoginDTO.getUser().getRole().getRoleName());
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("user", userLogin);
        additionalClaims.put("authorities", authorities);
        additionalClaims.put("jti",UUID.randomUUID().toString());
        return createToken(email, now, validity, additionalClaims);
    }

    public String createRefreshToken(String email, UserLoginResponse res) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        UserLoginResponse.UserInsideToken userToken = new UserLoginResponse.UserInsideToken();
        userToken.setId(res.getUser().getId());
        userToken.setEmail(res.getUser().getEmail());
        userToken.setName(res.getUser().getName());

        List<String> authorities = List.of("ROLE_" + res.getUser().getRole().getRoleName());

        // Tạo claims bổ sung cho refresh token
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("user", userToken);
        additionalClaims.put("authorities", authorities);

        return createToken(email, now, validity, additionalClaims);
    }

    public String createResetToken(String email) {
        Instant now = Instant.now();
        Instant validity = now.plus(3, ChronoUnit.MINUTES);
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("type", "RESET_PASSWORD");
        additionalClaims.put("jti", UUID.randomUUID().toString());
        return createToken(email, now, validity, additionalClaims);
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }

    public Jwt decodeToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(JwtService.JWT_ALGORITHM).build();
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            System.out.println(">>> Check token error: " + e.getMessage());
            throw e;
        }
    }

}
