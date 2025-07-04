package com.store.grocery.controller;

import com.store.grocery.dto.request.feedback.CreateFeedbackRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.feedback.FeedbackResponse;
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
    public ResponseEntity<FeedbackResponse> add(@Valid @RequestBody CreateFeedbackRequest feedbackDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.feedbackService.addFeedback(feedbackDTO));
    }

    @GetMapping("product/{id}/ratings")
    @ApiMessage("Get feedbacks by product")
    public ResponseEntity<PaginationResponse> getByProductId(
            @PathVariable Long id,
            Pageable pageable) {
        PaginationResponse result = feedbackService.getByProductId(id, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("ratings")
    @ApiMessage("Get all feedbacks")
    public ResponseEntity<PaginationResponse> getAllByStatus(
            Pageable pageable,
            @RequestParam(value = "status", required = false) Boolean status,
            @RequestParam(value = "sort", required = false) String sort) {
        return ResponseEntity.ok(this.feedbackService.getBySortAndFilter(pageable, status, sort));
    }

    @PatchMapping("ratings/{id}/status")
    @ApiMessage("Change feedback status")
    public ResponseEntity<Boolean> changeFeedbackStatus(@PathVariable Long id) {
        return ResponseEntity.ok(this.feedbackService.changeFeedbackStatus(id));
    }
}
