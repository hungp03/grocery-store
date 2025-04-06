package com.store.grocery.dto.response.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private long id;

    private String name;

    private String email;

    private boolean status;

    private String phone;

    private String address;

    private String avatarUrl;
}
