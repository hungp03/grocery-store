package com.app.webnongsan.repository;

import com.app.webnongsan.domain.User;
import com.app.webnongsan.domain.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByUserAndDeviceInfo(User user, String deviceInfo);

    Optional<UserToken> findByRefreshTokenAndDeviceHash(String refreshToken, String abc);

    Optional<UserToken> findByUserIdAndDeviceHash(long id, String hash);

    List<UserToken> findByUserId(long userId);
}
