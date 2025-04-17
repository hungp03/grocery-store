package com.store.grocery.mapper;

import com.store.grocery.domain.Feedback;
import com.store.grocery.dto.response.feedback.FeedbackResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.avatarUrl", target = "userAvatarUrl")
    @Mapping(source = "product.productName", target = "product_name")
    @Mapping(source = "ratingStar", target = "ratingStar")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "updatedAt", target = "updatedAt")
    FeedbackResponse toFeedbackResponse(Feedback feedback);
}

