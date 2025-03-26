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
@Table(name = "otp_codes")
public class OTPCode {
    @Id
    private String email;
    @Column(nullable = false, length = 6)
    private String otpCode;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant expiresAt;
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
//    @Column(nullable = false, length = 16)
    private OTPType type;
    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
        this.expiresAt = Instant.now().plus(5, TimeUnit.MINUTES.toChronoUnit());
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.expiresAt = Instant.now().plus(5, TimeUnit.MINUTES.toChronoUnit());
    }
}
