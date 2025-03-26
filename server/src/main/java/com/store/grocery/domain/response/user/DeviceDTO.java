package com.store.grocery.domain.response.user;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class DeviceDTO {
    private String deviceInfo;
    private Instant loginTime;
    private String deviceHash;
    private Boolean isCalledDevice;
}
