package com.store.grocery.controller;

import com.store.grocery.domain.request.feedback.CreateFeedbackDTO;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.feedback.FeedbackDTO;
import com.store.grocery.service.FeedbackService;
import com.store.grocery.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v2")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping("product/ratings")
    @ApiMessage("Create a feedback")
    public ResponseEntity<FeedbackDTO> add(@Valid @RequestBody CreateFeedbackDTO feedbackDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.feedbackService.addFeedback(feedbackDTO));
    }

    @GetMapping("product/ratings/{id}")
    @ApiMessage("Get feedbacks by product")
    public ResponseEntity<PaginationDTO> getByProductId(
            @PathVariable Long id,
            @RequestParam(value = "size", required = false) Integer size,
            Pageable pageable) {
        PaginationDTO result = feedbackService.getFeedbacksWithAdjustedSize(id, size, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("ratings")
    @ApiMessage("Get all feedbacks")
    public ResponseEntity<PaginationDTO> getAllByStatus(
            Pageable pageable,
            @RequestParam(value = "status", required = false) Boolean status,
            @RequestParam(value = "sort", required = false) String sort) {
        return ResponseEntity.ok(this.feedbackService.getBySortAndFilter(pageable, status, sort));
    }

    @PutMapping("ratings/{id}")
    @ApiMessage("Change feedback status")
    public ResponseEntity<Void> hideFeedback(@PathVariable Long id) {
        this.feedbackService.changeFeedbackStatus(id);
        return ResponseEntity.ok().build();
    }
}
