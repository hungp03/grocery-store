package com.store.grocery.repository;

import com.store.grocery.domain.OTPCode;
import com.store.grocery.util.enums.OTPType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OTPCodeRepository extends JpaRepository<OTPCode, Long> {
    Optional<OTPCode> findByEmailAndType(String email, OTPType otpType);
    // Xóa tất cả OTP đã hết hạn
    void deleteByExpiresAtBefore(Instant now);
    void deleteByEmailAndType(String email, OTPType resetPassword);
}
