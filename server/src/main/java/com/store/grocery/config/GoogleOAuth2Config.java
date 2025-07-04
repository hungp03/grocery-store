package com.store.grocery.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.api.client.json.gson.GsonFactory;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class GoogleOAuth2Config {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.android-client-id}")
    private String androidClientId;
    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        NetHttpTransport transport = new NetHttpTransport();
        GsonFactory jsonFactory = new GsonFactory();

        return new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
//                .setAudience(Collections.singletonList(googleClientId))
                .setAudience(Arrays.asList(googleClientId, androidClientId))
                .build();
    }
}
