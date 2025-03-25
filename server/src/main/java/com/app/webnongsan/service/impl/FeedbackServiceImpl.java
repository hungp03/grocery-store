package com.app.webnongsan.service.impl;

import com.app.webnongsan.domain.Feedback;
import com.app.webnongsan.domain.Product;
import com.app.webnongsan.domain.User;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.domain.response.feedback.FeedbackDTO;
import com.app.webnongsan.repository.FeedbackRepository;
import com.app.webnongsan.repository.ProductRepository;
import com.app.webnongsan.service.FeedbackService;
import com.app.webnongsan.service.UserService;
import com.app.webnongsan.util.SecurityUtil;
import com.app.webnongsan.util.exception.ResourceInvalidException;
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

    @Override
    public Feedback addFeedback(FeedbackDTO feedbackDTO) {
        long uid = SecurityUtil.getUserId();
        User u = this.userService.getUserById(uid);
        Product p = productRepository.findById(feedbackDTO.getProductId()).orElseThrow(() -> new ResourceInvalidException("Sản phẩm không tồn tại"));
        boolean exists = this.feedbackRepository.existsByUserIdAndProductId(u.getId(), p.getId());

        Feedback f;
        if (!exists) {
            f = new Feedback();
            f.setProduct(p);
            f.setUser(u);
            f.setDescription(feedbackDTO.getDescription());
            f.setStatus(0);
        } else {
            f = feedbackRepository.findByUserIdAndProductId(u.getId(), p.getId());
            f.setDescription(feedbackDTO.getDescription());
        }
        f.setRatingStar(feedbackDTO.getRatingStar());
        this.feedbackRepository.save(f);
        double averageRating = feedbackRepository.calculateAverageRatingByProductId(p.getId());
        p.setRating(averageRating);
        productRepository.save(p);
        return f;
    }

    @Override
    public PaginationDTO getBySortAndFilter(Pageable pageable, Integer status, String sort) {
        if (sort != null) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sort).descending());
        }
        Page<Feedback> feedbackPage = this.feedbackRepository.findByStatus(status, pageable);

        PaginationDTO p = new PaginationDTO();
        PaginationDTO.Meta meta = new PaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(feedbackPage.getTotalPages());
        meta.setTotal(feedbackPage.getTotalElements());

        p.setMeta(meta);

        List<FeedbackDTO> listFeedback = feedbackPage.getContent().stream()
                .map(this::convertToFeedbackDTO).toList();
        p.setResult(listFeedback);
        return p;
    }
    @Override
    public FeedbackDTO hideFeedback(Long id) {

        Optional<Feedback> feedbackOptional = feedbackRepository.findById(id);
        Feedback f;
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        if (feedbackOptional.isPresent()) {
            f = feedbackOptional.get();
            if (f.getStatus() == 0) f.setStatus(1);
            else f.setStatus(0);
            this.feedbackRepository.save(f);
            feedbackDTO.setId(f.getId());

            feedbackDTO.setUserAvatarUrl(f.getUser().getAvatarUrl());

            feedbackDTO.setProductId(f.getProduct().getId());
            feedbackDTO.setProduct_name(f.getProduct().getProductName());
            feedbackDTO.setImageUrl(f.getProduct().getImageUrl());

            feedbackDTO.setStatus(f.getStatus());
            feedbackDTO.setDescription(f.getDescription());
            feedbackDTO.setRatingStar(f.getRatingStar());
            feedbackDTO.setUpdatedAt(f.getUpdatedAt());
        }
        return feedbackDTO;
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

        Page<Feedback> feedbackPage = this.feedbackRepository.findByProductId(productId, pageable);
        PaginationDTO p = new PaginationDTO();
        PaginationDTO.Meta meta = new PaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(feedbackPage.getTotalPages());
        meta.setTotal(feedbackPage.getTotalElements());
        p.setMeta(meta);
        List<FeedbackDTO> listFeedback = feedbackPage.getContent().stream()
                .map(this::convertToFeedbackDTO).toList();
        p.setResult(listFeedback);
        return p;
    }

    private FeedbackDTO convertToFeedbackDTO(Feedback feedback) {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        User u = this.userService.getUserById(feedback.getUser().getId());
        if (u != null) {
            feedbackDTO.setId(feedback.getId());
            feedbackDTO.setUserId(u.getId());
            feedbackDTO.setUserName(u.getName());
            feedbackDTO.setUserAvatarUrl(feedback.getUser().getAvatarUrl());

            feedbackDTO.setProductId(feedback.getProduct().getId());
            feedbackDTO.setProduct_name(feedback.getProduct().getProductName());
            feedbackDTO.setImageUrl(feedback.getProduct().getImageUrl());

            feedbackDTO.setStatus(feedback.getStatus());
            feedbackDTO.setDescription(feedback.getDescription());
            feedbackDTO.setRatingStar(feedback.getRatingStar());
            feedbackDTO.setUpdatedAt(feedback.getUpdatedAt());
        }
        return feedbackDTO;
    }
}
