package com.store.grocery.service;

import com.store.grocery.domain.Category;
import com.store.grocery.dto.request.category.CreateCategoryRequest;
import com.store.grocery.dto.request.category.UpdateCategoryRequest;
import com.store.grocery.dto.response.PaginationResponse;

import org.springframework.data.domain.Pageable;


public interface CategoryService {
      boolean isCategoryExisted(String name);
      Category create(CreateCategoryRequest categoryDTO);
      boolean isCategoryNameUnique(Long id, String name);
      Category findById(long id);
      Category update(UpdateCategoryRequest categoryDTO);
      void delete(long id);
      PaginationResponse fetchAllCategories(Pageable pageable);
}
