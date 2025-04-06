package com.store.grocery.dto.response.user;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class DeviceResponse {
    private String deviceInfo;
    private Instant loginTime;
    private String deviceHash;
    private Boolean isCalledDevice;
}
