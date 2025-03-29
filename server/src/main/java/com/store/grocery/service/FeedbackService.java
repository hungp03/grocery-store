package com.store.grocery.service;

import com.store.grocery.domain.Feedback;
import com.store.grocery.domain.request.feedback.CreateFeedbackDTO;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.feedback.FeedbackDTO;
import org.springframework.data.domain.Pageable;


public interface FeedbackService {
    FeedbackDTO addFeedback(CreateFeedbackDTO feedbackDTO);
    PaginationDTO getBySortAndFilter(Pageable pageable, Boolean status, String sort);
    void changeFeedbackStatus(Long id);
    PaginationDTO getFeedbacksWithAdjustedSize(Long productId, Integer size, Pageable pageable);
    PaginationDTO getByProductId(Long productId, Pageable pageable);
}
