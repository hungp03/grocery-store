package com.store.grocery.dto.request.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFeedbackRequest {
    @NotNull(message = "Không được bỏ trống productId")
    private Long productId;
    @Min(value = 0, message = "Rating từ 0-5")
    @Max(value = 5, message = "Rating từ 0-5")
    private Integer rating;
    private String description;
}
