package com.store.grocery.service.impl;

import com.store.grocery.domain.OTPCode;
import com.store.grocery.dto.response.auth.OtpVerificationResponse;
import com.store.grocery.repository.OTPCodeRepository;
import com.store.grocery.service.OTPService;
import com.store.grocery.util.enums.OTPType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {
    private final OTPCodeRepository otpCodeRepository;

    @Override
    public void storeOTP(String otp, String email, OTPType otpType) {
        log.info("Storing OTP for email: {} with type: {}", email, otpType);
        OTPCode otpCode = otpCodeRepository.findByEmailAndType(email, otpType).orElse(new OTPCode());
        otpCode.setEmail(email);
        otpCode.setOtpCode(otp);
        otpCode.setType(otpType);
        otpCodeRepository.save(otpCode);
        log.info("OTP successfully stored for email: {}", email);
    }

    @Override
    public boolean verifyOTP(String email, String inputOtp, OTPType otpType) {
        log.debug("Verify OTP for email: {}, type: {}", email, otpType);
        return otpCodeRepository.findByEmailAndType(email, otpType)
                .filter(otp -> otp.getOtpCode().equals(inputOtp) && otp.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    public void deleteOtpByEmailAndType(String email, OTPType otpType) {
        log.info("Deleting OTP for email: {}, type: {}", email, otpType);
        otpCodeRepository.deleteByEmailAndType(email, otpType);
        log.info("Deleted OTP for email: {}, type: {}", email, otpType);
    }

    @Override
    public String generateOTP() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
