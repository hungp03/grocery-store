package com.store.grocery.service.impl;

import com.store.grocery.domain.Feedback;
import com.store.grocery.domain.Product;
import com.store.grocery.domain.User;
import com.store.grocery.dto.request.feedback.CreateFeedbackRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.feedback.FeedbackResponse;
import com.store.grocery.mapper.FeedbackMapper;
import com.store.grocery.repository.FeedbackRepository;
import com.store.grocery.repository.ProductRepository;
import com.store.grocery.service.FeedbackService;
import com.store.grocery.service.UserService;
import com.store.grocery.util.SecurityUtil;
import com.store.grocery.util.exception.ResourceInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final FeedbackMapper feedbackMapper;

    @Override
    @Transactional
    public FeedbackResponse addFeedback(CreateFeedbackRequest feedbackDTO) {
        User user = userService.findById(SecurityUtil.getUserId());
        Product product = productRepository.findByIdAndIsActiveTrue(feedbackDTO.getProductId())
                .orElseThrow(() -> new ResourceInvalidException("Sản phẩm không tồn tại hoặc ngừng kinh doanh"));

        Feedback feedback = feedbackRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElseGet(() -> {
                    log.info("Creating new feedback for product: {} by user: {}", product.getId(), user.getId());
                    return Feedback.builder().user(user).product(product).status(true).build();
                });

        feedback.setDescription(feedbackDTO.getDescription());
        feedback.setRatingStar(feedbackDTO.getRating());
        feedbackRepository.save(feedback);
        product.setRating(feedbackRepository.calculateAverageRatingByProductId(product.getId()));
        productRepository.save(product);
        log.info("Feedback has been added");
        return feedbackMapper.toFeedbackResponse(feedback);
    }

    @Override
    public PaginationResponse getBySortAndFilter(Pageable pageable, Boolean status, String sort) {
        log.info("Getting feedbacks with status and sort");
        if (sort != null) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sort).descending());
        }
        Page<FeedbackResponse> feedbackPage = this.feedbackRepository.findByStatus(status, pageable);
        PaginationResponse p = PaginationResponse.from(feedbackPage, pageable);
        log.info("Feedbacks have been retrieved");
        return p;
    }

    @Override
    public boolean changeFeedbackStatus(Long id) {
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(id);

        if (optionalFeedback.isPresent()) {
            Feedback feedback = optionalFeedback.get();
            boolean currentStatus = feedback.isStatus();
            feedback.setStatus(!currentStatus);
            feedbackRepository.save(feedback);
            log.info("Feedback status updated for ID {}. New status: {}", id, !currentStatus);
            return !currentStatus;
        } else {
            log.warn("Feedback not found with ID: {}", id);
            throw new ResourceInvalidException("Không tìm thấy feedback ID: " + id);
        }
    }

    @Override
    public PaginationResponse getByProductId(Long productId, Pageable pageable) {
        log.info("Getting feedbacks by product ID: {}", productId);
        Page<FeedbackResponse> feedbackPage = this.feedbackRepository.findByProductId(productId, pageable);
        return PaginationResponse.from(feedbackPage, pageable);
    }
}
