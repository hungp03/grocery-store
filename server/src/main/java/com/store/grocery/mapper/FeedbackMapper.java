package com.store.grocery.mapper;

import com.store.grocery.domain.Feedback;
import com.store.grocery.dto.response.feedback.FeedbackResponse;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {
    public FeedbackResponse toFeedbackResponse(Feedback feedback) {
        if (feedback == null) {
            return null;
        }

        FeedbackResponse response = new FeedbackResponse();
        response.setId(feedback.getId());

        if (feedback.getUser() != null) {
            response.setUserName(feedback.getUser().getName());
            response.setUserAvatarUrl(feedback.getUser().getAvatarUrl());
        }

        if (feedback.getProduct() != null) {
            response.setProductName(feedback.getProduct().getProductName());
        }

        response.setRatingStar(feedback.getRatingStar());
        response.setDescription(feedback.getDescription());
        response.setStatus(feedback.isStatus());
        response.setUpdatedAt(feedback.getUpdatedAt());

        return response;
    }
}
