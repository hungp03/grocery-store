package com.store.grocery.repository;

import com.store.grocery.domain.OTPCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OTPCodeRepository extends JpaRepository<OTPCode, String> {
    Optional<OTPCode> findByEmail(String email);
    void deleteByEmail(String email);
    // Xóa tất cả OTP đã hết hạn
    void deleteByExpiresAtBefore(Instant now);
}
