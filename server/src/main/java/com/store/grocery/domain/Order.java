package com.store.grocery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Instant orderTime;

    private Instant deliveryTime;

    @Column(nullable = false)
    private int status;

    private String paymentMethod;

    private String address;

    private String phone;

    private double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @PreUpdate
    public void validateStatus() {
        if (status < 0 || status > 3) {
            throw new IllegalArgumentException("Status must be between 0 and 3");
        }
    }

    @PrePersist
    public void handleBeforeCreate() {
        validateStatus();
        this.orderTime = Instant.now();
    }
}
