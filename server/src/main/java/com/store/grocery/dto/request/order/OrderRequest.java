package com.store.grocery.dto.request.order;

import com.store.grocery.dto.response.order.OrderDetailResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "0\\d{9}", message = "Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số")
    private String phone;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @Pattern(regexp = "COD|VNPAY", message = "Phương thức thanh toán chỉ được là COD hoặc VNPAY")
    private String paymentMethod;

    @NotNull(message = "Tổng giá không được để trống")
    @Positive(message = "Tổng giá phải lớn hơn 0")
    private Double totalPrice;

    private List<OrderDetailResponse> items;
}
