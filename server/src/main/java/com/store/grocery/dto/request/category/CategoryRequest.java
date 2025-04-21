package com.store.grocery.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    @NotBlank(message = "Không được bỏ trống tên phân loại")
    private String name;
    private String imageUrl;
}
