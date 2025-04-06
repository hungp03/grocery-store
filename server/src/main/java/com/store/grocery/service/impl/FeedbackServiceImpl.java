package com.store.grocery.service.impl;

import com.store.grocery.domain.Feedback;
import com.store.grocery.domain.Product;
import com.store.grocery.domain.User;
import com.store.grocery.dto.request.feedback.CreateFeedbackRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.feedback.FeedbackResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public FeedbackResponse addFeedback(CreateFeedbackRequest feedbackDTO) {
        User user = userService.getUserById(SecurityUtil.getUserId());
        Product product = productRepository.findById(feedbackDTO.getProductId())
                .orElseThrow(() -> new ResourceInvalidException("Sản phẩm không tồn tại"));

        Feedback feedback = feedbackRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElseGet(() -> {
                    log.info("Creating new feedback for product: {} by user: {}", product.getId(), user.getId());
                    Feedback newFeedback = new Feedback();
                    newFeedback.setUser(user);
                    newFeedback.setProduct(product);
                    newFeedback.setStatus(true);
                    return newFeedback;
                });

        feedback.setDescription(feedbackDTO.getDescription());
        feedback.setRatingStar(feedbackDTO.getRating());

        feedbackRepository.save(feedback);
        product.setRating(feedbackRepository.calculateAverageRatingByProductId(product.getId()));
        productRepository.save(product);
        log.info("Feedback has been added");
        return convertToFeedbackDTO(feedback);
    }

    @Override
    public PaginationResponse getBySortAndFilter(Pageable pageable, Boolean status, String sort) {
        log.info("Getting feedbacks with status and sort");
        if (sort != null) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sort).descending());
        }
        Page<FeedbackResponse> feedbackPage = this.feedbackRepository.findByStatus(status, pageable);
        PaginationResponse p = new PaginationResponse();
        PaginationResponse.Meta meta = new PaginationResponse.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(feedbackPage.getTotalPages());
        meta.setTotal(feedbackPage.getTotalElements());
        p.setMeta(meta);
        p.setResult(feedbackPage.getContent());
        log.info("Feedbacks have been retrieved");
        return p;
    }
    @Override
    public void changeFeedbackStatus(Long id) {
        feedbackRepository.findById(id).ifPresentOrElse(feedback -> {
            feedback.setStatus(!feedback.isStatus());
            feedbackRepository.save(feedback);
            log.info("Feedback status updated for ID {}", id, feedback.isStatus());
        }, () -> log.warn("Feedback not found with ID: {}", id));
    }

    @Override
    public PaginationResponse getFeedbacksWithAdjustedSize(Long productId, Integer size, Pageable pageable) {
        if (size == null || size < 1) {
            long totalEls = this.feedbackRepository.countByProductId(productId);
            size = totalEls > 0 ? (int) totalEls : 1;
        }

        Pageable updatedPageable = PageRequest.of(pageable.getPageNumber(), size);
        return getByProductId(productId, updatedPageable);
    }
    @Override
    public PaginationResponse getByProductId(Long productId, Pageable pageable) {
        log.info("Getting feedbacks by product ID: {}", productId);
        Page<FeedbackResponse> feedbackPage = this.feedbackRepository.findByProductId(productId, pageable);
        PaginationResponse p = new PaginationResponse();
        PaginationResponse.Meta meta = new PaginationResponse.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(feedbackPage.getTotalPages());
        meta.setTotal(feedbackPage.getTotalElements());
        p.setMeta(meta);
        p.setResult(feedbackPage.getContent());
        log.info("Feedbacks have been retrieved");
        return p;
    }

    private FeedbackResponse convertToFeedbackDTO(Feedback feedback) {
        log.info("Converting feedback to feedback DTO");
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getUser().getName(),
                feedback.getUser().getAvatarUrl(),
                feedback.getProduct().getProductName(),
                feedback.getRatingStar(),
                feedback.getDescription(),
                feedback.isStatus(),
                feedback.getUpdatedAt()
        );
    }
}
