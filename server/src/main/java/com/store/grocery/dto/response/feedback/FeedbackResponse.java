package com.store.grocery.dto.response.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponse implements Serializable {
    private long id;
    private String userName;
    private String userAvatarUrl;
    private String productName;
    private int ratingStar;
    private String description;
    private boolean status;
    private Instant updatedAt;
}
