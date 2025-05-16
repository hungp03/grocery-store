package com.store.grocery.service.impl;

import com.store.grocery.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private static final String BLACKLIST_PREFIX = "tk:bl:";

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;

    @Override
    public void blacklistToken(String token) {
        Jwt jwt = jwtService.decodeToken(token);
        Object jtiObj = jwt.getClaim("jti");
        if (jtiObj == null) {
            // Pass or log
            return;
        }
        String jti = jtiObj.toString();

        Instant expirationTime = jwt.getExpiresAt();
        if (expirationTime == null) {
            // Pass
            return;
        }

        long ttl = expirationTime.getEpochSecond() - Instant.now().getEpochSecond();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, Boolean.TRUE.toString(), ttl, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        Jwt jwt = jwtService.decodeToken(token);
        String jti = jwt.getClaim("jti");
        Boolean exists = redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
        return exists != null && exists;
    }
}
