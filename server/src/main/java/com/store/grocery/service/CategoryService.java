package com.store.grocery.service;

import com.store.grocery.domain.Category;
import com.store.grocery.dto.request.category.CategoryRequest;
import com.store.grocery.dto.response.PaginationResponse;

import org.springframework.data.domain.Pageable;


public interface CategoryService {
      boolean isCategoryExisted(String name);
      Category create(CategoryRequest categoryDTO);
      boolean isCategoryNameUnique(Long id, String name);
      Category findById(long id);
      Category update(long id, CategoryRequest categoryDTO);
      void delete(long id);
      PaginationResponse fetchAllCategories(Pageable pageable);
}
