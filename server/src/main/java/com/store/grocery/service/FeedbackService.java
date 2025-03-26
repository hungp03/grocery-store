package com.store.grocery.service;

import com.store.grocery.domain.Feedback;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.feedback.FeedbackDTO;
import org.springframework.data.domain.Pageable;


public interface FeedbackService {
    Feedback addFeedback(FeedbackDTO feedbackDTO);
    PaginationDTO getBySortAndFilter(Pageable pageable, Boolean status, String sort);
    FeedbackDTO hideFeedback(Long id);
    PaginationDTO getFeedbacksWithAdjustedSize(Long productId, Integer size, Pageable pageable);
    PaginationDTO getByProductId(Long productId, Pageable pageable);
}
