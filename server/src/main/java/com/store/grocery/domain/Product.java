package com.store.grocery.domain;

import com.store.grocery.util.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Tên không được để trống")
    @Column(nullable = false, unique = true)
    private String productName;

    @DecimalMin(value = "0.1", message = "Giá sản phẩm phải lớn hơn 0")
    private double price;

    private String imageUrl;

    @Min(value = 0, message = "Số lượng không thể âm")
    private int quantity;

    @Min(value = 0, message = "Rating không hợp lệ")
    @Max(value = 5, message = "Rating tối đa là 5")
    private double rating;

    private int sold;

    private Instant createdAt;
    private String createdBy;

    private Instant updatedAt;

    @Size(max = 20, message = "Đơn vị không được vượt quá 20 ký tự")
    private String unit;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @NotNull(message = "Category không được để trống")
    @JsonBackReference
    private Category category;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    @JsonIgnore
    private List<Feedback> feedbacks;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    @JsonIgnore
    private List<OrderDetail> orderDetails;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    @JsonIgnore
    private List<Wishlist> wishlist;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate(){
        this.updatedAt = Instant.now();
    }
}
