package com.store.grocery.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    private String name;
    private String phone;
    private String address;
    private String avatarUrl;
}
