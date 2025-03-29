package com.store.grocery.domain.request.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFeedbackDTO {
    private long productId;
    @Min(value = 0, message = "Rating từ 0-5")
    @Max(value = 5, message = "Rating từ 0-5")
    private int rating;
    private String description;
}
