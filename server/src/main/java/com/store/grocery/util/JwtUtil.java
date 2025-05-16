package com.store.grocery.util;

import com.store.grocery.util.exception.UnauthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Optional;

public class JwtUtil {
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }


    public static long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {

            Object userClaimObj = jwt.getClaims().get("user");

            if (userClaimObj instanceof Map<?, ?> userClaim) {
                Object idObj = userClaim.get("id");
                if (idObj instanceof Number) {
                    return ((Number) idObj).longValue();
                } else {
                    throw new UnauthenticatedException("Token không hợp lệ");
                }
            } else {
                throw new UnauthenticatedException("Token không hợp lệ");
            }
        }

        throw new UnauthenticatedException("User ID not found or invalid token");
    }
}
