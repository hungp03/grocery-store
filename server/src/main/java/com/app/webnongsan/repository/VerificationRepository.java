package com.app.webnongsan.repository;

import com.app.webnongsan.domain.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationCode, UUID> {
    VerificationCode findByOtpAndEmail(String otp, String email);

    void deleteByOtpAndEmail(String otp, String email);
}
