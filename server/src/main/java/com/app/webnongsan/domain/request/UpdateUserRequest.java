package com.app.webnongsan.domain.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String phone;
    private String address;
}
