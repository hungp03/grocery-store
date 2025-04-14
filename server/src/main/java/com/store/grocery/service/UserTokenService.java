package com.store.grocery.service;

import com.store.grocery.domain.User;
import com.store.grocery.domain.UserToken;
import com.store.grocery.dto.response.user.DeviceResponse;

import java.util.List;

public interface UserTokenService {
    UserToken findByfindByUserAndDeviceHash(long uid, String deviceHash);
    void deleteToken(UserToken userToken);
    UserToken validateRefreshToken(String refreshToken, String deviceHash);
    void saveToken(UserToken userToken);
    void storeUserToken(User user, String refreshToken, String deviceInfo, String deviceHash);
    List<UserToken> findDevicesByUser(long uid);
}
