package com.app.webnongsan.service;

import com.app.webnongsan.domain.Feedback;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.domain.response.feedback.FeedbackDTO;
import org.springframework.data.domain.Pageable;


public interface FeedbackService {
    Feedback addFeedback(FeedbackDTO feedbackDTO);
    PaginationDTO getBySortAndFilter(Pageable pageable, Integer status, String sort);
    FeedbackDTO hideFeedback(Long id);
    PaginationDTO getFeedbacksWithAdjustedSize(Long productId, Integer size, Pageable pageable);
    PaginationDTO getByProductId(Long productId, Pageable pageable);
}
