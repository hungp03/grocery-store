package com.store.grocery.domain.response.feedback;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackDTO {
    private long id;
    private String userName;
    private String userAvatarUrl;
    private String product_name;
    private int ratingStar;
    private String description;
    private boolean status;
    private Instant updatedAt;

}
