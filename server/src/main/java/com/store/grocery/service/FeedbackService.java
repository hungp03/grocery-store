package com.store.grocery.service;

import com.store.grocery.dto.request.feedback.CreateFeedbackRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.feedback.FeedbackResponse;
import org.springframework.data.domain.Pageable;


public interface FeedbackService {
    FeedbackResponse addFeedback(CreateFeedbackRequest feedbackDTO);
    PaginationResponse getBySortAndFilter(Pageable pageable, Boolean status, String sort);
    boolean changeFeedbackStatus(Long id);
    PaginationResponse getByProductId(Long productId, Pageable pageable);
}
