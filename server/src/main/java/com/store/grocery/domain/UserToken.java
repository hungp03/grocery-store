package com.store.grocery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(
        name = "user_tokens",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "device_hash"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserToken implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, unique = true, length = 512)
    private String refreshToken;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    private String deviceInfo;
    private String deviceHash;
}

