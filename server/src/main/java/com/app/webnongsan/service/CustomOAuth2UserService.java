package com.app.webnongsan.service;

import com.app.webnongsan.config.CustomGoogleUserDetails;
import com.app.webnongsan.domain.Role;
import com.app.webnongsan.domain.User;
import com.app.webnongsan.repository.UserRepository;
import com.app.webnongsan.util.exception.AuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String idToken = (String) oauth2User.getAttributes().get("id_token");
        try {
            return processOAuth2User(idToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OAuth2User processOAuth2User(String idToken) throws GeneralSecurityException, IOException {
        // Xác thực token với Google
        GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
        if (googleIdToken != null) {
            // Lấy email từ payload
            String email = googleIdToken.getPayload().getEmail();
            String name = (String) googleIdToken.getPayload().get("name");
            String picture = (String) googleIdToken.getPayload().get("picture");
            String providerId = googleIdToken.getPayload().getSubject();

            User user = userRepository.findByEmail(email);
            if (user == null) {
                user = registerNewUser(email, name, picture, providerId);
            }

            return new CustomGoogleUserDetails(user, googleIdToken.getPayload());
        }

        throw new AuthException("Invalid ID Token");
    }


    private User registerNewUser(String email, String name, String picture, String providerId) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setAvatarUrl(picture);
        user.setProvider("GOOGLE");
        user.setProviderId(providerId);
        user.setStatus(1);
        
        Role role = new Role();
        role.setId(2L); // USER role
        user.setRole(role);
        
        return userRepository.save(user);
    }
}
