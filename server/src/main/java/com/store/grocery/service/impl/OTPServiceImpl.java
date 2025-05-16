package com.store.grocery.service.impl;

import com.store.grocery.service.OTPService;
import com.store.grocery.util.enums.OTPType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void storeOTP(String otp, String email, OTPType otpType) {
        log.info("Storing OTP for email: {} with type: {}", email, otpType);
        redisTemplate.opsForValue().set(email + otpType.toString(), otp, 5, TimeUnit.MINUTES);
        log.info("OTP successfully stored for email: {}", email);
    }

    @Override
    public boolean verifyOTP(String email, String inputOtp, OTPType otpType) {
        log.debug("Verify OTP for email: {}, type: {}", email, otpType);
        String key = email + otpType.toString();
        String savedOtp = redisTemplate.opsForValue().get(key);
        return savedOtp != null && savedOtp.equals(inputOtp);
    }

    @Override
    public void deleteOtpByEmailAndType(String email, OTPType otpType) {
        log.info("Deleting OTP for email: {}, type: {}", email, otpType);
        String key = email + otpType.toString();
        redisTemplate.delete(key);
        log.info("Deleted OTP for email: {}, type: {}", email, otpType);
    }

    @Override
    public String generateOTP() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
