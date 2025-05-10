package com.store.grocery.dto.request.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateRequest {
    @Min(value = 0, message = "Trạng thái đơn hàng phải từ 0 đến 3")
    @Max(value = 3, message = "Trạng thái đơn hàng phải từ 0 đến 3")
    private Integer status;
}
