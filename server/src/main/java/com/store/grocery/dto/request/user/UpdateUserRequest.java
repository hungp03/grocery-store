package com.store.grocery.dto.request.user;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String phone;
    private String address;
    private String avatarUrl;
}
