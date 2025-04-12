package com.store.grocery.service.impl;

import com.store.grocery.domain.User;
import com.store.grocery.domain.UserToken;
import com.store.grocery.dto.response.user.DeviceResponse;
import com.store.grocery.repository.UserTokenRepository;
import com.store.grocery.service.UserTokenService;
import com.store.grocery.util.SecurityUtil;
import com.store.grocery.util.exception.ResourceInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTokenServiceImpl implements UserTokenService {
    private final UserTokenRepository userTokenRepository;
    private final SecurityUtil securityUtil;
    @Override
    public UserToken findByfindByUserAndDeviceHash(long uid, String deviceHash) {
        log.info("Get refresh token for user id: {}", uid);
        Optional<UserToken> userTokenOpt = this.userTokenRepository.findByUserIdAndDeviceHash(uid, deviceHash);
        if (userTokenOpt.isEmpty()){
            log.warn("No refresh token found for user ID {} on device {}", uid, deviceHash);
            throw new ResourceInvalidException("Không tìm thấy phiên đăng nhập trên thiết bị này.");
        }
        return userTokenOpt.get();
    }

    @Override
    public void deleteToken(UserToken userToken) {
        userTokenRepository.delete(userToken);
    }

    public UserToken validateRefreshToken(String refreshToken, String deviceHash) {
        Jwt decodedToken = this.securityUtil.checkValidToken(refreshToken);
        String email = decodedToken.getSubject();
        Optional<UserToken> userTokenOpt = this.userTokenRepository.findByRefreshTokenAndDeviceHash(refreshToken, deviceHash);
        if (userTokenOpt.isEmpty() || !userTokenOpt.get().getUser().getEmail().equals(email)) {
            throw new ResourceInvalidException("Refresh token không hợp lệ hoặc không khớp với thiết bị.");
        }

        return userTokenOpt.get();
    }

    @Override
    public void saveToken(UserToken userToken) {
        this.userTokenRepository.save(userToken);
    }

    @Override
    public void storeUserToken(User user, String refreshToken, String deviceInfo, String deviceHash) {
        log.info("Storing user token for user ID: {}", user.getId());
        Optional<UserToken> existingToken = userTokenRepository.findByUserAndDeviceInfo(user, deviceInfo);

        if (existingToken.isPresent()) {
            log.debug("Updating existing token for user ID: {}", user.getId());
            // Cập nhật refreshToken mới
            UserToken userToken = existingToken.get();
            userToken.setRefreshToken(refreshToken);
            userToken.setDeviceInfo(deviceInfo);
            userTokenRepository.save(userToken);
        } else {
            log.debug("Creating new token for user ID: {}", user.getId());
            // Tạo mới UserToken
            UserToken newUserToken = new UserToken();
            newUserToken.setUser(user);
            newUserToken.setRefreshToken(refreshToken);
            newUserToken.setDeviceInfo(deviceInfo);
            newUserToken.setDeviceHash(deviceHash);
            newUserToken.setCreatedAt(Instant.now());
            userTokenRepository.save(newUserToken);
        }
    }



}
