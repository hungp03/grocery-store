package com.store.grocery.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {
    private long id;
    private boolean status;
}
