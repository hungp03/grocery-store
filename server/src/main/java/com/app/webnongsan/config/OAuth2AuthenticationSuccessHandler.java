package com.app.webnongsan.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${cors.client1}")
    private String client1;
    @Value("${cors.client2}")
    private String client2;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String referer = request.getHeader("Referer");
        String targetUrl;
        if (referer != null && referer.startsWith(client2)) {
            targetUrl = client2;
        } else {
            targetUrl = client1;
        }
        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", "google-auth-success")
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}