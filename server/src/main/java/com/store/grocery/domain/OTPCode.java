package com.store.grocery.domain;

import com.store.grocery.util.enums.OTPType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Entity
@Getter
@Setter
@Table(name = "otp_codes", indexes = {
        @Index(name = "idx_email_type", columnList = "email, type")
})
public class OTPCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Khóa chính tự động tăng

    @Column(nullable = false)
    private String email; // Không làm khóa chính nữa

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OTPType type;

    @Column(nullable = false, length = 6)
    private String otpCode;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
        this.expiresAt = Instant.now().plus(5, TimeUnit.MINUTES.toChronoUnit());
    }
}
