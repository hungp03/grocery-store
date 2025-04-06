package com.store.grocery.dto.request.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {
    private long id;
    private String name;
    private String imageUrl;
}
