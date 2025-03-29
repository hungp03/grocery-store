package com.store.grocery.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
public class Feedback implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private int ratingStar;
    private String description;

    @Column(nullable = false)
    private boolean status;

    private Instant updatedAt;

    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @PrePersist
    public void handleCreateFeedback() {
        this.timestamp = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void hadnleBeforeUpdate(){
        this.updatedAt = Instant.now();
    }
}
