package com.store.grocery.controller;

import com.store.grocery.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2")
public class GeminiController {
    private final GeminiService geminiService;

//    @PostMapping("/generative")
//    public Mono<String> generate(@RequestBody Map<String, String> body) {
//        return geminiService.generate(body.get("prompt"));
//    }

    @PostMapping("/generative/{id}")
    public Mono<String> generate(@PathVariable("id") Long productId, Pageable pageable) {
        return geminiService.summarizeFeedbackByProduct(productId, pageable);
    }
}
