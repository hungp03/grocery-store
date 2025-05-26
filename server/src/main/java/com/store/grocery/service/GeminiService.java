package com.store.grocery.service;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface GeminiService {
    Mono<String> generate(String prompt);
    Mono<String> summarizeFeedbackByProduct(Long productId, Pageable pageable);
}
