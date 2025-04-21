package com.store.grocery.service;

import com.store.grocery.dto.response.auth.OtpVerificationResponse;
import com.store.grocery.util.enums.OTPType;

public interface OTPService {
    void storeOTP(String otp, String email, OTPType otpType);
    boolean verifyOTP(String email, String inputOtp, OTPType otpType);
    String generateOTP();
    void deleteOtpByEmailAndType(String email, OTPType otpType);
    boolean checkExistOTP(String email, OTPType otpType);
}
