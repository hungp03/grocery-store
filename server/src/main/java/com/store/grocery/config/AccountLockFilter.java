package com.store.grocery.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.grocery.domain.User;
import com.store.grocery.dto.response.RestResponse;
import com.store.grocery.repository.UserRepository;
import com.store.grocery.util.constants.StatusCodeConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class AccountLockFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // kiểm tra khi đã authenticated
        if (authentication != null && authentication.isAuthenticated()) {

            User user = userRepository.findByEmail(authentication.getName());
            if (user != null && !user.isStatus()) {
                SecurityContextHolder.clearContext();
                RestResponse<Object> restResponse = new RestResponse<>();
                restResponse.setStatusCode(StatusCodeConstant.AUTH_EXCEPTION_STATUS);
                restResponse.setData(null);
                restResponse.setError("Account is locked");
                restResponse.setMessage("Tài khoản đã bị vô hiệu hóa");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(restResponse));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
