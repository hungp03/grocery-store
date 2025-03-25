package com.app.webnongsan.service;

import com.app.webnongsan.domain.Category;
import com.app.webnongsan.domain.response.PaginationDTO;

import org.springframework.data.domain.Pageable;


public interface CategoryService {
      boolean isCategoryExisted(String name);
      Category create(Category category);
      boolean isCategoryNameUnique(Long id, String name);
      Category findById(long id);
      Category update(Category category);
      void delete(long id);
      PaginationDTO fetchAllCategories(Pageable pageable);
}
