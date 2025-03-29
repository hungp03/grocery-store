package com.store.grocery.service.impl;

import com.store.grocery.domain.Feedback;
import com.store.grocery.domain.Product;
import com.store.grocery.domain.User;
import com.store.grocery.domain.request.feedback.CreateFeedbackDTO;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.feedback.FeedbackDTO;
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

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public FeedbackDTO addFeedback(CreateFeedbackDTO feedbackDTO) {
        User user = userService.getUserById(SecurityUtil.getUserId());
        Product product = productRepository.findById(feedbackDTO.getProductId())
                .orElseThrow(() -> new ResourceInvalidException("Sản phẩm không tồn tại"));

        Feedback feedback = feedbackRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElseGet(() -> {
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
        return convertToFeedbackDTO(feedback);
    }

    @Override
    public PaginationDTO getBySortAndFilter(Pageable pageable, Boolean status, String sort) {
        if (sort != null) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sort).descending());
        }
        Page<FeedbackDTO> feedbackPage = this.feedbackRepository.findByStatus(status, pageable);
        PaginationDTO p = new PaginationDTO();
        PaginationDTO.Meta meta = new PaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(feedbackPage.getTotalPages());
        meta.setTotal(feedbackPage.getTotalElements());
        p.setMeta(meta);
        p.setResult(feedbackPage.getContent());
        return p;
    }
    @Override
    public void changeFeedbackStatus(Long id) {
        feedbackRepository.findById(id).ifPresent(feedback -> {
            feedback.setStatus(!feedback.isStatus());
            feedbackRepository.save(feedback);
        });
    }

    @Override
    public PaginationDTO getFeedbacksWithAdjustedSize(Long productId, Integer size, Pageable pageable) {
        if (size == null || size < 1) {
            long totalEls = this.feedbackRepository.countByProductId(productId);
            size = totalEls > 0 ? (int) totalEls : 1;
        }

        Pageable updatedPageable = PageRequest.of(pageable.getPageNumber(), size);
        return getByProductId(productId, updatedPageable);
    }
    @Override
    public PaginationDTO getByProductId(Long productId, Pageable pageable) {

        Page<FeedbackDTO> feedbackPage = this.feedbackRepository.findByProductId(productId, pageable);
        PaginationDTO p = new PaginationDTO();
        PaginationDTO.Meta meta = new PaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(feedbackPage.getTotalPages());
        meta.setTotal(feedbackPage.getTotalElements());
        p.setMeta(meta);
        p.setResult(feedbackPage.getContent());
        return p;
    }

    private FeedbackDTO convertToFeedbackDTO(Feedback feedback) {
        return new FeedbackDTO(
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
