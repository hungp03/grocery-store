package com.store.grocery.service.impl;

import com.store.grocery.config.CustomGoogleUserDetails;
import com.store.grocery.domain.Role;
import com.store.grocery.domain.User;
import com.store.grocery.repository.UserRepository;
import com.store.grocery.util.exception.AuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Loading OAuth2 user for client registration: {}", userRequest.getClientRegistration().getRegistrationId());
        OAuth2User oauth2User = super.loadUser(userRequest);
        String idToken = (String) oauth2User.getAttributes().get("id_token");
        try {
            return processOAuth2User(idToken);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error processing OAuth2 user", e);
            throw new RuntimeException(e);
        }
    }

    public OAuth2User processOAuth2User(String idToken) throws GeneralSecurityException, IOException {
        // Xác thực token với Google
        log.info("Processing OAuth2 user with ID Token");
        GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
        if (googleIdToken != null) {
            // Lấy email từ payload
            String email = googleIdToken.getPayload().getEmail();
            log.info("Verified Google ID Token for email: {}", email);
            String name = (String) googleIdToken.getPayload().get("name");
            String picture = (String) googleIdToken.getPayload().get("picture");
            String providerId = googleIdToken.getPayload().getSubject();

            User user = userRepository.findByEmail(email);
            if (user == null) {
                log.info("User with email '{}' not found, registering new user", email);
                user = registerNewUser(email, name, picture, providerId);
            }
            return new CustomGoogleUserDetails(user, googleIdToken.getPayload());
        }
        log.warn("Invalid Google ID Token received");
        throw new AuthException("Invalid ID Token");
    }


    private User registerNewUser(String email, String name, String picture, String providerId) {
        log.info("Registering new GOOGLE user with email: {}", email);
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setAvatarUrl(picture);
        user.setProvider("GOOGLE");
        user.setProviderId(providerId);
        user.setStatus(true);
        Role role = new Role();
        role.setId(2L); // USER role
        user.setRole(role);
        return userRepository.save(user);
    }
}
