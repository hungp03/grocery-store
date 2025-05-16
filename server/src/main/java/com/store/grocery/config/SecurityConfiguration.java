
package com.store.grocery.config;

import com.store.grocery.service.TokenBlacklistService;
import com.store.grocery.service.impl.CustomOAuth2UserService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import com.store.grocery.service.impl.JwtService;
import com.store.grocery.util.constants.WhiteListAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {
    @Value("${jwt.base64-secret}")
    private String jwtKey;

    private final CustomOAuth2UserService customOauth2;
    private final OAuth2AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AccountLockFilter accountLockFilter;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        String[] whiteList = WhiteListAPI.whiteList;

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .addFilterAfter(
                        accountLockFilter,
                        BearerTokenAuthenticationFilter.class
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(whiteList).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/products/export/excel").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v2/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/product/*/ratings").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/files").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/payment/vn-pay-callback").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/ratings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v2/ratings/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v2/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v2/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v2/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v2/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v2/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v2/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v2/admin/overview").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v2/reports/revenue/monthly").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "api/v2/orders/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v2/orders").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v2/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v2/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v2/users/devices").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v2/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v2/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "api/v2/users/*/status").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2.userInfoEndpoint(
                                userInfo -> userInfo.userService(customOauth2)
                        ).successHandler(authenticationSuccessHandler)
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    //@FunctionalInterface
    @Bean
    public JwtDecoder jwtDecoder(TokenBlacklistService tokenBlacklistService) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JwtService.JWT_ALGORITHM)
                .build();

        decoder.setJwtValidator(token -> {
            if (tokenBlacklistService.isTokenBlacklisted(token.getTokenValue())) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token is blacklisted", null));
            }
            return OAuth2TokenValidatorResult.success();
        });
        return decoder;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JwtService.JWT_ALGORITHM.getName());
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}

