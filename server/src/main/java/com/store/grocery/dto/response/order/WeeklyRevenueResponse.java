package com.store.grocery.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WeeklyRevenueResponse {
    private String days;
    private double totalRevenue;
}
