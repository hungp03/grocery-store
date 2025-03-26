package com.store.grocery.domain.request.user;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String phone;
    private String address;
}
