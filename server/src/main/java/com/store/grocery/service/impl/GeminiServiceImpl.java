package com.store.grocery.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.feedback.FeedbackResponse;
import com.store.grocery.service.FeedbackService;
import com.store.grocery.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {
    private final WebClient webClient;
    private final FeedbackService feedbackService;
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.model-id}")
    private String modelId;

    @Override
    public Mono<String> generate(String userPrompt) {
        Map<String, Object> systemInstruction = Map.of(
                "parts", List.of(
                        Map.of("text",
                                """
                                Bạn là trợ lý AI chuyên tóm tắt đánh giá sản phẩm từ người dùng thực tế.
                                Hướng dẫn:
                                - Nếu có đủ dữ liệu: hãy **tóm tắt cả điểm tích cực và tiêu cực**, trung lập, rõ ràng, không thiên vị.
                                - Nếu phản hồi quá ngắn, mơ hồ, hoặc chỉ là biểu tượng cảm xúc: **diễn giải lại một cách lịch sự nhưng đúng ý**.
                                - Nếu **đa số là đánh giá khách quan, không chứa nội dung** (null, khoảng trống, hoặc không mang thông tin), trả lời rằng người dùng không cung cấp thông tin phản hồi cụ thể
                                Chỉ trả lại phần nội dung tóm tắt, không tách ý, nên viết trong 1 câu hoặc một đoạn, không được thêm "Tóm tắt:", không bình luận thêm.
                                """
                        )
                )
        );


        Map<String, Object> userContent = Map.of(
                "parts", List.of(
                        Map.of("text", userPrompt)
                )
        );

        Map<String, Object> body = Map.of(
                "system_instruction", systemInstruction,
                "contents", List.of(userContent)
        );

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/" + modelId + ":generateContent")
                        .queryParam("key", apiKey)
                        .build())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.path("candidates")
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText());
    }

    @Override
    public Mono<String> summarizeFeedbackByProduct(Long productId, Pageable pageable) {
        String cacheKey = String.format("generative_feedback:id=%d&p=%d&s=%d",
                productId, pageable.getPageNumber(), pageable.getPageSize());

        return Mono.defer(() -> {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Mono.just(cached);
            }

            PaginationResponse response = feedbackService.getByProductId(productId, pageable);
            List<FeedbackResponse> feedbacks = (List<FeedbackResponse>) response.getResult();

            String joinedFeedback = feedbacks.stream()
                    .map(FeedbackResponse::getDescription)
                    .filter(desc -> desc != null && !desc.isBlank())
                    .collect(Collectors.joining("\n"));

            if (joinedFeedback.isBlank()) {
                return Mono.just("Không có thông tin đánh giá cụ thể từ người dùng");
            }

            return generate(joinedFeedback)
                    .doOnNext(result -> {
                        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(15));
                    });
        });
    }
}
