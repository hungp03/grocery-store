package com.store.grocery.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "Tên không được để trống")
    private String productName;
    @NotNull(message = "Tổng giá không được để trống")
    @Positive(message = "Tổng giá phải lớn hơn 0")
    private Double price;
    @Min(value = 0, message = "Số lượng không thể âm")
    private int quantity;
    private String description;
    private String imageUrl;
    @Size(max = 20, message = "Đơn vị không được vượt quá 20 ký tự")
    private String unit;
    private CategoryDTO category;
    @Getter
    @Setter
    public static class CategoryDTO {
        @NotNull
        private Long id;
    }
}
