package com.store.grocery.dto.response.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponse {
    private long id;
    private String userName;
    private String userAvatarUrl;
    private String product_name;
    private int ratingStar;
    private String description;
    private boolean status;
    private Instant updatedAt;

}
