package com.store.grocery.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Tên không được để trống")
    private String name;

    @NotBlank(message = "Không được để trống email")
    @Column(unique = true, nullable = false)
    @Email(message = "Email không hợp lệ")
    private String email;

//    @NotBlank(message = "Không được để trống password")
    private String password;

    @Column(nullable = false)
    private boolean status;

    private String phone;

    private String address;
    private String provider;
    private String providerId;
    private String avatarUrl;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Feedback> feedbacks;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Order> orders;

}
